/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.helpers.IndexedFieldTester;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.ihtsdo.otf.refset.rf2.AssociationReferenceConceptRefSetMember;
import org.ihtsdo.otf.refset.rf2.AttributeValueConceptRefSetMember;
import org.ihtsdo.otf.refset.rf2.ComplexMapRefSetMember;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.rf2.SimpleMapRefSetMember;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.AssociationReferenceConceptRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.AttributeValueConceptRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ComplexMapRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RelationshipJpa;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleMapRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleRefSetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ConceptJpa}.
 */
public class ModelUnit007Test {

  /** The model object to test. */
  private ConceptJpa object;

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
    object = new ConceptJpa();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet007");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode007");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("active");
    tester.include("moduleId");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("definitionStatusId");

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
  public void testModelCopy007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy007");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructorDeep(Concept.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient007");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test deep copy.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy007");
    Concept c = new ConceptJpa();
    c.setId(1L);
    c.setTerminologyId("1");
    c.setDefaultPreferredName("1");

    Description d = new DescriptionJpa();
    d.setId(1L);
    d.setTerminologyId("1");
    d.setTerm("1");
    d.setTypeId("1");
    d.setConcept(c);
    c.addDescription(d);

    Relationship r = new RelationshipJpa();
    r.setId(1L);
    r.setTerminologyId("1");
    r.setTypeId("1");
    r.setSourceConcept(c);
    r.setDestinationConcept(c);
    c.addRelationship(r);

    AttributeValueConceptRefSetMember avmember =
        new AttributeValueConceptRefSetMemberJpa();
    avmember.setTerminologyId("1");
    avmember.setConcept(c);
    c.addAttributeValueRefSetMember(avmember);

    AssociationReferenceConceptRefSetMember asmember =
        new AssociationReferenceConceptRefSetMemberJpa();
    asmember.setTerminologyId("1");
    asmember.setConcept(c);
    c.addAssociationReferenceRefSetMember(asmember);

    ComplexMapRefSetMember cmmember = new ComplexMapRefSetMemberJpa();
    cmmember.setTerminologyId("1");
    cmmember.setConcept(c);
    c.addComplexMapRefSetMember(cmmember);

    SimpleRefSetMember smember = new SimpleRefSetMemberJpa();
    smember.setTerminologyId("1");
    smember.setConcept(c);
    c.addSimpleRefSetMember(smember);

    SimpleMapRefSetMember smmember = new SimpleMapRefSetMemberJpa();
    smmember.setTerminologyId("1");
    smmember.setConcept(c);
    c.addSimpleMapRefSetMember(smmember);

    // Deep copy includes simple, simple map, complex map, attribute value, and
    // association reference members
    Concept c3 = new ConceptJpa(c, true);
    assertTrue(c3.getDescriptions().size() == 1);
    assertTrue(c3.getDescriptions().iterator().next().equals(d));
    assertTrue(c3.getRelationships().size() == 1);
    assertTrue(c3.getRelationships().iterator().next().equals(r));
    assertTrue(c.equals(c3));
    assertTrue(c3.getAttributeValueRefSetMembers().size() == 1);
    assertTrue(c3.getAttributeValueRefSetMembers().iterator().next()
        .equals(avmember));
    assertTrue(c3.getAssociationReferenceRefSetMembers().size() == 1);
    assertTrue(c3.getAssociationReferenceRefSetMembers().iterator().next()
        .equals(asmember));
    assertTrue(c3.getComplexMapRefSetMembers().size() == 1);
    assertTrue(c3.getComplexMapRefSetMembers().iterator().next()
        .equals(cmmember));
    assertTrue(c3.getSimpleRefSetMembers().size() == 1);
    assertTrue(c3.getSimpleRefSetMembers().iterator().next().equals(smember));
    assertTrue(c3.getSimpleMapRefSetMembers().size() == 1);
    assertTrue(c3.getSimpleMapRefSetMembers().iterator().next()
        .equals(smmember));

  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField007");
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
    tester.include("fullyDefined");

    tester.include("definitionStatusId");
    tester.include("anonymous");
    tester.include("defaultPreferredName");

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
    tester.include("defaultPreferredName");
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
    tester.include("defaultPreferredNameSort");

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
