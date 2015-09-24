/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.ModuleDependencyRefsetMemberList;
import org.ihtsdo.otf.refset.jpa.helpers.ModuleDependencyRefsetMemberListJpa;
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ModuleDependencyRefsetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ModuleDependencyRefsetMemberList}.
 */
public class ListUnit008Test extends
    AbstractListUnit<ModuleDependencyRefsetMember> {

  /** The list1 test fixture . */
  private ModuleDependencyRefsetMemberList list1;

  /** The list2 test fixture . */
  private ModuleDependencyRefsetMemberList list2;

  /** The test fixture o1. */
  private ModuleDependencyRefsetMember o1;

  /** The test fixture o2. */
  private ModuleDependencyRefsetMember o2;

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
    list1 = new ModuleDependencyRefsetMemberListJpa();
    list2 = new ModuleDependencyRefsetMemberListJpa();
    o1 = new ModuleDependencyRefsetMemberJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    o1.setConceptId("1");
    o2 = new ModuleDependencyRefsetMemberJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    o2.setConceptId("2");
  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse008() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse008() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases008() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization008() throws Exception {
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
