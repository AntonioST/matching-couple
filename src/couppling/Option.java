/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiPredicate;

/**
 *
 * @author antonio
 */
public class Option implements Serializable{

    public static final Map<String, Option> SET = new HashMap<>();
    public final String category;
    public final Set<String> defSet;
    public final Set<String> allSet;
    public final BiPredicate<String, String> rule;

    public Option(String category, String[] contents){
        this(category, contents, Objects::equals);
    }

    public Option(String category, String[] contents, BiPredicate<String, String> rule){
        if (SET.containsKey(category)) {
            throw new RuntimeException(category + " has existed");
        }
        this.category = category;
        defSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(contents)));
        this.rule = rule;
        allSet = new HashSet<>();
        allSet.addAll(defSet);
        SET.put(category, this);
        System.out.println("+Option " + category + defSet.toString());
    }

    public static Option get(String category){
        return SET.get(category);
    }

    public void add(String content){
        allSet.add(content);
    }

    public boolean contains(String content){
        return allSet.contains(content);
    }

}
