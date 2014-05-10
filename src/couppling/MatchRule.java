/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package couppling;

import java.util.HashSet;
import java.util.List;
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

    //\$(\w+)(\[\d+(,\d+)*\])?
    //$test
    private static final String R = "\\$(\\w+)";
    private static final Pattern P = Pattern.compile(R);
    private static final String FUNC_NAME = "match";
    private static final String FUNC_HEAD = "function " + FUNC_NAME + "(){";
    private static final String FUNC_LINE = FUNC_HEAD + "return %s;}";
    private static final String FUNC_LINES = FUNC_HEAD + "%s;}";
    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    private final Set<String> set = new HashSet<>();

    public MatchRule(String eval){
        try{
            String line = String.format(FUNC_LINE, initSet(eval));
            System.out.println("eval : " + line);
            engine.eval(line);
        } catch (ScriptException ex){
            throw new RuntimeException(ex);
        }

    }

    public MatchRule(List<String> block){
        try{
            String line = String.format(FUNC_LINES, initSet(String.join(";", block)));
            System.out.println("eval : " + line);
            engine.eval(line);
        } catch (ScriptException ex){
            throw new RuntimeException(ex);
        }
    }

    private String initSet(String line){
        Matcher m = P.matcher(line);
        if (m.find()) {
            do{
                String key = m.group(1);
                System.out.println("catch " + key);
                set.add(key);
                line = m.replaceFirst(key);
                engine.put(key, null);
            } while (m.reset(line).find());
        }
        System.out.println("init set :" + set);
        return line;
    }

    public int matchScore(Person p, Person q){
        set.forEach(k -> engine.put(k, p.match(q, k)));
        Object ret = null;
        try{
            ret = ((Invocable)engine).invokeFunction(FUNC_NAME);
        } catch (ScriptException | NoSuchMethodException ex){
            throw new RuntimeException(ex);
        }
        if (ret == null) {
            throw new RuntimeException("null return");
        } else if (ret instanceof Number) {
            return ((Number)ret).intValue();
        }
        throw new RuntimeException("unknown return type : " + ret.getClass());
    }
}
