/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.util.*;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author antonio
 */
public class MatchHandle{

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

    public MatchHandle(MatchRule rule){
        this.rule = rule;
    }

    void printTable(Map<Person, List<Score>> table){
        List<Person> ls = new ArrayList<>(table.keySet());
        ls.sort(Comparator.comparing(p -> p.name));
        ToIntBiFunction<List<Score>, Person> extra = (t, p) -> {
            for (Score s: t) {
                if (s.self.equals(p)) return s.score;
            }
            return -1;
        };
        ls.forEach(p -> {
            StringJoiner j = new StringJoiner(",", "[", "]");
            List<Score> row = table.get(p);
            ls.stream()
              .mapToInt(t -> extra.applyAsInt(row, t))
              .mapToObj(i -> String.format("%2d", i))
              .forEach(j::add);
            System.out.println(p.name + j.toString());
        });
    }

    Map<Person, List<Score>> calculateAffinity(Collection<Person> c){
        Map<Person, List<Score>> ret = new HashMap();
        c.forEach(p -> ret.put(p, c.stream()
                               .filter(t -> t != p)
                               .map(t -> new Score(p, t, rule.matchScore(p, t)))
                               .filter(s -> s.score > 0)
                               .sorted()
                               .collect(Collectors.toList())));
        return ret;
    }

    List<Score> matchCouple(List<Person> ps, List<Person> left){
        left.clear();
        System.out.println("matching");
        Map<Person, List<Score>> table = calculateAffinity(ps);
        //
        List<Score> result = matchCoupleRound(table);
        ps.stream()
          .filter(p -> result.stream().noneMatch(s -> s.self.equals(p) || s.target.equals(p)))
          .forEach(left::add);
        System.out.println("match " + result);
        System.out.println("left " + left);
        return result;
    }

    List<Score> matchCoupleRound(Map<Person, List<Score>> table){
        int size = 0;
        int preSize;
        List<Score> ret = new ArrayList<>();
        do{
            printTable(table);
            preSize = size;
            ret.addAll(matchCoupleFirst(table));
            ret.addAll(matchCoupleChain(table));
            size = ret.size();
        } while (preSize != size);
        ret.addAll(matchCoupleBack(table));
        ret.addAll(matchCoupleSingle(table));
        return ret;
    }

    Map<Person, List<Score>> removeFromTable(Map<Person, List<Score>> table, Person p){
        table.remove(p);
        table.values()
          .forEach(list -> list.removeIf(it -> it.self.equals(p) || it.target.equals(p)));
        return table;
    }

    List<Score> matchCoupleFirst(Map<Person, List<Score>> table){
        System.out.print("matching first");
        List<Score> ret = new ArrayList<>();
        new ArrayList<>(table.keySet()).forEach(self -> {
            Stream.of(table.get(self))
              .filter(sls -> sls != null && !sls.isEmpty())
              .map(sls -> table.get(sls.get(0).target))
              .filter(tls -> tls != null && !tls.isEmpty())
              .map(tls -> tls.get(0))
              .filter(sc -> sc.target.equals(self))
              .peek(sc -> removeFromTable(table, sc.self))
              .peek(sc -> removeFromTable(table, sc.target))
              .forEach(ret::add);
        });
        System.out.println(ret);
        return ret;
    }

    List<Score> matchCoupleChain(Map<Person, List<Score>> table){
        System.out.print("matching chain");
        List<Score> ret = new LinkedList<>();
        for (Person p: new ArrayList<>(table.keySet())) {
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
        System.out.println(ret);
        return ret;
    }

    // may be unnessary
    List<Score> matchCoupleBack(Map<Person, List<Score>> table){
        System.out.print("matching back");
        List<Score> ret = new ArrayList<>(table.keySet()).stream()
          .map(p -> matchCoupleChainFind(table, p, 2))
          .filter(ls -> !ls.isEmpty())
          .map(ls -> ls.get(0))
          .filter(s -> s.score > 0)
          .peek(s -> removeFromTable(table, s.self))
          .peek(s -> removeFromTable(table, s.target))
          .collect(Collectors.toList());
        System.out.println(ret);
        return ret;
    }

    List<Score> matchCoupleSingle(Map<Person, List<Score>> table){
        System.out.print("matching single");
        List<Score> ret = new LinkedList<>();
        new ArrayList<>(table.keySet()).stream()
          .map(p -> matchCoupleChainFind(table, p, 2))
          .filter(r -> r.size() >= 2)
          .findAny()
          .map(r -> r.get(0))
          .ifPresent(sc -> {
              ret.add(sc);
              removeFromTable(table, sc.self);
              removeFromTable(table, sc.target);
          });
        System.out.println(ret);
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
