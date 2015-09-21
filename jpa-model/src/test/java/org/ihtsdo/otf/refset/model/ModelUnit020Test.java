/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.SimpleMapRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleMapRefSetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link SimpleMapRefSetMemberJpa}.
 */
public class ModelUnit020Test {

  /** The model object to test. */
  private SimpleMapRefSetMemberJpa object;

  /** The test fixture c1. */
  private Concept c1;

  /** The test fixture c2. */
  private Concept c2;

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
    object = new SimpleMapRefSetMemberJpa();
    // Set up some objects
    c1 = new ConceptJpa();
    c1.setId(1L);
    c1.setTerminologyId("1");
    c1.setDefinitionStatusId("1");
    c2 = new ConceptJpa();
    c2.setId(2L);
    c2.setTerminologyId("2");
    c2.setDefinitionStatusId("2");

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet020() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet009");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode020() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode020");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("active");
    tester.include("moduleId");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("refSetId");
    tester.include("concept");
    // needed for generic refset class
    tester.include("component");
    tester.include("mapTarget");

    // Set up some objects
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);

    assertTrue(tester.testIdentitiyFieldEquals());
    assertTrue(tester.testNonIdentitiyFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentitiyFieldHashcode());
    assertTrue(tester.testNonIdentitiyFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy020() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy009");
    CopyConstructorTester tester = new CopyConstructorTester(object);

    // Set up some objects
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);

    assertTrue(tester.testCopyConstructor(SimpleMapRefSetMember.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization020() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient020");
    XmlSerializationTester tester = new XmlSerializationTester(object);

    // Set up some objects
    Concept c = new ConceptJpa();
    c.setId(1L);
    c.setTerminology("1");
    c.setTerminologyId("1");
    c.setVersion("1");
    c.setDefaultPreferredName("1");
    tester.proxy(Concept.class, 1, c);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test concept reference in XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient020() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient020");
    Concept c = new ConceptJpa();
    c.setId(1L);
    c.setTerminologyId("1");
    c.setDefaultPreferredName("1");
    SimpleMapRefSetMember member = new SimpleMapRefSetMemberJpa();
    member.setId(1L);
    member.setTerminologyId("1");
    member.setConcept(c);
    c.addSimpleMapRefSetMember(member);
    String xml = ConfigUtility.getStringForGraph(member);
    assertTrue(xml.contains("<conceptId>"));
    assertTrue(xml.contains("<conceptTerminologyId>"));
    assertTrue(xml.contains("<conceptPreferredName>"));
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField020() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField020");
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("active");
    tester.include("published");
    tester.include("publishable");
    tester.include("moduleId");
    tester.include("terminologyId");
    tester.include("terminology");
    tester.include("version");

    tester.include("refSetId");
    tester.include("mapTarget");
    assertTrue(tester.testNotNullFields());
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
