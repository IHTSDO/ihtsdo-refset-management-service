/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ConceptRefsetMemberList}.
 */
public class ConceptRefsetMemberListUnitTest
    extends AbstractListUnit<ConceptRefsetMember> {

  /** The list1 test fixture . */
  private ConceptRefsetMemberList list1;

  /** The list2 test fixture . */
  private ConceptRefsetMemberList list2;

  /** The test fixture o1. */
  private ConceptRefsetMember o1;

  /** The test fixture o2. */
  private ConceptRefsetMember o2;

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
    list1 = new ConceptRefsetMemberListJpa();
    list2 = new ConceptRefsetMemberListJpa();
    o1 = new ConceptRefsetMemberJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    o1.setConceptId("1");
    o1.setRefset(new RefsetJpa());
    o1.getRefset().setId(1L);
    o2 = new ConceptRefsetMemberJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    o2.setConceptId("2");
    o2.setRefset(new RefsetJpa());
    o2.getRefset().setId(2L);
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
