/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ReleaseInfoList}.
 */
public class ListUnit013Test extends AbstractListUnit<ReleaseInfo> {

  /** The list1 test fixture . */
  private ReleaseInfoList list1;

  /** The list2 test fixture . */
  private ReleaseInfoList list2;

  /** The test fixture o1. */
  private ReleaseInfo o1;

  /** The test fixture o2. */
  private ReleaseInfo o2;

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
    list1 = new ReleaseInfoListJpa();
    list2 = new ReleaseInfoListJpa();

    ProxyTester tester = new ProxyTester(new ReleaseInfoJpa());
    o1 = (ReleaseInfo) tester.createObject(1);
    o2 = (ReleaseInfo) tester.createObject(2);

    ProxyTester tester1 = new ProxyTester(new RefsetJpa());
    o1.setRefset((Refset) tester1.createObject(1));
    o2.setRefset((Refset) tester1.createObject(2));

    tester1 = new ProxyTester(new TranslationJpa());
    o1.setTranslation((Translation) tester1.createObject(1));
    o2.setTranslation((Translation) tester1.createObject(2));

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse013() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse013() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases013() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization013() throws Exception {
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
