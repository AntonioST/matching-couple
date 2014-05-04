/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiPredicate;

/**
 *
 * @author antonio
 */
public class Main{

    public static final String VERSION = "2.0";

    //
    public static void main(String[] args){
        //input rule output
        if (args.length != 3) {
            System.out.println("input rule output");
            System.exit(1);
        }
        String inputFileName = args[0];
        String ruleFileName = args[1];
        String onputFileName = args[2];

    }

    private static List<Person> loadPeople(BufferedReader reader) throws IOException{
        List<String> category = new ArrayList<>();
        List<Person> people = new ArrayList<>();
        List<String> buf = new ArrayList<>();
        int size;
        String line;
        line = reader.readLine();
        //head
        splitCSVLine(buf, line);
        size = buf.size();
        for (int i = 1; i + 1 < size; i += 2) {
            String c = buf.get(i);
            if (!c.equals(buf.get(i + 1))) {
                throw new RuntimeException("head format wrong");
            }
            new Option(c, loadOptionList(c), loadOptionRule(c));
            category.add(c);
        }
        while ((line = reader.readLine()) != null) {
            buf.clear();
            if (splitCSVLine(buf, line).size() != size) {
                throw new RuntimeException("illegal format : row size not match head");
            }
            Person p = new Person(buf.get(0));
            for (int i = 0, len = category.size(); i < len; i++) {
                String c = category.get(i);
                p.self.put(c, new HashSet<>(Arrays.asList(buf.get(2 * i + 1).split(","))));
                p.target.put(c, new HashSet<>(Arrays.asList(buf.get(2 * i + 2).split(","))));
            }
            people.add(p);
        }
        return people;
    }

    static List<String> splitCSVLine(List<String> buf, String line){
        int start = 0;
        int index;
        for (; start >= 0;) {
            index = skipWhileSpace(line, start);
            if (index == -1) {
                buf.add(line.substring(start).trim());
                break;
            }
            start = index;
            if (line.charAt(start) == '"' || line.charAt(start) == '\'') {
                index = line.indexOf(line.charAt(start), start + 1);
                if (index == -1) {
                    throw new RuntimeException("illegal format : lost " + line.charAt(start));
                }
                buf.add(line.substring(start + 1, index));
                int next = line.indexOf(",", index);
                if (skipWhileSpace(line, index + 1) != next) {
                    throw new RuntimeException("illegal format : text out of " + line.charAt(start));
                }
                if (next == -1) {
                    break;
                }
                start = next + 1;
            } else if (line.charAt(start) == ',') {
                buf.add("");
                start++;
            } else {
                index = line.indexOf(',', start);
                if (index == -1) {
                    buf.add(line.substring(start).trim());
                    start = -1;
                } else {
                    buf.add(line.substring(start, index).trim());
                    start = index + 1;
                }
            }
        }
        return buf;
    }

    static int skipWhileSpace(String line, int start){
        int len = line.length();
        for (; start < len && Character.isWhitespace(line.charAt(start)); start++);
        return start < len ? start : -1;
    }

    static String[] loadOptionList(String category) throws IOException{
        Path p = Paths.get(category + ".list");
        if (!Files.exists(p)) {
            throw new FileNotFoundException(category + ".list");
        }
        return Files.readAllLines(p, Charset.defaultCharset()).stream().toArray(String[]::new);
    }

    static BiPredicate<String, String> loadOptionRule(String category) throws IOException{
        Path p = Paths.get(category + ".rule");
        if (!Files.exists(p)) {
            throw new FileNotFoundException(category + ".rule");
        }
        return new RuleLoader().load(p);
    }
}
