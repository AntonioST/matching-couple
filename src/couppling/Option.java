/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author antonio
 */
public abstract class Option implements Serializable{


    //
    private String doc;

    public Option(String doc){
        this.doc = doc;
        Main.log.printf("[%s] create: %s\n", getClass().getSimpleName(), doc);
    }

    public String getDoc(){
        return doc;
    }

    public MatchRule getRule(){
        return RuleMaker.equalMatcher;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Option that = (Option)obj;
        if (doc == null ? that.doc != null : !doc.equals(that.doc)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.doc);
        return hash;
    }

    public boolean match(Option o){
        return getRule().match(this, o);
    }

    @Override
    public String toString(){
        return doc;
    }
}
