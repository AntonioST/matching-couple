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

    public int match(Person target, String category){
        Set<String> selfops = self.get(category);
        Set<String> targetops = target.self.get(category);
        if (selfops == null || targetops == null) return 0;
        Option op = Option.get(category);
        if (op == null) {
            throw new RuntimeException("no such category");
        }
        return selfops.parallelStream().mapToInt(a
          -> targetops.parallelStream()
          .mapToInt(b -> op.rule.test(a, b) ? 1 : 0).sum()
        ).sum();
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
