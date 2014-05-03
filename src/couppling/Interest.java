/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author antonio
 */
public class Interest extends Option{

    public static final String[] INTEREST_NAME = Main.loadList("list.interest");
    public static final List<Interest> DEFAULT_INTEREST;

    static{
        DEFAULT_INTEREST = new ArrayList<>(INTEREST_NAME.length);
        for (String s : INTEREST_NAME) {
            DEFAULT_INTEREST.add(new Interest(s));
        }
    }
    public static final Map<String, Interest> ALL_INTEREST = new HashMap<>();

    static{
        for (Interest itt : DEFAULT_INTEREST) {
            ALL_INTEREST.put(itt.getDoc(), itt);
        }
    }
    private static final MatchRule rule;

    static{
        MatchRule r = Main.loadRule(Interest.class, "rule.interest");
        rule = r == null ? RuleMaker.equalMatcher : r;
    }

    @Override
    public MatchRule getRule(){
        return rule;
    }

    public static Interest getInterest(String s){
        Interest itt = ALL_INTEREST.get(s);
        if (itt == null) {
            synchronized (ALL_INTEREST){
                itt = ALL_INTEREST.get(s);
                if (itt == null) {
                    Main.log.println("[Interest] new create");
                    itt = new Interest(s);
                    ALL_INTEREST.put(s, itt);
                }
            }
        }
        return itt;
    }

    private Interest(String doc){
        super(doc);
    }

    private Object readResolve(){
        Interest itt = ALL_INTEREST.get(getDoc());
        return itt == null ? this : itt;
    }

    @Override
    public String toString(){
        return "Interest[" + super.toString() + "]";
    }
}
