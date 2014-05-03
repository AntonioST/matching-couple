/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author antonio
 */
public class Coupler{

    private Person[] persons;
    private Person[][] couple;
    private Person[] left;

    private static String[] deString(String s){
        if (s == null || s.isEmpty()) return new String[0];
        String src = s;
        if (s.startsWith("\"")) {
            int e = s.indexOf('"', 1);
            if (e != -1) {
                src = s.substring(1, e);
            } else {
                System.err.println("person format err : unbalanced \" pairs " + s);
                src = "";
            }
        }
        return src.split(", ");
    }

    private static String[] splitString(String s, int limit){
        if (s == null) return new String[0];
        LinkedList<String> ls = new LinkedList<>();
        int length = s.length();
        int n = 0;
        int p = 0;
        for (; n < length; n++) {
            char ch = s.charAt(n);
            if (ch == '"') {
                while ((ch = s.charAt(++n)) != '"');
                assert ch == '"';
            } else if (ch == ',') {
                ls.add(s.substring(p, n));
                p = n + 1;
                if (ls.size() == limit) {
                    break;
                }
            }
        }
        ls.add(s.substring(p, n));
        return ls.toArray(new String[ls.size()]);
    }

    public static Person[] inputPerson(String[] lines){
        LinkedList<Person> ls = new LinkedList<>();
        try (PrintStream log = Main.getStreamOrNull("log.person.create")){
            for (String line : lines) {
                String[] unit = splitString(line, 7);
                if (unit.length < 7) {
                    System.err.printf("person format error : %s\n\t%s\n",
                                      line, Arrays.toString(unit));
                    continue;
                }
                Person p = new Person(unit[0]);
                p.setSelfGender(deString(unit[1])[0]);
                p.setLoveGender(deString(unit[2])[0]);
                p.setSelfAttribute(deString(unit[3]));
                p.setLoveAttribute(deString(unit[4]));
                p.setSelfInterest(deString(unit[5]));
                p.setLoveInterest(deString(unit[6]));
                ls.add(p);
                Main.log.printf("[Coupler] create person %s\n", p.getName());
                log.println(p);
            }
        }
        return ls.toArray(new Person[ls.size()]);
    }

    public Coupler(Person[] persons){
        this.persons = persons;
        List<Person> ls = Arrays.asList(persons);


        matchCouple(ls);
        Main.log.println("[Coupler] end");
    }

    private void initPerson(Collection<Person> c){
        for (Person p : c) {
            p.update();
        }
    }

    private Map<Person, List<Score>> calculateAffinity(Collection<Person> c){
        Map<Person, List<Score>> ret = new HashMap();
        for (Person p : c) {
            LinkedList<Score> ls = new LinkedList<>();
            for (Person o : c) {
                int sc = getScore(p, o);
                if (sc >= 0) {
                    Score s = new Score(o);
                    s.score = sc;
                    ls.add(s);
                    Main.log.printf("[Coupler] Affinity: %s -> %s (%d)\n",
                                    p.getName(), o.getName(), sc);
                }

            }
            Collections.sort(ls);
            ret.put(p, ls);
        }
        return ret;
    }

    private void printAffinity(PrintStream ps, Map<Person, List<Score>> c){
        for (Person p : c.keySet()) {
            ps.printf("%-10s: ", p.getName());
            Iterator<Score> it = c.get(p).iterator();
            if (!it.hasNext()) {
                ps.println();
                continue;
            }
            ps.println(it.next());
            while (it.hasNext()) {
                ps.print("            ");
                ps.println(it.next());
            }
        }
    }

    private int getScore(Person p, Person o){
        if (p == o || !p.matchable(o) || !o.matchable(p)) {
            return -2;
        }
        int ats = p.matchAttribute(o);
        int its = p.matchInterest(o);
        return ats * its + ats + its;
    }

    private Map<Person, Deque<Person>> createTmpMap(Map<Person, List<Score>> m){
        Map<Person, Deque<Person>> ret = new HashMap<>();
        for (Person p : m.keySet()) {
            List<Score> sls = m.get(p);
            LinkedList<Person> pls = new LinkedList<>();
            for (Score sp : sls) {
                if (sp.score > 0) {
                    pls.add(sp.person);
                }
            }
            ret.put(p, pls);
        }
        return ret;
    }

