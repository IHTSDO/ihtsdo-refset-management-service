/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.MemberValidationResult;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.MemberValidationResultList;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.jpa.MemberValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.MemberValidationResultListJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link MemberValidationResultList}.
 */
public class MemberValidationResultListUnitTest extends AbstractListUnit<MemberValidationResult> {

  /** The list1 test fixture . */
  private MemberValidationResultList list1;

  /** The list2 test fixture . */
  private MemberValidationResultList list2;

  /** The test fixture o1. */
  private MemberValidationResult o1;

  /** The test fixture o2. */
  private MemberValidationResult o2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    list1 = new MemberValidationResultListJpa();
    list2 = new MemberValidationResultListJpa();

    ProxyTester tester = new ProxyTester(new MemberValidationResultJpa());
    o1 = (MemberValidationResult) tester.createObject(1);
    o2 = (MemberValidationResult) tester.createObject(2);
    Refset r1 = new RefsetJpa();
    r1.setId(1L);
    Refset r2 = new RefsetJpa();
    r2.setId(2L);
    ConceptRefsetMember m1 = new ConceptRefsetMemberJpa();
    m1.setId(1L);
    m1.getSynonyms().add("testWord1");
    ConceptRefsetMember m2 = new ConceptRefsetMemberJpa();
    m2.setId(2L);
    m2.getSynonyms().add("testWord2");
    m1.setRefset(r1);
    m2.setRefset(r2);
    o1.setMember(m1);
    o2.setMember(m2);
    o1.addComment("1");
    o2.addComment("2");
  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse018() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse018() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases018() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization018() throws Exception {
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
