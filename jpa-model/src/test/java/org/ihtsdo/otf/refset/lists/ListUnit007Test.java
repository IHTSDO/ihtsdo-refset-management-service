/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.LanguageRefSetMemberList;
import org.ihtsdo.otf.refset.jpa.helpers.LanguageRefSetMemberListJpa;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefSetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link LanguageRefSetMemberList}.
 */
public class ListUnit007Test extends AbstractListUnit<LanguageRefSetMember> {

  /** The list1 test fixture . */
  private LanguageRefSetMemberList list1;

  /** The list2 test fixture . */
  private LanguageRefSetMemberList list2;

  /** The test fixture o1. */
  private LanguageRefSetMember o1;

  /** The test fixture o2. */
  private LanguageRefSetMember o2;

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
    list1 = new LanguageRefSetMemberListJpa();
    list2 = new LanguageRefSetMemberListJpa();
    o1 = new LanguageRefSetMemberJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    Description d1 = new DescriptionJpa();
    d1.setId(1L);
    d1.setTerminologyId("1");
    d1.setTerm("1");
    o1.setComponent(d1);

    o2 = new LanguageRefSetMemberJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    Description d2 = new DescriptionJpa();
    d2.setId(2L);
    d2.setTerminologyId("2");
    d2.setTerm("2");
    o2.setComponent(d2);

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