    private void matchCouple(List<Person> ps){
        Map<Person, List<Score>> sc = new HashMap();
        Map<Person, Deque<Person>> table = new HashMap<>();
        //
        for (Person p : ps) {
            sc.put(p, null);
            table.put(p, null);
        }
        //
        Main.log.println("[Coupler] mapping couple start");
        List<Couple> ls = new LinkedList<>();
        try (PrintStream log = Main.getStreamOrNull("log.couple.match");
            PrintStream aff = Main.getStreamOrNull("log.couple.affinity")){
            int size = 0;
            do{
                size = ls.size();
                Main.log.println("[Coupler] update person");
                initPerson(sc.keySet());
                Main.log.println("[Coupler] calculate affinity");
                sc = calculateAffinity(table.keySet());
                aff.println("new result");
                printAffinity(aff, sc);
                table = createTmpMap(sc);
                Main.log.println("[Coupler] mapping couple");
                ls.addAll(matchCoupleRound(log, table));
            } while (size != ls.size());
        }
        Main.log.println("[Coupler] mapping couple finished");
        //
        Main.log.println("[Coupler] print left affinity");
        sc = calculateAffinity(table.keySet());
        try (PrintStream lefts = Main.getStreamOrNull("log.couple.left")){
            lefts.printf("left people count %s\n", sc.size());
            printAffinity(lefts, sc);
        }
        //
        Main.log.println("[Coupler] create couple, left array");
        createCoupleArray(ls);
        createLeftArray(table.keySet());
    }

    private void createCoupleArray(List<Couple> ls){
        Main.log.println("[Coupler] create couple array");
        couple = new Person[ls.size()][2];
        for (int i = ls.size() - 1; i >= 0; i--) {
            Couple c = ls.get(i);
            couple[i][0] = c.p;
            couple[i][1] = c.q;
        }
    }

    private void createLeftArray(Collection<Person> c){
        left = c.toArray(new Person[c.size()]);
    }

    private List<Couple> matchCoupleRound(PrintStream log, Map<Person, Deque<Person>> table){
        List<Couple> ls = new LinkedList<>();
        int size = 0;
        int preSize;
        Main.log.println("[Coupler] mapping couple start round");
        do{
            preSize = size;
            ls.addAll(matchCoupleFirst(log, table));
            ls.addAll(matchCoupleChain(log, table));
            size = ls.size();
        } while (preSize != size);
        //
        Main.log.println("[Coupler] mapping couple by back");
        ls.addAll(matchCoupleBack(log, table));
        Main.log.println("[Coupler] couple number +" + ls.size());
        //
        Main.log.println("[Coupler] mapping couple by single");
        ls.addAll(matchCoupleSingle(log, table));
        Main.log.println("[Coupler] couple number +" + ls.size());
        //
        Main.log.println("[Coupler] mapping couple finished round");
        return ls;
    }

    private List<Couple> matchCoupleFirst(PrintStream log, Map<Person, Deque<Person>> table){
        List<Couple> ls = new LinkedList<>();
        boolean escape = true;
        while (escape) {
            escape = false;
            Set<Person> set = new HashSet(table.keySet());
            for (Person p : set) {
                Deque<Person> dqp = table.get(p);
                //p has coupled with another
                if (dqp == null) {
                    continue;
                }
                Person q = dqp.peekFirst();
                // remove the person who has been a couple with another in the list
                while (q != null && !table.containsKey(q)) {
                    dqp.removeFirst();
                    q = dqp.peekFirst();
                }
                if (q == null) {
                    // all person list in p are matched by others
                    continue;
                }
                // both are (fake) first one
                // q may be null
                if (p.equals(table.get(q).peekFirst())) {
                    Couple c = new Couple(p, q);
                    ls.add(c);
                    // remove both from table
                    table.remove(p);
                    table.remove(q);
                    // table has modify
                    escape = true;
                    //
                    log.print("First Match : ");
                    log.println(c);
                }
            }
        }
        return ls;
    }

