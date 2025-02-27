/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link KeyValuesMap}.
 */
public class HelperUnit011Test {

  /** The helper object to test. */
  private KeyValuesMap object;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   */
  @Before
  public void setup() {
    object = new KeyValuesMap();
  }

  /**
   * Test normal use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperNormalUse011() throws Exception {
    Logger.getLogger(getClass()).info("TEST testHelperNormalUse011");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();

    // Test equals
    KeyValuesMap map1 = new KeyValuesMap();
    map1.add("1", "a");
    assertTrue(map1.getMap().get("1").getObjects().size() == 1);
    assertTrue(map1.getMap().get("1").contains("a"));
    map1.add("1", "b");
    assertTrue(map1.getMap().get("1").getObjects().size() == 2);
    assertTrue(map1.getMap().get("1").contains("a"));
    assertTrue(map1.getMap().get("1").contains("b"));
    KeyValuesMap map2 = new KeyValuesMap();
    map2.add("1", "a");
    map2.add("1", "b");
    assertTrue(map1.equals(map2));
    assertTrue(map1.hashCode() == map2.hashCode());
    assertTrue(map1.toString().equals(map2.toString()));

    KeyValuesMap map3 = new KeyValuesMap(map1);
    assertTrue(map1.equals(map3));
    assertTrue(map1.hashCode() == map3.hashCode());
    assertTrue(map1.toString().equals(map3.toString()));

    // Test serialization
    assertEquals(map1, ConfigUtility.getGraphForString(
        ConfigUtility.getStringForGraph(map1), KeyValuesMap.class));
    assertEquals(map1, ConfigUtility.getGraphForJson(
        ConfigUtility.getJsonForGraph(map1), KeyValuesMap.class));

  }

  /**
   * Test degenerate use of the helper object.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings({
    "static-method"
  })
  @Test
  public void testHelperDegenerateUse011() throws Exception {
    try {
      KeyValuesMap map = new KeyValuesMap(null);
      fail("Expected exception did not occur. " + map);
    } catch (Exception e) {
      // do nothing, this is expected
    }
  }

  /**
   * Test edge cases of the helper object.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Test
  public void testHelperEdgeCases011() throws Exception {
    KeyValuesMap map1 = new KeyValuesMap();
    map1.add("1", (String) null);
    map1.add("1", (String) null);
    KeyValuesMap map2 = new KeyValuesMap();
    map2.add("1", (String) null);
    map2.add("1", (String) null);
    assertTrue(map1.equals(map2));
    assertTrue(map1.hashCode() == map2.hashCode());
    try {
      assertTrue(map1.toString().equals(map2.toString()));
      fail("Expected NPE");
    } catch (Exception e) {
      // n/a
    }
    map1 = new KeyValuesMap();
    map1.add(null, "1");
    map1.add(null, "2");
    map2 = new KeyValuesMap();
    map2.add(null, "1");
    map2.add(null, "2");
    assertTrue(map1.equals(map2));
    assertTrue(map1.hashCode() == map2.hashCode());
    assertTrue(map1.toString().equals(map2.toString()));

  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
    // do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
