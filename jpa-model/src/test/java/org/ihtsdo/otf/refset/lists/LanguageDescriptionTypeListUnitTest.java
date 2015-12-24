/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.LanguageDescriptionTypeList;
import org.ihtsdo.otf.refset.jpa.helpers.LanguageDescriptionTypeListJpa;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageDescriptionTypeJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link LanguageDescriptionTypeList}.
 */
public class LanguageDescriptionTypeListUnitTest extends
    AbstractListUnit<LanguageDescriptionType> {

  /** The list1 test fixture . */
  private LanguageDescriptionTypeList list1;

  /** The list2 test fixture . */
  private LanguageDescriptionTypeList list2;

  /** The test fixture o1. */
  private LanguageDescriptionType o1;

  /** The test fixture o2. */
  private LanguageDescriptionType o2;

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
    list1 = new LanguageDescriptionTypeListJpa();
    list2 = new LanguageDescriptionTypeListJpa();
    o1 = new LanguageDescriptionTypeJpa();
    o2 = new LanguageDescriptionTypeJpa();
    o1.setId(1L);
    o1.setName("1");
    o1.setAcceptabilityId("1");
    o1.setTypeId("1");
    o2.setId(2L);
    o2.setName("2");
    o2.setTypeId("2");
    o2.setAcceptabilityId("2");
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
