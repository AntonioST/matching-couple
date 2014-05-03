/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package couppling;

import cclo.ngraph.util.loader.LineBaseLoader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    @Override
    protected boolean startLoading(){
        head = null;
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
        } else if (lhs.equals("*") && rhs.equals("*")) {
            tmp = (a, b) -> true;
        } else if (rhs.equals("*")) {
            if (lhs.startsWith("[") && lhs.endsWith("]")) {
                List<String> list = Arrays.asList(lhs.substring(1, lhs.length() - 1).split(" *, *"));
                tmp = (a, b) -> list.contains(a) || list.contains(b);
            } else {
                tmp = (a, b) -> lhs.equals(a) || lhs.equals(b);
            }
        } else {
            List<String> lset;
            List<String> rset;
            if (lhs.startsWith("[") && lhs.endsWith("]")) {
                lset = Arrays.asList(lhs.substring(1, lhs.length() - 1).split(" *, *"));
            } else {
                lset = Collections.singletonList(lhs);
            }
            if (rhs.startsWith("[") && rhs.endsWith("]")) {
                rset = Arrays.asList(rhs.substring(1, rhs.length() - 1).split(" *, *"));
            } else {
                rset = Collections.singletonList(rhs);
            }
            if (m.group(2).equals("==")) {
                tmp = (a, b) -> (lset.contains(a) && rset.contains(b))
                  || (lset.contains(b) && rset.contains(a));
            } else {
                tmp = (a, b) -> (lset.contains(a) && rset.contains(b));
            }
        }
        if (head == null) {
            head = tmp;
        } else {
            head = head.or(tmp);
        }
    }
}
