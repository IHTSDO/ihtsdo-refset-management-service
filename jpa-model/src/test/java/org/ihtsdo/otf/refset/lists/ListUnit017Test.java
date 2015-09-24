/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.SimpleRefsetMemberList;
import org.ihtsdo.otf.refset.jpa.helpers.SimpleRefsetMemberListJpa;
import org.ihtsdo.otf.refset.rf2.SimpleRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleRefsetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link SimpleRefsetMemberList}.
 */
public class ListUnit017Test extends AbstractListUnit<SimpleRefsetMember> {

  /** The list1 test fixture . */
  private SimpleRefsetMemberList list1;

  /** The list2 test fixture . */
  private SimpleRefsetMemberList list2;

  /** The test fixture o1. */
  private SimpleRefsetMember o1;

  /** The test fixture o2. */
  private SimpleRefsetMember o2;

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
    list1 = new SimpleRefsetMemberListJpa();
    list2 = new SimpleRefsetMemberListJpa();
    o1 = new SimpleRefsetMemberJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    o1.setConceptId("1");
    o2 = new SimpleRefsetMemberJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    o2.setConceptId("2");
  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse017() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse017() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases017() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization017() throws Exception {
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
