/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.DescriptionTypeList;
import org.ihtsdo.otf.refset.jpa.helpers.DescriptionTypeListJpa;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link DescriptionTypeList}.
 */
public class DescriptionTypeListUnitTest extends
    AbstractListUnit<DescriptionType> {

  /** The list1 test fixture . */
  private DescriptionTypeList list1;

  /** The list2 test fixture . */
  private DescriptionTypeList list2;

  /** The test fixture o1. */
  private DescriptionType o1;

  /** The test fixture o2. */
  private DescriptionType o2;

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
    list1 = new DescriptionTypeListJpa();
    list2 = new DescriptionTypeListJpa();
    o1 = new DescriptionTypeJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    o1.setName("1");
    o1.setTypeId("1");
    o2 = new DescriptionTypeJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    o2.setName("2");
    o2.setTypeId("2");
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
