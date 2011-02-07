/**
 * **** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2011 Yoko Harada <yokolet@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 * **** END LICENSE BLOCK *****
 */
package org.jruby.embed.internal;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.StreamHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Handler;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.logging.Logger;
import org.jruby.RubyClass;
import java.util.Arrays;
import org.jruby.embed.LocalVariableBehavior;
import java.util.ArrayList;
import org.jruby.embed.ScriptingContainer;
import org.jruby.embed.LocalContextScope;
import java.util.List;
import org.jruby.embed.variable.BiVariable;
import org.jruby.embed.variable.VariableInterceptor;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author yoko
 */
public class BiVariableMapTest {
    private String basedir = System.getProperty("user.dir");

    static Logger logger0 = Logger.getLogger(BiVariableMapTest.class.getName());
    static Logger logger1 = Logger.getLogger(BiVariableMapTest.class.getName());
    static OutputStream outStream = null;
    FileWriter writer = null;

    public BiVariableMapTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        outStream.close();
    }

    @Before
    public void setUp() throws FileNotFoundException, IOException {
        outStream = new FileOutputStream(basedir + "/build/test-results/run-junit-embed.log", true);
        Handler handler = new StreamHandler(outStream, new SimpleFormatter());
        logger0.addHandler(handler);
        logger0.setUseParentHandlers(false);
        logger0.setLevel(Level.INFO);
        logger1.setUseParentHandlers(false);
        logger1.addHandler(new ConsoleHandler());
        logger1.setLevel(Level.WARNING);

        writer = new FileWriter(basedir + "/build/test-results/run-junit-embed.txt", true);
    }

    @After
    public void tearDown() throws IOException {
        writer.close();
    }

    /**
     * Test of getNames method, of class BiVariableMap.
     */
    @Test
    public void testGetNames() {
        logger1.info("getNames");
        ScriptingContainer container =
                new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        BiVariableMap instance = container.getVarMap();
        List<String> expResult = new ArrayList<String>();
        List<String> result = instance.getNames();
        assertEquals(expResult, result);
        container.put("ARGV", new String[] {"spring", "fall"});
        container.put("SEASON", new String[] {"summer", "winter"});
        container.put("$sports", new String[] {"baseball", "hiking", "soccer", "ski"});
        container.put("@weather", new String[] {"snow", "sleet", "drizzle", "rain"});
        container.put("trees", new String[] {"cypress", "hemlock", "spruce"});
        expResult = Arrays.asList("ARGV", "SEASON", "$sports", "@weather", "trees");
        result = instance.getNames();
        assertEquals(expResult, result);
        assertTrue(result.size() == 5);
        
        // transient local variable should vanish after eval
        container.runScriptlet("a = 1");
        expResult = Arrays.asList("ARGV", "SEASON", "$sports", "@weather");
        result = instance.getNames();
        assertEquals(expResult, result);

        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL);
        instance = container.getVarMap();
        container.put("ARGV", new String[] {"spring", "fall"});
        container.put("SEASON", new String[] {"summer", "winter"});
        container.put("$sports", new String[] {"baseball", "hiking", "soccer", "ski"});
        container.put("@weather", new String[] {"snow", "sleet", "drizzle", "rain"});
        container.put("trees", new String[] {"cypress", "hemlock", "spruce"});
        // "$sports" and "@waether" are not eligible keys for localglobal type. Those are cut out.
        expResult = Arrays.asList("ARGV", "SEASON", "trees");
        result = instance.getNames();
        assertEquals(expResult, result);
        assertTrue(result.size() == 3);
    }

    /**
     * Test of getVariables method, of class BiVariableMap.
     */
    @Test
    public void testGetVariables() {
        logger1.info("getVariables");
        ScriptingContainer container =
                new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        BiVariableMap instance = container.getVarMap();
        List<BiVariable> result = instance.getVariables();
        container.put("ARGV", new String[] {"spring", "fall"});
        container.put("SEASON", new String[] {"summer", "winter"});
        container.put("$sports", new String[] {"baseball", "hiking", "soccer", "ski"});
        container.put("@weather", new String[] {"snow", "sleet", "drizzle", "rain"});
        container.put("trees", new String[] {"cypress", "hemlock", "spruce"});
        String[][] extResult = {{"spring", "fall"},
                                {"summer", "winter"},
                                {"baseball", "hiking", "soccer", "ski"},
                                {"snow", "sleet", "drizzle", "rain"},
                                {"cypress", "hemlock", "spruce"}};
        result = instance.getVariables();
        for (int i=0; i<result.size(); i++) {
            BiVariable var = result.get(i);
            assertArrayEquals(extResult[i], (String[]) var.getJavaObject());
        }
        assertTrue(result.size() == 5);

        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL);
        instance = container.getVarMap();
        container.put("ARGV", new String[] {"spring", "fall"});
        container.put("SEASON", new String[] {"summer", "winter"});
        container.put("$sports", new String[] {"baseball", "hiking", "soccer", "ski"});
        container.put("@weather", new String[] {"snow", "sleet", "drizzle", "rain"});
        container.put("trees", new String[] {"cypress", "hemlock", "spruce"});
        String[][] extResult2 = {{"spring", "fall"},
                                 {"summer", "winter"},
                                 {"cypress", "hemlock", "spruce"}};
        result = instance.getVariables();
        for (int i=0; i<result.size(); i++) {
            BiVariable var = result.get(i);
            assertArrayEquals(extResult2[i], (String[]) var.getJavaObject());
        }      
        assertTrue(result.size() == 3);
    }

    /**
     * Test of getVariableInterceptor method, of class BiVariableMap.
     */
    @Test
    public void testGetVariableInterceptor() {
        logger1.info("getVariableInterceptor");
        ScriptingContainer container =
                new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        BiVariableMap instance = container.getVarMap();
        VariableInterceptor result = instance.getVariableInterceptor();
        assertNotNull(result);
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.BSF);
        instance = container.getVarMap();
        result = instance.getVariableInterceptor();
        assertNotNull(result);
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL);
        instance = container.getVarMap();
        result = instance.getVariableInterceptor();
        assertNotNull(result);
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
        instance = container.getVarMap();
        result = instance.getVariableInterceptor();
        assertNotNull(result);
    }

    /**
     * Test of size method, of class BiVariableMap.
     */
    @Test
    public void testSize() {
        logger1.info("size");
        ScriptingContainer container =
                new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        BiVariableMap instance = container.getVarMap();
        container.put("ARGV", new String[] {"spring", "fall"});
        container.put("SEASON", new String[] {"summer", "winter"});
        container.put("$sports", new String[] {"baseball", "hiking", "soccer", "ski"});
        container.put("@weather", new String[] {"snow", "sleet", "drizzle", "rain"});
        container.put("trees", new String[] {"cypress", "hemlock", "spruce"});
        assertTrue(instance.size() == 5);
        
        String[] expResult = {"snow", "sleet", "drizzle", "rain"};
        String[] weather = (String[]) container.remove("@weather");
        assertArrayEquals(expResult, weather);
        assertTrue(instance.size() == 4);
        
        // transient local variable should vanish after eval
        container.runScriptlet("a = 1");
        assertTrue(instance.size() == 3);
        
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
        instance = container.getVarMap();
        container.put("ARGV", new String[] {"spring", "fall"});
        container.put("SEASON", new String[] {"summer", "winter"});
        container.put("$sports", new String[] {"baseball", "hiking", "soccer", "ski"});
        container.put("@weather", new String[] {"snow", "sleet", "drizzle", "rain"});
        container.put("trees", new String[] {"cypress", "hemlock", "spruce"});
        assertTrue(instance.size() == 5);
        
        // persistent local variable should be kept even after eval, plus retrieved.
        container.runScriptlet("a = 1");
        assertTrue(instance.size() == 6);
        
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL);
        instance = container.getVarMap();
        container.put("ARGV", new String[] {"spring", "fall"});
        container.put("SEASON", new String[] {"summer", "winter"});
        container.put("$sports", new String[] {"baseball", "hiking", "soccer", "ski"});
        container.put("@weather", new String[] {"snow", "sleet", "drizzle", "rain"});
        container.put("trees", new String[] {"cypress", "hemlock", "spruce"});
        // $sports and @weather are not eligible key for local global type.
        assertTrue(instance.size() == 3);
        
        // local global variable should not be dropped.
        // "a" in Ruby code is not local global var.
        container.runScriptlet("a = 1");
        assertTrue(instance.size() == 3);
        
    }

    /**
     * Test of isEmpty method, of class BiVariableMap.
     */
    @Test
    public void testIsEmpty() {
        logger1.info("isEmpty");
        ScriptingContainer container =
                new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        BiVariableMap instance = container.getVarMap();
        container.put("ARGV", new String[] {"spring", "fall"});
        container.put("SEASON", new String[] {"summer", "winter"});
        container.put("$sports", new String[] {"baseball", "hiking", "soccer", "ski"});
        container.put("@weather", new String[] {"snow", "sleet", "drizzle", "rain"});
        container.put("trees", new String[] {"cypress", "hemlock", "spruce"});
        assertFalse(instance.isEmpty());
        container.clear();
        assertTrue(instance.isEmpty());
        
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL);
        instance = container.getVarMap();
        container.put("ARGV", new String[] { "spring", "fall" });
        container.put("SEASON", new String[] { "summer", "winter" });
        container.put("trees", new String[] { "cypress", "hemlock", "spruce" });
        assertFalse(instance.isEmpty());
        container.clear();
        assertTrue(instance.isEmpty());
    }

    /**
     * Test of containsKey method, of class BiVariableMap.
     */
    @Test
    public void testContainsKey() {
        logger1.info("containsKey");
        ScriptingContainer container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        BiVariableMap instance = container.getVarMap();
        container.put("ARGV", new String[] { "spring", "fall" });
        container.put("SEASON", new String[] { "summer", "winter" });
        container.put("$sports", new String[] { "baseball", "hiking", "soccer", "ski" });
        container.put("@weather", new String[] { "snow", "sleet", "drizzle", "rain" });
        container.put("trees", new String[] { "cypress", "hemlock", "spruce" });
        assertTrue(instance.containsKey("ARGV"));
        assertTrue(instance.containsKey("SEASON"));
        assertTrue(instance.containsKey("$sports"));
        assertTrue(instance.containsKey("@weather"));
        assertTrue(instance.containsKey("trees"));

        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL);
        instance = container.getVarMap();
        container.put("ARGV", new String[] { "spring", "fall" });
        container.put("SEASON", new String[] { "summer", "winter" });
        container.put("$sports", new String[] { "baseball", "hiking", "soccer", "ski" });
        container.put("@weather", new String[] { "snow", "sleet", "drizzle", "rain" });
        container.put("trees", new String[] { "cypress", "hemlock", "spruce" });
        assertTrue(instance.containsKey("ARGV"));
        assertTrue(instance.containsKey("SEASON"));
        assertFalse(instance.containsKey("$sports"));
        assertFalse(instance.containsKey("@weather"));
        assertTrue(instance.containsKey("trees"));
        
        // eager retieval mode test
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL, false);
        instance = container.getVarMap();
        assertTrue(instance.isEmpty());
        container.runScriptlet("$SEASON = ['mid-winter', 'late-summer']; ARGV << \"St. Patrick's day\"");
        assertTrue(instance.containsKey("SEASON"));
        assertTrue(instance.containsKey("ARGV"));
        assertFalse(instance.containsKey("trees"));
        assertEquals(2, instance.size());
        
        // lazy retieval mode test
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL, true);
        instance = container.getVarMap();
        assertTrue(instance.isEmpty());
        container.runScriptlet("$SEASON = ['mid-winter', 'late-summer']; ARGV << \"St. Patrick's day\"");
        assertFalse(instance.containsKey("SEASON"));
        List<String> expResult1 = Arrays.asList("mid-winter", "late-summer");
        List<String> result1 = (List<String>) container.get("SEASON");
        assertEquals(expResult1, result1);
        assertTrue(instance.containsKey("SEASON"));
        
        assertFalse(instance.containsKey("ARGV"));
        List<String> expResult2 = new ArrayList<String>();
        expResult2.add("St. Patrick's day");
        List<String> result2 = (List<String>) container.get("ARGV");
        assertEquals(expResult2, result2);
        assertTrue(instance.containsKey("ARGV"));
    }

    /**
     * Test of containsValue method, of class BiVariableMap.
     */
    @Test
    public void testContainsValue() {
        logger1.info("containsValue");
        ScriptingContainer container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        BiVariableMap instance = container.getVarMap();
        ArrayList<String> argv_values = new ArrayList<String>();
        argv_values.add("spring"); argv_values.add("fall");
        container.put("ARGV", argv_values);
        assertTrue(instance.containsValue(argv_values));
        
        ArrayList<String> const_values = new ArrayList<String>();
        const_values.add("summer"); const_values.add("winter");
        container.put("SEASON", const_values);
        assertTrue(instance.containsValue(argv_values));
        
        ArrayList<String> gvar_values = new ArrayList<String>();
        gvar_values.add("baseball"); gvar_values.add("soccer"); gvar_values.add("ski");
        container.put("$sports", gvar_values);
        assertTrue(instance.containsValue(gvar_values));
        
        ArrayList<String> ivar_values = new ArrayList<String>();
        ivar_values.add("snow"); ivar_values.add("sleet"); ivar_values.add("drizzle");
        container.put("@weather", ivar_values);
        assertTrue(instance.containsValue(ivar_values));
        
        ArrayList<String> lvar_values = new ArrayList<String>();
        lvar_values.add("cypress"); lvar_values.add("hemlock"); lvar_values.add("spruce");
        container.put("trees", lvar_values);
        assertTrue(instance.containsValue(lvar_values));
        
        container.runScriptlet("ARGV << \"late-fall\"; SEASON << \"mid-summer\"; $sports << \"basketball\"; @weather << \"freezing-rain\"");
        argv_values.add("late-fall");
        const_values.add("mid-summer");
        gvar_values.add("basketball");
        ivar_values.add("freezing-rain");
        
        // transient type lvar should vanish after eval.
        assertFalse(instance.containsValue(lvar_values));
        // lazy retrieval mode. needs to get before containsValue method
        assertEquals(argv_values, container.get("ARGV"));
        assertEquals(const_values, container.get("SEASON"));
        assertEquals(gvar_values, container.get("$sports"));
        assertEquals(ivar_values, container.get("@weather"));
        assertNull(container.get("trees"));
        
        assertTrue(instance.containsValue(argv_values));
        assertTrue(instance.containsValue(const_values));
        assertTrue(instance.containsValue(gvar_values));
        assertTrue(instance.containsValue(ivar_values));
        assertFalse(instance.containsValue(lvar_values));
        
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL, false);
        instance = container.getVarMap();
        container.put("ARGV", argv_values);
        assertTrue(instance.containsValue(argv_values));
        container.put("SEASON", const_values);
        assertTrue(instance.containsValue(argv_values));
        container.put("$sports", gvar_values);
        assertFalse(instance.containsValue(gvar_values));
        container.put("@weather", ivar_values);
        assertFalse(instance.containsValue(ivar_values));
        container.put("trees", lvar_values);
        assertTrue(instance.containsValue(lvar_values));
 
        container.runScriptlet("ARGV << \"early-winter\"; $SEASON << \"deep-fall\"; $trees << \"pine\"");
        argv_values.add("early-winter");
        const_values.add("deep-fall");
        lvar_values.add("pine");
        
        // eager retrival mode. no need to get before containesValue method
        assertTrue(instance.containsValue(argv_values));
        assertTrue(instance.containsValue(const_values));
        assertTrue(instance.containsValue(lvar_values));
        
        assertEquals(argv_values, container.get("ARGV"));
        assertEquals(const_values, container.get("SEASON"));
        assertEquals(lvar_values, container.get("trees"));
    }

    /**
     * Test of get method, of class BiVariableMap.
     */
    @Test
    public void testGet_Object() {
        logger1.info("get");
        ScriptingContainer container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        BiVariableMap instance = container.getVarMap();
        
        ArrayList<String> argv_values = new ArrayList<String>();
        argv_values.add("spring"); argv_values.add("fall");
        container.put("ARGV", argv_values);
        ArrayList<String> const_values = new ArrayList<String>();
        const_values.add("summer"); const_values.add("winter");
        container.put("SEASON", const_values);
        ArrayList<String> gvar_values = new ArrayList<String>();
        gvar_values.add("baseball"); gvar_values.add("soccer"); gvar_values.add("ski");
        container.put("$sports", gvar_values);
        ArrayList<String> ivar_values = new ArrayList<String>();
        ivar_values.add("snow"); ivar_values.add("sleet"); ivar_values.add("drizzle");
        container.put("@weather", ivar_values);
        ArrayList<String> cvar_values = new ArrayList<String>();
        cvar_values.add("cirrus"); cvar_values.add("stratus"); cvar_values.add("cumulus");
        container.put("@@clouds", cvar_values);
        ArrayList<String> lvar_values = new ArrayList<String>();
        lvar_values.add("cypress"); lvar_values.add("hemlock"); lvar_values.add("spruce");
        container.put("trees", lvar_values);
        assertEquals(argv_values, instance.get("ARGV"));
        assertEquals(const_values, instance.get("SEASON"));
        assertEquals(gvar_values, instance.get("$sports"));
        assertEquals(ivar_values, instance.get("@weather"));
        assertEquals(cvar_values, instance.get("@@clouds"));
        assertEquals(lvar_values, instance.get("trees"));
        
        String script =
            "class Forecast\n" +
            "  SEASON = 'halloween'\n" +
            "  @@clouds = ['contrail', 'snow laden clouds']\n" +
            "  attr_accessor :weather, :temp\n" +
            "  def initialize(weather, temp)\n" +
            "    @weather, @temp = weather, temp\n" +
            "  end\n" +
            "  def temp\n" +
            "    @temp\n" +
            "  end\n" +
            "  def cloud_names\n" +
            "    @@clouds\n" +
            "  end\n" +
            "end";
        container.runScriptlet(script);
     
        Object klazz = container.get("Forecast");
        assertEquals("Forecast", ((RubyClass)klazz).getName());
        Object receiver = container.callMethod(klazz, "new", "blizzard", "6F");
        assertEquals("blizzard", container.get(receiver, "@weather"));
        assertEquals("6F", container.callMethod(receiver, "temp"));
        assertEquals("halloween", container.get(receiver, "SEASON"));
        ArrayList<String> expResult = new ArrayList<String>();
        expResult.add("contrail"); expResult.add("snow laden clouds");
        //assertEquals(expResult, container.get(receiver, "@@clouds")); // why this fails?
        container.put(receiver, "@temp", "-5F");
        assertEquals("-5F", container.callMethod(receiver, "temp"));
        container.put(receiver, "SEASON", "colored leaves");
        container.put(receiver, "@@clouds", "thunder clouds");
        // need runScriptlet/callMethod to inject a new value
        container.runScriptlet("a=1");
        assertEquals("colored leaves", container.get(receiver, "SEASON"));
        assertEquals("thunder clouds", container.callMethod(receiver, "cloud_names"));
        assertEquals("thunder clouds", container.get(receiver, "@@clouds")); // this passes
        expResult.clear();
        expResult.add("cirrus"); expResult.add("stratus"); expResult.add("cumulus");
        assertEquals(expResult, container.get("@@clouds"));
        // gvar should be receiver insensitive
        expResult.clear();
        expResult.add("baseball"); expResult.add("soccer"); expResult.add("ski");
        assertEquals(expResult, container.get(receiver, "$sports"));
        container.put(receiver, "$team", "tigers");
        assertEquals("tigers", container.get("$team"));
        
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
        instance = container.getVarMap();
        container.put("trees", lvar_values);
        container.runScriptlet(script);
        klazz = container.get("Forecast");
        receiver = container.callMethod(klazz, "new", "blizzard", "6F");
        expResult.clear();
        expResult.add("cypress"); expResult.add("hemlock"); expResult.add("spruce");
        assertEquals(expResult, container.get("trees"));
    }

    /**
     * Test of terminate method, of class BiVariableMap.
     */
    //@Test
    public void testTerminate() {
        logger1.info("terminate");
        /* add this test  later
        BiVariableMap instance = null;
        instance.terminate();
        */
    }

    /**
     * Test of remove method, of class BiVariableMap.
     */
    //@Test
    public void testRemove_Object() {
        logger1.info("remove");
        /* add this test later
        Object key = null;
        BiVariableMap instance = null;
        Object expResult = null;
        Object result = instance.remove(key);
        assertEquals(expResult, result);
        */
    }

    /**
     * Test of remove method, of class BiVariableMap.
     */
    //@Test
    public void testRemove_Object_Object() {
        logger1.info("remove");
        /* add this test later
        Object receiver = null;
        Object key = null;
        BiVariableMap instance = null;
        Object expResult = null;
        Object result = instance.remove(receiver, key);
        assertEquals(expResult, result);
        */
    }


    /**
     * Test of clear method, of class BiVariableMap.
     */
    //@Test
    public void testClear() {
        logger1.info("clear");
        /* add more clear test later
        BiVariableMap instance = null;
        instance.clear();
        */
    }

    /**
     * Test of isLazy method, of class BiVariableMap.
     */
    @Test
    public void testIsLazy() {
        logger1.info("isLazy");
        ScriptingContainer container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        BiVariableMap instance = container.getVarMap();
        assertTrue(instance.isLazy());
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT, false);
        instance = container.getVarMap();
        assertFalse(instance.isLazy());
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
        instance = container.getVarMap();
        assertTrue(instance.isLazy());
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT, false);
        instance = container.getVarMap();
        assertFalse(instance.isLazy());
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL);
        instance = container.getVarMap();
        assertTrue(instance.isLazy());
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.GLOBAL, false);
        instance = container.getVarMap();
        assertFalse(instance.isLazy());
    }

}