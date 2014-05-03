/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import edu.ast.dev.FileTable;
import java.io.*;
import java.util.LinkedList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author antonio
 */
public class Main{

    public static final String VERSION = "1.0";
    //
    private static final String PROPERITY_FILE = "properity.list";
    private static final String PROPERITY_REF = "\\$([\\w.]+)";
    private static final Pattern PROPERITY_PATTERN = Pattern.compile(PROPERITY_REF);
    public static final Properties properity;

    static{
        Properties p = new Properties();
        try{
            try{
                p.load(new FileInputStream(PROPERITY_FILE));
            } catch (FileNotFoundException ex){
                ex.printStackTrace();
                if (copyFileFromSys("", PROPERITY_FILE, "")) {
                    p.load(new FileInputStream(PROPERITY_FILE));
                }
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
        properity = p;
        FileTable.setAutoClose(false);
    }
    public static final PrintStream log = getStreamOrNull("log.file");
    private static final String PRINT_STREAM_CONTROL = ".control";

    public static void main(String[] args){
        try{
            System.setOut(getStream("log.stdout", "stdout"));
        } catch (IOException ex){
        }
        try{
            System.setErr(getStream("log.stderr", "stderr"));
        } catch (IOException ex){
        }
        log.println("[Main] start");
        String[] inp = loadList("file.person");
        if (inp.length == 0) {
            log.println("[Main] no input");
            System.out.println("no input");
            return;
        }
        log.println("[Main] create Person");
        Person[] ps = Coupler.inputPerson(inp);
        log.println("[Main] create Coupler");
        Coupler cp = new Coupler(ps);
        try{
            log.println("[Main] output result");
            cp.outputResultAsCsv();
        } catch (IOException ex){
            ex.printStackTrace();
        }
        log.println("[Main] end");
    }

    private static String getValue(String key, String def){
        String v = properity.getProperty(key, def);
        if (v != null && v.startsWith("$")) {
            Matcher m = PROPERITY_PATTERN.matcher(v);
            if (m.find()) {
                return getValue(m.group(), def);
            }
        }
        return v;
    }

    public static int[] loadIntArray(String key){
        log.printf("[Main] load int array : %s\n", key);
        String[] ss = loadList(key);
        int[] is = new int[ss.length];
        for (int i = ss.length - 1; i >= 0; i--) {
            is[i] = Integer.parseInt(ss[i]);
        }
        return is;
    }

    public static String[] loadList(String key){
        log.printf("[Main] load list : %s\n", key);
        String value = getValue(key, "");
        if (value.isEmpty()) {
        } else if (value.startsWith("[")) {
            return getLinesFromString(value);
        } else {
            try (BufferedReader r = getReaderFromPath(value)){
                return getLinesFromFile(r);
            } catch (FileNotFoundException ex){
                ex.printStackTrace();
                log.printf("[Main] load list : %s fail, file not found\n", key);
            } catch (IOException ex){
                ex.printStackTrace();
                log.printf("[Main] load list : %s fail, ioex\n", key);
            }
        }
        return new String[0];
    }

    public static <T extends Option> MatchRule loadRule(Class<T> cls, String key){
        log.printf("[Main] load rule : %s\n", key);
        String path = getValue(key, "");
        if (!path.isEmpty()) {
            try (BufferedReader r = getReaderFromPath(path)){
                return RuleMaker.make(cls, getLinesFromFile(r));
            } catch (FileNotFoundException ex){
                ex.printStackTrace();
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    private static boolean checkControl(String key){
        String controlkey;
        int i = key.indexOf('.');
        if (i == -1) {
            controlkey = key.concat(PRINT_STREAM_CONTROL);
        } else {
            controlkey = key.substring(0, i).concat(PRINT_STREAM_CONTROL);
        }
        String v = getValue(controlkey, "true");
        if (v.toLowerCase().equals("true")) {
            return true;
        }
        return false;
    }

    public static PrintStream getStream(String key, String def) throws IOException{
        if (log != null) {
            log.printf("[Main] open stream : %s\n", key);
        }
        if (key == null || !checkControl(key)) {
            if (log != null) {
                log.printf("[Main] null stream : %s\n", key);
            }
            return new PrintStream(new NullStream());
        } else if (key.startsWith("file::")) {
            return createOutputPrintStream(key.substring(7));
        }else if("stdout".equals(key)){
            return System.out;
        }else if("stderr".equals(key)){
            return System.err;
        }
        String p = getValue(key, def);
        if (p.equals("null")) {
            if (log != null) {
                log.printf("[Main] null stream : %s\n", key);
            }
            return new PrintStream(new NullStream());
        } else {
            return createOutputPrintStream(p);
        }
    }

    public static PrintStream getStreamOrNull(String key){
        try{
            return getStream(key, "null");
        } catch (IOException ex){
            return new PrintStream(new NullStream());
        }
    }

    private static String[] getLinesFromFile(BufferedReader r) throws IOException{
        LinkedList<String> pool = new LinkedList<>();
        String line;
        while ((line = r.readLine()) != null) {
            line = line.replaceAll("#.*$", "");
            if (!line.isEmpty()) {
                pool.add(line);
            }
        }
        return pool.toArray(new String[pool.size()]);
    }

    private static String[] getLinesFromString(String line){
        int start = line.indexOf('[');
        if (start == -1) {
            return new String[0];
        }
        int end = line.indexOf(']', start);
        if (end == -1) {
            return new String[0];
        }
        LinkedList<String> pool = new LinkedList<>();
        int pre = start + 1;
        int index;
        for (;;) {
            index = line.indexOf(',', pre);
            if (index == -1 || index >= end) {
                break;
            }
            pool.add(line.substring(pre, index));
            pre = index + 1;
        }
        return pool.toArray(new String[pool.size()]);
    }

    private static BufferedReader getReaderFromPath(String path) throws FileNotFoundException{
        return new BufferedReader(new FileReader(path));
    }

    private static PrintStream createOutputPrintStream(String path) throws IOException{
        return new PrintStream(createOutputFile(path));
    }

    private static OutputStream createOutputFile(String path) throws IOException{
        return FileTable.appendOutputStream(path);
    }

    private static boolean copyFileFromSys(String frompath, String filename, String toPath){
        String fsp = System.getProperty("file.separator");
        String path = frompath + fsp + filename;
        String target = toPath + fsp + filename;
        try (InputStream is = ClassLoader.getSystemResourceAsStream(path);
            OutputStream os = createOutputFile(target)){
            int r;
            byte[] buffer = new byte[1 << 10];
            while ((r = is.read(buffer)) != -1) {
                os.write(buffer, 0, r);
            }
        } catch (IOException ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private static class NullStream extends OutputStream{

        @Override
        public void write(int b) throws IOException{
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException{
        }

        @Override
        public void write(byte[] b) throws IOException{
        }
    }
}
