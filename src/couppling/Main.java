/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import couppling.Coupler.Score;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
        Coupler c = null;
        try (BufferedReader r = Files.newBufferedReader(Paths.get(inputFileName))){
            people = loadPeople(r);
        } catch (IOException ex){
            ex.printStackTrace();
            System.exit(1);
        }
        try{
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
                c = new Coupler(new MatchRule(ruleline.get(0)));
            } else {
                c = new Coupler(new MatchRule(ruleline));
            }
        } catch (IOException ex){
            ex.printStackTrace();
            System.exit(1);
        }
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(ouputFileName),
                                                        StandardOpenOption.CREATE)){
            for (Score s: c.matchCouple(people)) {
                w.write(s.self.name);
                w.write(";");
                w.write(s.target.name);
                w.write(";");
                w.write(Integer.toString(s.score));
                w.newLine();
            }
            for (Person p: people) {
                w.write(p.name);
                w.write(";");
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
        Stream.of(line.split(";")).map(String::trim).forEach(buf::add);
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
