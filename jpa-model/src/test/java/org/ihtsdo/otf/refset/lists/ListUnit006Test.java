/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.DescriptionTypeRefsetMemberList;
import org.ihtsdo.otf.refset.jpa.helpers.DescriptionTypeRefsetMemberListJpa;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeRefsetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link DescriptionTypeRefsetMemberList}.
 */
public class ListUnit006Test extends
    AbstractListUnit<DescriptionTypeRefsetMember> {

  /** The list1 test fixture . */
  private DescriptionTypeRefsetMemberList list1;

  /** The list2 test fixture . */
  private DescriptionTypeRefsetMemberList list2;

  /** The test fixture o1. */
  private DescriptionTypeRefsetMember o1;

  /** The test fixture o2. */
  private DescriptionTypeRefsetMember o2;

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
    list1 = new DescriptionTypeRefsetMemberListJpa();
    list2 = new DescriptionTypeRefsetMemberListJpa();
    o1 = new DescriptionTypeRefsetMemberJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    o1.setConceptId("1");
    o2 = new DescriptionTypeRefsetMemberJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    o2.setConceptId("2");
  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse006() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse006() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases006() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization006() throws Exception {
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
