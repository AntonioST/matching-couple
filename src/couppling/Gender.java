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
public class Gender extends Option{

    public static final String[] GENDER_NAME = Main.loadList("list.gender");
    public static final List<Gender> DEFAULT_GENDER;

    static{
        DEFAULT_GENDER = new ArrayList<>(GENDER_NAME.length);
        for (String s : GENDER_NAME) {
            DEFAULT_GENDER.add(new Gender(s));
        }
    }
    public static final Map<String, Gender> ALL_GENDER = new HashMap<>();

    static{
        for (Gender g : DEFAULT_GENDER) {
            ALL_GENDER.put(g.getDoc(), g);
        }
    }
    private static final MatchRule rule;

    static{
        MatchRule r = Main.loadRule(Gender.class, "rule.gender");
        rule = r == null ? RuleMaker.equalMatcher : r;
    }

    @Override
    public MatchRule getRule(){
        return rule;
    }

    public static Gender getGender(String s){
        Gender g = ALL_GENDER.get(s);
        if (g == null) {
            synchronized (ALL_GENDER){
                g = ALL_GENDER.get(s);
                if (g == null) {
                    g = new Gender(s);
                    ALL_GENDER.put(s, g);
                }
            }
        }
        return g;
    }

    private Gender(String doc){
        super(doc);
    }

    private Object readResolve(){
        Gender g = ALL_GENDER.get(getDoc());
        return g == null ? this : g;
    }

    @Override
    public String toString(){
        return "Gender[" + super.toString() + "]";
    }
}
