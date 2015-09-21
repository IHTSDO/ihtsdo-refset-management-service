/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.SimpleRefSetMemberList;
import org.ihtsdo.otf.refset.jpa.helpers.SimpleRefSetMemberListJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleRefSetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link SimpleRefSetMemberList}.
 */
public class ListUnit017Test extends AbstractListUnit<SimpleRefSetMember> {

  /** The list1 test fixture . */
  private SimpleRefSetMemberList list1;

  /** The list2 test fixture . */
  private SimpleRefSetMemberList list2;

  /** The test fixture o1. */
  private SimpleRefSetMember o1;

  /** The test fixture o2. */
  private SimpleRefSetMember o2;

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
    list1 = new SimpleRefSetMemberListJpa();
    list2 = new SimpleRefSetMemberListJpa();
    o1 = new SimpleRefSetMemberJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    Concept c1 = new ConceptJpa();
    c1.setId(1L);
    c1.setTerminologyId("1");
    c1.setDefaultPreferredName("1");
    o1.setComponent(c1);
    o2 = new SimpleRefSetMemberJpa();
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
