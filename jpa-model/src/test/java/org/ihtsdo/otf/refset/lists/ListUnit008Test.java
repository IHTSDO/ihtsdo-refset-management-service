/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.ModuleDependencyRefSetMemberList;
import org.ihtsdo.otf.refset.jpa.helpers.ModuleDependencyRefSetMemberListJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ModuleDependencyRefSetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ModuleDependencyRefSetMemberList}.
 */
public class ListUnit008Test extends
    AbstractListUnit<ModuleDependencyRefSetMember> {

  /** The list1 test fixture . */
  private ModuleDependencyRefSetMemberList list1;

  /** The list2 test fixture . */
  private ModuleDependencyRefSetMemberList list2;

  /** The test fixture o1. */
  private ModuleDependencyRefSetMember o1;

  /** The test fixture o2. */
  private ModuleDependencyRefSetMember o2;

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
    list1 = new ModuleDependencyRefSetMemberListJpa();
    list2 = new ModuleDependencyRefSetMemberListJpa();
    o1 = new ModuleDependencyRefSetMemberJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    Concept c1 = new ConceptJpa();
    c1.setId(1L);
    c1.setTerminologyId("1");
    c1.setDefaultPreferredName("1");
    o1.setComponent(c1);
    o2 = new ModuleDependencyRefSetMemberJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    Concept c2 = new ConceptJpa();
    c2.setId(2L);
    c2.setTerminologyId("2");
    c2.setDefaultPreferredName("2");
    o2.setComponent(c2);

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
