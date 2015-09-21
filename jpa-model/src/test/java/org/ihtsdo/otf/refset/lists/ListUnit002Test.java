/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.AttributeValueRefSetMemberList;
import org.ihtsdo.otf.refset.jpa.helpers.AttributeValueRefSetMemberListJpa;
import org.ihtsdo.otf.refset.rf2.AttributeValueRefSetMember;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.jpa.AttributeValueConceptRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.AttributeValueDescriptionRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link AttributeValueRefSetMemberList}.
 */
public class ListUnit002Test extends
    AbstractListUnit<AttributeValueRefSetMember<?>> {

  /** The list1 test fixture . */
  private AttributeValueRefSetMemberList list1;

  /** The list2 test fixture . */
  private AttributeValueRefSetMemberList list2;

  /** The list3 test fixture . */
  private AttributeValueRefSetMemberList list3;

  /** The list4 test fixture . */
  private AttributeValueRefSetMemberList list4;

  /** The test fixture o1. */
  private AttributeValueRefSetMember<Concept> o1;

  /** The test fixture o2. */
  private AttributeValueRefSetMember<Concept> o2;

  /** The test fixture o3. */
  private AttributeValueRefSetMember<Description> o3;

  /** The test fixture o4. */
  private AttributeValueRefSetMember<Description> o4;

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
    list1 = new AttributeValueRefSetMemberListJpa();
    list2 = new AttributeValueRefSetMemberListJpa();
    list3 = new AttributeValueRefSetMemberListJpa();
    list4 = new AttributeValueRefSetMemberListJpa();
    o1 = new AttributeValueConceptRefSetMemberJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    Concept c1 = new ConceptJpa();
    c1.setId(1L);
    c1.setTerminologyId("1");
    c1.setDefaultPreferredName("1");
    o1.setComponent(c1);
    o2 = new AttributeValueConceptRefSetMemberJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    Concept c2 = new ConceptJpa();
    c2.setId(2L);
    c2.setTerminologyId("2");
    c2.setDefaultPreferredName("2");
    o2.setComponent(c2);

    o3 = new AttributeValueDescriptionRefSetMemberJpa();
    o3.setId(3L);
    o3.setTerminologyId("3");
    Description d1 = new DescriptionJpa();
    d1.setId(1L);
    d1.setTerminologyId("1");
    d1.setTerm("1");
    o3.setComponent(d1);

    o4 = new AttributeValueDescriptionRefSetMemberJpa();
    o4.setId(4L);
    o4.setTerminologyId("4");
    Description d2 = new DescriptionJpa();
    d2.setId(2L);
    d2.setTerminologyId("2");
    d2.setTerm("2");
    o4.setComponent(d2);
  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse002() throws Exception {
    testNormalUse(list1, list2, o1, o2);
    testNormalUse(list3, list4, o3, o4);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse002() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
    testDegenerateUse(list3, list4, o3, o4);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases002() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
    testEdgeCases(list3, list4, o3, o4);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization002() throws Exception {
    testXmllSerialization(list1, list2, o1, o2);
    testXmllSerialization(list3, list4, o3, o4);
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
