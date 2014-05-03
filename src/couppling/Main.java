/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package couppling;

import java.io.*;
import java.util.LinkedList;

/**
 *
 * @author antonio
 */
public class Main{

    public static final String VERSION = "2.0";

    //
    public static void main(String[] args){
        try{
            System.setOut(getStream("log.stdout", "stdout"));
        } catch (IOException ex){
        }
        try{
            System.setErr(getStream("log.stderr", "stderr"));
        } catch (IOException ex){
        }
        String[] inp = loadList("file.person");
        if (inp.length == 0) {
            System.out.println("no input");
            return;
        }
        Person[] ps = Coupler.inputPerson(inp);
        Coupler cp = new Coupler(ps);
        try{
            cp.outputResultAsCsv();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static int[] loadIntArray(String key){
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
