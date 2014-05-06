/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package couppling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author antonio
 */
public class MatchRule{

    private static final String R = "\\$(\\w+)(\\[\\d+(,\\d+)*\\])?";
    private static final Pattern P = Pattern.compile(R);
    private static final String FUNC_NAME = "match";
    private static final String FUNC_HEAD = "function " + FUNC_NAME + "(){";
    private static final String FUNC_LINE = FUNC_HEAD + "return %s;}";
    private static final String FUNC_LINES = FUNC_HEAD + "%s;}";
    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    private final Map<String, int[]> set = new HashMap<>();

    public MatchRule(String eval){
        initSet(eval);
        try{
            engine.eval(String.format(FUNC_LINE, eval));
        } catch (ScriptException ex){
            throw new RuntimeException(ex);
        }

    }

    public MatchRule(List<String> block){
        String eval = String.join(";", block);
        initSet(eval);
        try{
            engine.eval(String.format(FUNC_LINES, eval));
        } catch (ScriptException ex){
            throw new RuntimeException(ex);
        }
    }

    private void initSet(String line){
        Matcher m = P.matcher(line);
        int[] empty = new int[0];
        if (m.find()) {
            do{
                String key = m.group(1);
                if (m.group(2) != null) {
                    String p = m.group(2);
                    p = p.substring(1, p.length() - 1);
                    set.put(key, Stream.of(p.split(",")).mapToInt(Integer::parseInt).toArray());
                } else {
                    set.put(key, empty);
                }
                line = m.replaceAll(key);
                engine.put(key, null);
            } while (m.reset(line).find());
        }
    }

    public int matchScore(Person p, Person q){
        set.forEach((k, ps) -> {
            int i = p.match(q, k);
            if (ps.length == 0) {
            } else if (i >= ps.length) {
                i = ps[ps.length - 1];
            } else {
                i = ps[i];
            }
            engine.put(k, i);
        });
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
