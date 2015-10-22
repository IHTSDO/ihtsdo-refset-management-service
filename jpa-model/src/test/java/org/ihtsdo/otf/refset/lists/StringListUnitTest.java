/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.StringList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link StringList}.
 */
public class StringListUnitTest extends AbstractListUnit<String> {

  /** The list test fixture . */
  private StringList list;

  /** The list2 test fixture . */
  private StringList list2;

  /** The test fixture s1. */
  private String s1;

  /** The test fixture s2. */
  private String s2;

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
    list = new StringList();
    list2 = new StringList();
    s1 = "abc";
    s2 = "def";
  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse009() throws Exception {
    testNormalUse(list, list2, s1, s2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse009() throws Exception {
    testDegenerateUse(list, list2, s1, s2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases009() throws Exception {
    testEdgeCases(list, list2, s1, s2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization009() throws Exception {
    testXmllSerialization(list, list2, s1, s2);
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
