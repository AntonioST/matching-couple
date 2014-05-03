/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.LinkedList;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author antonio
 */
public class RuleMaker{

    private static final String regx = "(.+?) *(==?) *(.+)";
    private static final Pattern pattern = Pattern.compile(regx);
    private static final Lookup look = MethodHandles.lookup();
    public static final BiPredicate<String, String> equalMatcher = (p, q) -> p == q;

    private static boolean isBothDir(String s){
        return "==".equals(s);
    }

    private static boolean isAllStar(String s){
        return "*".equals(s);
    }

    private RuleMaker(){
        throw new UnsupportedOperationException();
    }

    public static <T extends Option> MatchRule make(Class<T> cls, String[] lines){
        Matcher m = pattern.matcher("");
        LinkedList<LinkedMatchRule> ls = new LinkedList<>();
        for (String line: lines) {
            m.reset(line);
            if (m.matches()) {
                String g1 = m.group(1);
                String g3 = m.group(3);
                if ("$".equals(g1) && "$".equals(g3)) {
                    ls.add(new LinkedEqualMatchRule());
                } else if ("<".equals(g3)) {
                    EqualTest e1 = create(cls, g1);
                    ls.add(new LinkedMatchRule(e1, e1));
                } else {
                    EqualTest e1 = create(cls, g1);
                    EqualTest e2 = create(cls, g3);
                    ls.add(new LinkedMatchRule(e1, e2));
                    if (isBothDir(m.group(2))) {
                        ls.add(new LinkedMatchRule(e2, e1));
                    }
                }
            } else {
                System.err.println("gender.list format error : " + line);
            }
        }
        LinkedMatchRule ret = ls.pollFirst();
        if (ret != null) {
            LinkedMatchRule p = ret;
            while (!ls.isEmpty()) {
                p.next = ls.pollFirst();
                p = p.next;
            }
        }
        return ret;
    }

    private static <T extends Option> Option get(Class<T> cls, String s){
        try{
            MethodHandle meth = look.findStatic(cls,
                                                "get" + cls.getSimpleName(),
                                                MethodType.methodType(cls, String.class));
            return (Option)meth.invoke(s);
        } catch (Throwable ex){
            ex.printStackTrace();
        }
        return null;
    }

    private static <T extends Option> EqualTest create(Class<T> cls, String line){
        if (isAllStar(line)) {
            return star;
        }
        String[] opts = line.split("[, ]");
        LinkedList<Option> ls = new LinkedList<>();
        for (String op: opts) {
            if (op.isEmpty()) continue;
            ls.add(get(cls, op));
        }
        return new EqualTest(ls.toArray(new Option[ls.size()]));
    }

    private static class LinkedMatchRule implements MatchRule{

        LinkedMatchRule next = null;
        Predicate<Option> left = null;
        Predicate<Option> right = null;

        LinkedMatchRule(LinkedMatchRule prev, Predicate<Option> l, Predicate<Option> r){
            left = l;
            right = r;
            prev.next = this;
        }

        @Override
        public boolean match(Option p, Option q){
            //System.out.printf("%s -- %s\n", p, q);
            if (left == null || right == null) {
                return false;
            } else if (left.test(p) && right.test(q)) {
                return true;
            }
            return next == null ? false : next.match(p, q);
        }
    }

    private static class LinkedEqualMatchRule extends LinkedMatchRule{

        LinkedEqualMatchRule(LinkedMatchRule prev){
            super(prev, null, null);
        }

        @Override
        public boolean match(Option p, Option q){
            if (p == q) {
                return true;
            }
            return next == null ? false : next.match(p, q);
        }
    }

    private static Predicate<Option> equalTest(final Option op){
        return p -> p == null ? false : op == p;
    }

    private static Predicate<Option> equalTest(final Option op, final Option... ops){
        return p -> {
            if (p == null) return false;
            if (p == op) return true;
            for (Option opt: ops) {
                if (p == opt) return true;
            }
            return false;
        };
    }

    protected static final Predicate<Option> starTest = p -> true;
}
