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

    public Coupler(MatchRule rule){
        this.rule = rule;
    }

    Map<Person, List<Score>> calculateAffinity(Collection<Person> c){
        Map<Person, List<Score>> ret = new HashMap();
        c.forEach(p -> ret.put(p, c.stream()
                               .filter(t -> t != p)
                               .map(t -> new Score(p, t, rule.matchScore(p, t)))
                               .filter(s -> s.score >= 0)
                               .sorted()
                               .collect(Collectors.toList())));
        return ret;
    }

    List<Score> matchCouple(List<Person> ps){
        Map<Person, List<Score>> table;
        //
        List<Score> result = new LinkedList<>();
        int size;
        do{
            size = result.size();
            table = calculateAffinity(ps);
            result.addAll(matchCoupleRound(table));
        } while (size != result.size());
        return result;
    }

    List<Score> matchCoupleRound(Map<Person, List<Score>> table){
        List<Score> ret = new LinkedList<>();
        int size = 0;
        int preSize;
        do{
            preSize = size;
            ret.addAll(matchCoupleFirst(table));
            ret.addAll(matchCoupleChain(table));
            size = ret.size();
        } while (preSize != size);
        //
        ret.addAll(matchCoupleBack(table));
        //
        ret.addAll(matchCoupleSingle(table));
        //
        return ret;
    }

    Map<Person, List<Score>> removeFromTable(Map<Person, List<Score>> table, Person p){
        table.remove(p);
        table.values().forEach(list -> list.removeIf(it -> it.self == p || it.target == p));
        return table;
    }

    List<Score> matchCoupleFirst(Map<Person, List<Score>> table){
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

    List<Score> matchCoupleChain(Map<Person, List<Score>> table){
        List<Score> ret = new LinkedList<>();
        for (Person p: new HashSet<>(table.keySet())) {
            List<Score> path = matchCoupleChainFind(table, p, 3);
            if (path.isEmpty()) continue;
            // pre --> inx --> nxt
            Score pre = path.get(0);
            Score nxt = path.get(1);
            //
            int flag = 0;
            if (pre.score > 0 && nxt.score > 0) {
                // pre <- inx <- nxt
                int bpi = pre.score + rule.matchScore(pre.target, pre.self);
                int bin = nxt.score + rule.matchScore(nxt.target, nxt.self);
                if (bpi > bin) {
                    flag = 1;// pre <--> inx <-> nxt
                } else if (bpi < bin) {
                    flag = 2;// pre <-> inx <--> nxt
                } else if (pre.score > nxt.score) {
                    flag = 1;// pre <-- inx <- nxt
                } else {
                    flag = 2;//pre <- inx <-- nxt
                }
            } else if (pre.score > 0) {
                flag = 1;// pre <- inx x- nxt
            } else if (nxt.score > 0) {
                flag = 2;// pre x- inx <- nxt
            } else {
                // pre x- inx x- nxt
            }
            if (flag != 0) {
                if (flag == 1) {
                    ret.add(pre);
                    removeFromTable(table, pre.self);
                    removeFromTable(table, pre.target);
                } else if (flag == 2) {
                    ret.add(nxt);
                    removeFromTable(table, pre.self);
                    removeFromTable(table, pre.target);
                } else {
                    throw new IllegalStateException();
                }
            }
        }
        return ret;
    }

    // may be unnessary
    List<Score> matchCoupleBack(Map<Person, List<Score>> table){
        return new HashSet<>(table.keySet()).stream()
          .map(p -> matchCoupleChainFind(table, p, 2))
          .filter(ls -> !ls.isEmpty())
          .map(ls -> ls.get(0))
          .filter(s -> s.score > 0)
          .peek(s -> removeFromTable(table, s.self))
          .peek(s -> removeFromTable(table, s.target))
          .collect(Collectors.toList());
    }

    List<Score> matchCoupleSingle(Map<Person, List<Score>> table){
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

    List<Score> matchCoupleChainFind(Map<Person, List<Score>> table, Person start, int limit){
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
