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
import org.ihtsdo.otf.refset.jpa.helpers.IndexedFieldTester;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.ihtsdo.otf.refset.rf2.AssociationReferenceDescriptionRefSetMember;
import org.ihtsdo.otf.refset.rf2.AttributeValueDescriptionRefSetMember;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.AssociationReferenceDescriptionRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.AttributeValueDescriptionRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefSetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link DescriptionJpa}.
 */
public class ModelUnit008Test {

  /** The model object to test. */
  private DescriptionJpa object;

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
    object = new DescriptionJpa();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet001");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode001");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("active");
    tester.include("moduleId");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("caseSignificanceId");
    tester.include("concept");
    tester.include("languageCode");
    tester.include("term");
    tester.include("typeId");

    // Set up some objects
    Concept c1 = new ConceptJpa();
    c1.setDefinitionStatusId("1");
    c1.setTerminologyId("1");
    c1.setId(1L);
    Concept c2 = new ConceptJpa();
    c2.setId(2L);
    c2.setTerminologyId("2");
    c2.setDefinitionStatusId("2");
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
  public void testModelCopy008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy001");
    CopyConstructorTester tester = new CopyConstructorTester(object);

    // Set up some objects
    Concept c1 = new ConceptJpa();
    c1.setDefinitionStatusId("1");
    c1.setTerminologyId("1");
    c1.setId(1L);
    Concept c2 = new ConceptJpa();
    c2.setId(2L);
    c2.setTerminologyId("2");
    c2.setDefinitionStatusId("2");
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);

    assertTrue(tester.testCopyConstructorDeep(Description.class));
  }

  /**
   * Test deep copy.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy008");
    Description d = new DescriptionJpa();
    Concept c = new ConceptJpa();
    c.setId(1L);
    c.setTerminologyId("1");
    c.setDefaultPreferredName("1");
    d.setConcept(c);

    LanguageRefSetMember lmember = new LanguageRefSetMemberJpa();
    lmember.setId(1L);
    lmember.setTerminologyId("1");
    lmember.setDescription(d);
    d.addLanguageRefSetMember(lmember);

    AttributeValueDescriptionRefSetMember avmember =
        new AttributeValueDescriptionRefSetMemberJpa();
    avmember.setTerminologyId("1");
    avmember.setDescription(d);
    d.addAttributeValueRefSetMember(avmember);

    AssociationReferenceDescriptionRefSetMember asmember =
        new AssociationReferenceDescriptionRefSetMemberJpa();
    asmember.setTerminologyId("1");
    asmember.setDescription(d);
    d.addAssociationReferenceRefSetMember(asmember);

    // Deep copy includes language, attribute value, and association reference
    // members
    Description d3 = new DescriptionJpa(d, true);
    assertTrue(d3.getLanguageRefSetMembers().size() == 1);
    assertTrue(d3.getLanguageRefSetMembers().iterator().next().equals(lmember));
    assertTrue(d.equals(d3));
    assertTrue(d3.getAttributeValueRefSetMembers().size() == 1);
    assertTrue(d3.getAttributeValueRefSetMembers().iterator().next()
        .equals(avmember));
    assertTrue(d3.getAssociationReferenceRefSetMembers().size() == 1);
    assertTrue(d3.getAssociationReferenceRefSetMembers().iterator().next()
        .equals(asmember));

  }

  /**
   * Test concept reference in XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient008");
    Description d = new DescriptionJpa();
    Concept c = new ConceptJpa();
    c.setId(1L);
    c.setTerminologyId("1");
    c.setDefaultPreferredName("1");
    d.setConcept(c);
    String xml = ConfigUtility.getStringForGraph(d);
    assertTrue(xml.contains("<conceptId>"));
    assertTrue(xml.contains("<conceptTerminologyId>"));
    assertTrue(xml.contains("<conceptPreferredName>"));

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient008");
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
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField008");
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

    tester.include("languageCode");
    tester.include("typeId");
    tester.include("term");
    tester.include("caseSignificanceId");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelIndexedFields007");

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    tester.include("term");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    assertTrue(tester.testAnalyzedIndexedFields());
    tester = new IndexedFieldTester(object);
    tester.include("effectiveTime");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("moduleId");
    tester.include("terminologyId");
    tester.include("terminology");
    tester.include("version");
    tester.include("termSort");
    tester.include("typeId");

    assertTrue(tester.testNotAnalyzedIndexedFields());

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
