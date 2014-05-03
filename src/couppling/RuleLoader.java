/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package couppling;

import cclo.ngraph.util.loader.LineBaseLoader;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author antonio
 */
public class RuleLoader extends LineBaseLoader<BiPredicate<String, String>>{

    private static final String R = "(.+?) *(==?) *(.+)";
    private static final Pattern P = Pattern.compile(R);
    private BiPredicate<String, String> head;
    private BiPredicate<String, String> pre;

    @Override
    protected boolean startLoading(){
        head = null;
        pre = null;
        return super.startLoading();
    }

    @Override
    protected void endLoading(){
        setResult(head);
    }

    @Override
    protected void parseLine(String line) throws IOException{
        Matcher m = P.matcher(line);
        if (!m.matches()) {
            throw new IOException("illegal format : " + line);
        }
        BiPredicate<String, String> tmp;
        String lhs = m.group(1);
        String rhs = m.group(3);
        if (lhs.equals("$") && rhs.equals("$")) {
            tmp = Objects::equals;
        }
    }
}
