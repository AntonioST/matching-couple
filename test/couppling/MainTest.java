/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package couppling;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 *
 * @author antonio
 */
public class MainTest{

    @Test
    public void testSkipWhileSpace(){
        assertEquals(-1, Main.skipWhileSpace("   ", 0));
        assertEquals(3, Main.skipWhileSpace("   1", 0));
        assertEquals(4, Main.skipWhileSpace("1   1", 1));
        assertEquals(0, Main.skipWhileSpace("1   1", 0));
    }

    @Test
    public void testSplitCSVLine(){
        List<String> buf = new ArrayList<>();
        Main.splitCSVLine(buf, ",,");
        assertEquals(3, buf.size());
        assertEquals("", buf.get(0));
        assertEquals("", buf.get(1));
        assertEquals("", buf.get(2));
        buf.clear();
        Main.splitCSVLine(buf, "1,2,3");
        assertEquals(3, buf.size());
        assertEquals("1", buf.get(0));
        assertEquals("2", buf.get(1));
        assertEquals("3", buf.get(2));
        buf.clear();
        Main.splitCSVLine(buf, "1 , 2 , 3");
        assertEquals(3, buf.size());
        assertEquals("1", buf.get(0));
        assertEquals("2", buf.get(1));
        assertEquals("3", buf.get(2));
        buf.clear();
        Main.splitCSVLine(buf, "1,'2 4',\" 3_5 \"");
        assertEquals(3, buf.size());
        assertEquals("1", buf.get(0));
        assertEquals("2 4", buf.get(1));
        assertEquals(" 3_5 ", buf.get(2));
    }

    @Test(expected = RuntimeException.class)
    public void testSplitLost(){
        Main.splitCSVLine(new ArrayList<>(), ",'");
    }

    @Test(expected = RuntimeException.class)
    public void testSplitError(){
        Main.splitCSVLine(new ArrayList<>(), ",' test' more");
    }

}
