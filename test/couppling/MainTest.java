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
    public void testSplitCSVLine(){
        List<String> buf = new ArrayList<>();
        Main.splitCSVLine(buf, "1;2;3");
        assertEquals(3, buf.size());
        assertEquals("1", buf.get(0));
        assertEquals("2", buf.get(1));
        assertEquals("3", buf.get(2));
        buf.clear();
        Main.splitCSVLine(buf, "1 ; 2 ; 3");
        assertEquals(3, buf.size());
        assertEquals("1", buf.get(0));
        assertEquals("2", buf.get(1));
        assertEquals("3", buf.get(2));
        buf.clear();
    }
}
