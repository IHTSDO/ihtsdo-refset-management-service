/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.ConceptValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptValidationResultList;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.jpa.ConceptValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptValidationResultListJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ConceptValidationResultList}.
 */
public class ListUnit021Test extends AbstractListUnit<ConceptValidationResult> {

  /** The list1 test fixture . */
  private ConceptValidationResultList list1;

  /** The list2 test fixture . */
  private ConceptValidationResultList list2;

  /** The test fixture o1. */
  private ConceptValidationResult o1;

  /** The test fixture o2. */
  private ConceptValidationResult o2;

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
    list1 = new ConceptValidationResultListJpa();
    list2 = new ConceptValidationResultListJpa();

    ProxyTester tester = new ProxyTester(new ConceptValidationResultJpa());
    o1 = (ConceptValidationResult) tester.createObject(1);
    o2 = (ConceptValidationResult) tester.createObject(2);
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
