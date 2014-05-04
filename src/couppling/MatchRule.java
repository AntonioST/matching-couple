/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package couppling;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author antonio
 */
public class MatchRule{

    private static final String R = "\\w+";
    private static final Pattern P = Pattern.compile(R);
    private static final String FUNC_NAME = "match";
    private static final String FUNC = "function " + FUNC_NAME + "(){return %s;}";
    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    private final Set<String> set = new HashSet<>();

    public MatchRule(String eval){
        try{
            engine.eval(String.format(FUNC, eval));
        } catch (ScriptException ex){
            throw new RuntimeException(ex);
        }
        Matcher m = P.matcher(eval);
        int start = 0;
        while (m.find(start)) {
            set.add(m.group());
            start = m.end();
        }
    }

    public int matchScore(Person p, Person q){
        set.forEach(c -> engine.put(c, p.match(q, c)));
        Object ret = null;
        try{
            ret = ((Invocable)engine).invokeFunction(FUNC_NAME);
        } catch (ScriptException | NoSuchMethodException ex){
            throw new RuntimeException(ex);
        }
        if (ret == null) {
            throw new RuntimeException("null return");
        } else if (ret instanceof Integer) {
            return (Integer)ret;
        }
        throw new RuntimeException("unknown return type : " + ret.getClass());
    }
}
