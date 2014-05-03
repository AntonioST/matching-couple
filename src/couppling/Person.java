/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author antonio
 */
public class Person implements Serializable{

    private static final Random rand = new Random();
    private static final WeakHashMap<String, Person> people = new WeakHashMap<>();
    //
    final String name;
    final Map<String, Set<String>> self = new HashMap<>();
    final Map<String, Set<String>> target = new HashMap<>();

    public Person(String name){
        if (name == null) {
            throw new NullPointerException("null name");
        } else if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        synchronized (people){
            if (people.containsKey(name)) {
                throw new RuntimeException("this name has already created");
            }
            this.name = name;
            people.put(name, this);
        }
    }

    void updateAttribute(Attribute[] as, String[] ss){
        int sz = ss.length;
        int asz = as.length;
        if (sz <= asz) {
            for (int i = 0; i < sz; i++) {
                as[i] = Attribute.getAttribute(ss[i]);
            }
        } else {
            LinkedList<String> ls = new LinkedList<>(Arrays.asList(ss));
            int i = 0;
            while (i < asz) {
                as[i++] = Attribute.getAttribute(ls.remove(rand.nextInt(ls.size())));
            }
            Main.log.printf("choose %s\n", Arrays.toString(as));
        }
    }

    public boolean matchable(Person other){
        if (name.equals(other.name)) {
            return false;
        }
        if (!loveGender.match(other.selfGender)) {
            return false;
        }
        return true;
    }

    public int matchAttribute(Person other){
        int p = 0;
        for (int i = 0, isz = loveAtt.length; i < isz; i++) {
            for (int j = 0, jsz = other.selfAtt.length; j < jsz; j++) {
                if (loveAtt[i] != null && loveAtt[i].match(other.selfAtt[j])) {
                    p++;
                }
            }
        }
        return getAttPoint(p);
    }

    public int matchInterest(Person other){
        int p = 0;
        for (int i = 0, isz = loveItt.length; i < isz; i++) {
            for (int j = 0, jsz = other.selfItt.length; j < jsz; j++) {
                if (loveItt[i] != null && loveItt[i].match(other.selfItt[j])) {
                    p++;
                }
            }
        }
        return getIttPoint(p);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("Person:[");
        sb.append("name:").append(name).append(", ");
        sb.append(selfGender.getDoc()).append("->").append(loveGender.getDoc()).append(", ");
        sb.append("Attribute:[");
        for (String att: selfAttS) {
            sb.append(att).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "]->[");
        for (String att: loveAttS) {
            sb.append(att).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "], ");
        sb.append("Interest:[");
        for (String itt: selfIttS) {
            sb.append(itt).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "]->[");
        for (String itt: loveIttS) {
            sb.append(itt).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "]]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;
        Person q = (Person)obj;
        return name.equals(q.name);

    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