    private List<Couple> matchCoupleChain(PrintStream log, Map<Person, Deque<Person>> table){
        List<Couple> ls = new LinkedList<>();
        boolean escape = true;
        while (escape) {
            escape = false;
            Set<Person> set = new HashSet(table.keySet());
            for (Person p : set) {
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
                        ls.add(c);
                        log.printf("Cycle Match : %-5s : %s\n", symbo, c);
                        escape = true;
                    }
                }
            }
        }
        return ls;
    }

    // may be unnessary
    private List<Couple> matchCoupleBack(PrintStream log, Map<Person, Deque<Person>> table){
        List<Couple> ls = new LinkedList<>();
        Set<Person> set = new HashSet(table.keySet());
        for (Person p : set) {
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

    private List<Couple> matchCoupleSingle(PrintStream log, Map<Person, Deque<Person>> table){
        List<Couple> ls = new LinkedList<>();
        Set<Person> set = new HashSet(table.keySet());
        for (Person p : set) {
            List<Person> path = new ArrayList<>(matchCoupleChainFind(log, table, p, 2));
            // should compare between prev and next
            if (path.size() >= 2) {
                Couple c = new Couple(path.get(0), path.get(1));
                ls.add(c);
                table.remove(c.p);
                table.remove(c.q);
                log.printf("Single Match : %s\n", c);
            }
        }
        return ls;
    }

    private List<Person> matchCoupleChainFind(PrintStream log, Map<Person, Deque<Person>> table,
                                              Person start, int limit){
        if (!table.containsKey(start)) {
            return Collections.EMPTY_LIST;
        }
        LinkedList<Person> path = new LinkedList<>();
        HashSet<Person> set = new HashSet<>();
        Person ptr = start;
        while (ptr != null && !set.contains(ptr)) {
            set.add(ptr);
            path.add(ptr);
            Deque<Person> dq = table.get(ptr);
            if (dq == null) {
                ptr = null;
            } else {
                for (;;) {
                    ptr = dq.peekFirst();
                    if (ptr == null || table.containsKey(ptr)) {
                        break;
                    }
                    dq.removeFirst();
                }
            }
        }
        if (path.size() <= limit) {
            return Collections.EMPTY_LIST;
        }
        // log
        log.print("find chain : ");
        for (Person p : path) {
            log.print(p.getName());
            log.print(" -> ");
        }
        log.println();
        return path;
    }

    public void outputResultAsCsv() throws IOException{
        PrintStream ps = Main.getStream("file.output", "stdout");
        HashSet<Person> set = new HashSet<>();
        ps.printf("couple,%d\n", couple.length);
        if (couple != null) {
            for (Person[] p : couple) {
                ps.printf("%s,%s\n", p[0].getName(), p[1].getName());
                if (!set.add(p[0])) {
                    ps.println("wran : " + p[0].getName());
                }
                if (!set.add(p[1])) {
                    ps.println("wran : " + p[1].getName());
                }
            }
        }
        ps.printf("\nleft,%d\n", left.length);
        if (left != null) {
            for (Person p : left) {
                ps.println(p.getName());
                if (!set.add(p)) {
                    ps.println("wran : " + p.getName());
                }
            }
        }

    }

    private static class Score implements Comparable<Score>{

        int score = -1;
        Person person;

        public Score(Person person){
            this.person = person;
        }

        @Override
        public int compareTo(Score o){
            return o.score - score;
        }

        @Override
        public String toString(){
            return String.format("%s (%d)", person.getName(), score);
        }
    }

    private static class Couple{

        Person p;
        Person q;

        public Couple(Person p, Person q){
            this.p = p;
            this.q = q;
            Main.log.printf("[Coupler.Couple] new couple %s -- %s\n", p.getName(), q.getName());
        }

        Person[] toArray(){
            return new Person[]{p, q};
        }

        @Override
        public String toString(){
            return new StringBuilder().append(p.getName()).append(" -- ").append(q.getName())
                .toString();
        }
    }
}
