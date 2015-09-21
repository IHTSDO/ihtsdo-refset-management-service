/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import org.ihtsdo.otf.refset.helpers.RelationshipList;
import org.ihtsdo.otf.refset.jpa.helpers.RelationshipListJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RelationshipJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link RelationshipList}.
 */
public class ListUnit012Test extends AbstractListUnit<Relationship> {

  /** The list1 test fixture . */
  private RelationshipList list1;

  /** The list2 test fixture . */
  private RelationshipList list2;

  /** The test fixture o1. */
  private Relationship o1;

  /** The test fixture o2. */
  private Relationship o2;

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
    list1 = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    o1 = new RelationshipJpa();
    o1.setId(1L);
    o1.setTerminologyId("1");
    o1.setTypeId("1");
    Concept c1 = new ConceptJpa();
    c1.setId(1L);
    c1.setTerminologyId("1");
    c1.setDefaultPreferredName("1");
    Concept c2 = new ConceptJpa();
    c2.setId(2L);
    c2.setTerminologyId("2");
    c2.setDefaultPreferredName("2");
    o1.setSourceConcept(c1);
    o1.setDestinationConcept(c2);

    o2 = new RelationshipJpa();
    o2.setId(2L);
    o2.setTerminologyId("2");
    o2.setTypeId("2");
    Concept c3 = new ConceptJpa();
    c3.setId(3L);
    c3.setTerminologyId("3");
    c3.setDefaultPreferredName("3");
    Concept c4 = new ConceptJpa();
    c4.setId(4L);
    c4.setTerminologyId("4");
    c4.setDefaultPreferredName("4");
    o2.setSourceConcept(c3);
    o2.setDestinationConcept(c4);

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse012() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse012() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases012() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization012() throws Exception {
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
