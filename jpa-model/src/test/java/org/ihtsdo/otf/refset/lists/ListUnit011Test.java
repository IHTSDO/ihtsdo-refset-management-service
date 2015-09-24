/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.RefsetDescriptorRefsetMemberList;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetDescriptorRefsetMemberListJpa;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefsetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link RefsetDescriptorRefsetMemberList}.
 */
public class ListUnit011Test extends
    AbstractListUnit<RefsetDescriptorRefsetMember> {

  /** The list1 test fixture . */
  private RefsetDescriptorRefsetMemberList list1;

  /** The list2 test fixture . */
  private RefsetDescriptorRefsetMemberList list2;

  /** The test fixture o1. */
  private RefsetDescriptorRefsetMember o1;

  /** The test fixture o2. */
  private RefsetDescriptorRefsetMember o2;

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
    list1 = new RefsetDescriptorRefsetMemberListJpa();
    list2 = new RefsetDescriptorRefsetMemberListJpa();
    o1 = new RefsetDescriptorRefsetMemberJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    o1.setConceptId("1");
    o2 = new RefsetDescriptorRefsetMemberJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    o2.setConceptId("1");
  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse011() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse011() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases011() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization011() throws Exception {
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
