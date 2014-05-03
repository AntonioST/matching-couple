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
public class Attribute extends Option{

    public static final String[] ATTRIBUTE_NAME = Main.loadList("list.attribute");
    public static final List<Attribute> DEFAULT_ATTRIBUTE;

    static{
        DEFAULT_ATTRIBUTE = new ArrayList<>(ATTRIBUTE_NAME.length);
        for (String s : ATTRIBUTE_NAME) {
            DEFAULT_ATTRIBUTE.add(new Attribute(s));
        }
    }
    public static final Map<String, Attribute> ALL_ATTRIBUTE = new HashMap<>();

    static{
        for (Attribute att : DEFAULT_ATTRIBUTE) {
            ALL_ATTRIBUTE.put(att.getDoc(), att);
        }
    }
    private static final MatchRule rule;

    static{
        MatchRule r = Main.loadRule(Attribute.class, "rule.attribute");
        rule = r == null ? RuleMaker.equalMatcher : r;
    }

    @Override
    public MatchRule getRule(){
        return rule;
    }

    public static Attribute getAttribute(String s){
        Attribute att = ALL_ATTRIBUTE.get(s);
        if (att == null) {
            synchronized (ALL_ATTRIBUTE){
                att = ALL_ATTRIBUTE.get(s);
                if (att == null) {
                    Main.log.println("[Attribute] new create");
                    att = new Attribute(s);
                    ALL_ATTRIBUTE.put(s, att);
                }
            }
        }
        return att;
    }

    private Attribute(String doc){
        super(doc);
    }

    private Object readResolve(){
        Attribute att = ALL_ATTRIBUTE.get(getDoc());
        return att == null ? this : att;
    }

    @Override
    public String toString(){
        return "Attribute[" + super.toString() + "]";
    }
}
