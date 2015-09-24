/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.LanguageRefsetMemberList;
import org.ihtsdo.otf.refset.jpa.helpers.LanguageRefsetMemberListJpa;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link LanguageRefsetMemberList}.
 */
public class ListUnit007Test extends AbstractListUnit<LanguageRefsetMember> {

  /** The list1 test fixture . */
  private LanguageRefsetMemberList list1;

  /** The list2 test fixture . */
  private LanguageRefsetMemberList list2;

  /** The test fixture o1. */
  private LanguageRefsetMember o1;

  /** The test fixture o2. */
  private LanguageRefsetMember o2;

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
    list1 = new LanguageRefsetMemberListJpa();
    list2 = new LanguageRefsetMemberListJpa();
    o1 = new LanguageRefsetMemberJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    o1.setDescriptionId("1");

    o2 = new LanguageRefsetMemberJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    o2.setDescriptionId("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse007() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse007() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases007() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization007() throws Exception {
    testXmllSerialization(list1, list2, o1, o2);
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
