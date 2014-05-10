/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import couppling.MatchHandle.Score;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

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
        String ouputFileName = args[2];
        //
        List<Person> people = null;
        MatchHandle c = null;
        try (BufferedReader r = Files.newBufferedReader(Paths.get(inputFileName))){
            System.out.println("load people : " + inputFileName);
            people = loadPeople(r);
        } catch (IOException ex){
            ex.printStackTrace();
            System.exit(1);
        }
        try{
            System.out.println("load rule : " + ruleFileName);
            List<String> ruleline = Files.readAllLines(Paths.get(ruleFileName));
            ruleline.removeIf(ln -> ln.startsWith("#"));
            for (int i = 0, sz = ruleline.size(); i < sz; i++) {
                String line = ruleline.get(i);
                if (line.endsWith("\\")) {
                    ruleline.set(i, line.substring(0, line.length() - 1) + ruleline.get(i + 1));
                    ruleline.remove(i + 1);
                    sz--;
                }
            }
            if (ruleline.size() == 1 && !ruleline.get(0).startsWith("return")) {
                c = new MatchHandle(new MatchRule(ruleline.get(0)));
            } else {
                c = new MatchHandle(new MatchRule(ruleline));
            }
        } catch (IOException ex){
            ex.printStackTrace();
            System.exit(1);
        }
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(ouputFileName),
                                                        StandardOpenOption.CREATE)){
            List<Person> left = new ArrayList<>();
            List<Score> match = c.matchCouple(people, left);
            w.write("couple;" + match.size() + ";score");
            w.newLine();
            for (Score s: match) {
                w.write(s.self.name);
                w.write(";");
                w.write(s.target.name);
                w.write(";");
                w.write(Integer.toString(s.score));
                w.newLine();
            }
            w.write("left;" + left.size());
            w.newLine();
            for (Person p: left) {
                w.write(p.name);
                w.newLine();
            }
            w.newLine();
        } catch (IOException ex){
            ex.printStackTrace();
            System.exit(1);
        }
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
            String n = buf.get(i + 1);
            if (!n.isEmpty() && !c.equals(n)) {
                throw new RuntimeException("head format wrong : " + c + " != " + n);
            }
            new Option(c, loadOptionList(c), loadOptionRule(c));
            category.add(c);
        }
        Map<String, Set<String>> record = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            buf.clear();
            if (splitCSVLine(buf, line).size() != size) {
                throw new RuntimeException("illegal format : row size not match head");
            }
            Person p = new Person(buf.get(0));
            for (int i = 0, len = category.size(); i < len; i++) {
                String c = category.get(i);
                List<String> tmp = Arrays.asList(buf.get(2 * i + 1).split(","));
                record.computeIfAbsent(c, k -> new HashSet<>()).addAll(tmp);
                p.self.put(c, new ArrayList<>(tmp));
                tmp = Arrays.asList(buf.get(2 * i + 2).split(","));
                record.computeIfAbsent(c, k -> new HashSet<>()).addAll(tmp);
                p.target.put(c, new ArrayList<>(tmp));
            }
            people.add(p);
        }
        record.keySet().forEach(c -> {
            Option op = Option.get(c);
            Set<String> set = record.get(c);
            set.removeIf(op::contains);
            if (!set.isEmpty()) {
                System.out.println("?" + c + set);
            }
        });
        return people;
    }

    static List<String> splitCSVLine(List<String> buf, String line){
        Stream.of(line.split(";")).map(String::trim).forEach(buf::add);
        if (line.endsWith(";")) {
            buf.add("");
        }
        return buf;
    }

    static String[] loadOptionList(String category) throws IOException{
        Path p = Paths.get(category + ".list");
        System.out.println("find " + p.toString());
        if (!Files.exists(p)) {
            throw new FileNotFoundException(category + ".list");
        }
        return Files.readAllLines(p, Charset.defaultCharset()).stream().toArray(String[]::new);
    }

    static BiPredicate<String, String> loadOptionRule(String category) throws IOException{
        Path p = Paths.get(category + ".rule");
        System.out.println("find " + p.toString());
        if (!Files.exists(p)) {
            throw new FileNotFoundException(category + ".rule");
        }
        return new RuleLoader().load(p);
    }
}
