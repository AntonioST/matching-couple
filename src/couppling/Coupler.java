/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author antonio
 */
public class Coupler{

    public static class Score implements Comparable<Score>{

        public final int score;
        public final Person self;
        public final Person target;

        Score(Person self, Person target, int score){
            this.self = self;
            this.target = target;
            this.score = score;
        }

        @Override
        public int compareTo(Score o){
            return o.score - score;
        }

        @Override
        public String toString(){
            return String.format("%s -> %s (%d)", self.name, target.name, score);
        }
    }

    MatchRule rule;

    private Map<Person, List<Score>> calculateAffinity(Collection<Person> c){
        Map<Person, List<Score>> ret = new HashMap();
        c.forEach(p -> ret.put(p, c.stream()
                               .filter(t -> t != p)
                               .map(t -> new Score(p, t, rule.matchScore(p, t)))
                               .filter(s -> s.score >= 0)
                               .sorted()
                               .collect(Collectors.toList())));
        return ret;
    }

    private void matchCouple(List<Person> ps){
        Map<Person, List<Score>> sc = new HashMap();
        Map<Person, Deque<Person>> table = new HashMap<>();
        //
        for (Person p: ps) {
            sc.put(p, null);
            table.put(p, null);
        }
        //
        List<Couple> ls = new LinkedList<>();
        int size = 0;
        do{
            size = ls.size();
            sc = calculateAffinity(table.keySet());
            table = createTmpMap(sc);
            ls.addAll(matchCoupleRound(log, table));
        } while (size != ls.size());
        //
        sc = calculateAffinity(table.keySet());
        //
    }

    private List<Couple> matchCoupleRound(Map<Person, Deque<Person>> table){
        List<Couple> ls = new LinkedList<>();
        int size = 0;
        int preSize;
        do{
            preSize = size;
            ls.addAll(matchCoupleFirst(log, table));
            ls.addAll(matchCoupleChain(log, table));
            size = ls.size();
        } while (preSize != size);
        //
        ls.addAll(matchCoupleBack(log, table));
        //
        ls.addAll(matchCoupleSingle(log, table));
        //
        return ls;
    }

    private Map<Person, List<Score>> removeFromTable(Map<Person, List<Score>> table, Person p){
        table.remove(p);
        table.values().forEach(list -> list.removeIf(it -> it.self == p || it.target == p));
        return table;
    }

    private List<Score> matchCoupleFirst(Map<Person, List<Score>> table){
        List<Score> ret = new LinkedList<>();
        table.forEach((self, sls)
          -> sls.stream()
          .map(t -> table.get(t.target))
          .filter(tls -> tls.size() > 0)
          .map(tls -> tls.get(0))
          .filter(score -> score.target == self)
          .findAny()
          .ifPresent(s -> ret.add(s)));
        ret.forEach(score -> {
            removeFromTable(table, score.self);
            removeFromTable(table, score.target);
        });
        return ret;
    }

    private List<Score> matchCoupleChain(Map<Person, List<Score>> table){
        List<Score> ret = new LinkedList<>();
        boolean escape = true;
        while (escape) {
            escape = false;
            Set<Person> set = new HashSet(table.keySet());
            for (Person p: set) {
                List<Person> path = new ArrayList<>(matchCoupleChainFind(log, table, p, 3));
                int sz = path.size();
                for (int i = 1; i < sz - 1; i++) {
                    // pre --> inx --> nxt
                    Person pre = path.get(i - 1);
                    if (pre == null) {
                        continue;
                    }
                    Person inx = path.get(i);
                    Person nxt = path.get(i + 1);
                    if (nxt == null) {
                        continue;
                    }
                    // person may be null
                    int i2p = getScore(inx, pre);
                    int n2i = getScore(nxt, inx);
                    //
                    int flag = 0;
                    String symbo = "";
                    if (i2p > 0 && n2i > 0) {
                        // pre <- inx <- nxt
                        int bpi = i2p + getScore(pre, inx);
                        int bin = n2i + getScore(inx, nxt);
                        if (bpi > bin) {
                            flag = 1;// pre <--> inx <-> nxt
                            symbo = "<-><>";
                        } else if (bpi < bin) {
                            flag = 2;// pre <-> inx <--> nxt
                            symbo = "<><->";
                        } else if (i2p > n2i) {
                            flag = 1;// pre <-- inx <- nxt
                            symbo = "<-<";
                        } else {
                            flag = 2;//pre <- inx <-- nxt
                            symbo = "<<-";
                        }
                    } else if (i2p > 0) {
                        flag = 1;// pre <- inx x- nxt
                        symbo = "<x";
                    } else if (n2i > 0) {
                        flag = 2;// pre x- inx <- nxt
                        symbo = "x<";
                    } else {
                        // pre x- inx x- nxt
                    }
                    if (flag != 0) {
                        Couple c;
                        if (flag == 1) {
                            c = new Couple(pre, inx);
                            table.remove(pre);
                            table.remove(inx);
                            path.set(i, null);
                            path.set(i - 1, null);
                        } else if (flag == 2) {
                            c = new Couple(inx, nxt);
                            table.remove(inx);
                            table.remove(nxt);
                            path.set(i, null);
                            path.set(i + 1, null);
                        } else {
                            throw new IllegalStateException();
                        }
                        ret.add(c);
                        log.printf("Cycle Match : %-5s : %s\n", symbo, c);
                        escape = true;
                    }
                }
            }
        }
        return ret;
    }

    // may be unnessary
    private List<Score> matchCoupleBack(Map<Person, List<Score>> table){
        List<Score> ls = new LinkedList<>();
        Set<Person> set = new HashSet(table.keySet());
        for (Person p: set) {
            List<Person> path = new ArrayList<>(matchCoupleChainFind(log, table, p, 2));
            int sz = path.size();
            for (int i = 0; i < sz - 1; i++) {
                Person pi = path.get(i);
                Person qi = path.get(i + 1);
                if (getScore(qi, qi) > 0) {
                    assert false;
                    Couple c = new Couple(pi, qi);
                    ls.add(c);
                    table.remove(pi);
                    table.remove(qi);
                    i++;
                    log.printf("Back Match : %s\n", c);
                }
            }
        }
        return ls;
    }

    private List<Score> matchCoupleSingle(Map<Person, List<Score>> table){
        List<Score> ret = new LinkedList<>();
        table.keySet().stream()
          .map(p -> matchCoupleChainFind(table, p, 2))
          .filter(r -> r.size() >= 2)
          .findAny()
          .ifPresent(r -> ret.add(r.get(0)));
        ret.forEach(sc -> {
            removeFromTable(table, sc.self);
            removeFromTable(table, sc.target);
        });
        return ret;
    }

    private List<Score> matchCoupleChainFind(Map<Person, List<Score>> table, Person start, int limit){
        if (!table.containsKey(start)) {
            return Collections.EMPTY_LIST;
        }
        List<Score> path = new LinkedList<>();
        HashSet<Person> set = new HashSet<>();
        Person next = start;
        while (next != null && set.add(next)) {
            List<Score> ls = table.get(next);
            if (ls.size() < 0) break;
            Score s = ls.get(0);
            path.add(s);
            next = s.target;

        }
        //path.size + 1 >= limit
        if (path.size() + 1 < limit) {
            return Collections.EMPTY_LIST;
        }
        return path;
    }
}
