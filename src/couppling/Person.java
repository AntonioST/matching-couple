/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;
import java.util.WeakHashMap;

/**
 *
 * @author antonio
 */
public class Person implements Serializable{

    private static final Random rand = new Random();
    private static final int[] attps = Main.loadIntArray("point.attribute");
    private static final int[] ittps = Main.loadIntArray("point.interest");
    //
    private static final WeakHashMap<String, Person> pool = new WeakHashMap<>();
    //
    private String name;
    private Gender selfGender;
    private Gender loveGender;
    private String[] selfAttS;
    private String[] loveAttS;
    private String[] selfIttS;
    private String[] loveIttS;
    private transient Attribute[] selfAtt = new Attribute[6];
    private transient Attribute[] loveAtt = new Attribute[3];
    private transient Interest[] selfItt = new Interest[6];
    private transient Interest[] loveItt = new Interest[3];

    private static int getAttPoint(int i){
        return i < attps.length ? attps[i] : attps[attps.length - 1];
    }

    private static int getIttPoint(int i){
        return i < ittps.length ? ittps[i] : ittps[ittps.length - 1];
    }

    public Person(String name){
        if (name == null) {
            throw new NullPointerException("null name");
        } else if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        synchronized (pool){
            if (pool.containsKey(name)) {
                throw new RuntimeException("this name has already created");
            }
            this.name = name;
            pool.put(name, this);
        }
    }

    public String getName(){
        return name;
    }

    public Gender getSelfGender(){
        return selfGender;
    }

    public Gender getLoveGender(){
        return loveGender;
    }

    public Attribute[] getSelfAtt(){
        return selfAtt;
    }

    public Attribute[] getLoveAtt(){
        return loveAtt;
    }

    public Interest[] getSelfItt(){
        return selfItt;
    }

    public Interest[] getLoveItt(){
        return loveItt;
    }

    void setSelfGender(String s){
        selfGender = Gender.getGender(s);
    }

    void setLoveGender(String s){
        loveGender = Gender.getGender(s);
    }

    void setSelfAttribute(String[] line){
        selfAttS = line;
    }

    void setSelfInterest(String[] line){
        selfIttS = line;
    }

    void setLoveAttribute(String[] line){
        loveAttS = line;
    }

    void setLoveInterest(String[] line){
        loveIttS = line;
    }

    void updateAttribute(Attribute[] as, String[] ss){
        int sz = ss.length;
        int asz = as.length;
        Main.log.printf("[Person] %s update att: ", name);
        if (sz <= asz) {
            Main.log.println("choose all");
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

    void updateInterest(Interest[] is, String[] ss){
        int sz = ss.length;
        int isz = is.length;
        Main.log.printf("[Person] %s update itt: ", name);
        if (sz <= isz) {
            Main.log.println("choose all");
            for (int i = 0; i < sz; i++) {
                is[i] = Interest.getInterest(ss[i]);
            }
        } else {
            LinkedList<String> ls = new LinkedList<>(Arrays.asList(ss));
            int i = 0;
            while (i < isz) {
                is[i++] = Interest.getInterest(ls.remove(rand.nextInt(ls.size())));
            }
            Main.log.printf("choose %s\n", Arrays.toString(is));
        }
    }

    void update(){
        Main.log.printf("[Person] %s update self att\n", name);
        updateAttribute(selfAtt, selfAttS);
        Main.log.printf("[Person] %s update love att\n", name);
        updateAttribute(loveAtt, loveAttS);
        Main.log.printf("[Person] %s update self itt\n", name);
        updateInterest(selfItt, selfIttS);
        Main.log.printf("[Person] %s update love itt\n", name);
        updateInterest(loveItt, loveIttS);
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
        for (String att : selfAttS) {
            sb.append(att).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "]->[");
        for (String att : loveAttS) {
            sb.append(att).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "], ");
        sb.append("Interest:[");
        for (String itt : selfIttS) {
            sb.append(itt).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "]->[");
        for (String itt : loveIttS) {
            sb.append(itt).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "]]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if (this == obj) return true;
        if(getClass() != obj.getClass()) return false;
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
