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
import org.ihtsdo.otf.refset.rf2.AssociationReferenceDescriptionRefSetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.jpa.AssociationReferenceDescriptionRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link AssociationReferenceDescriptionRefSetMemberJpa}.
 */
public class ModelUnit013Test {

  /** The model object to test. */
  private AssociationReferenceDescriptionRefSetMemberJpa object;

  /** The test fixture d1. */
  private Description d1;

  /** The test fixture d2. */
  private Description d2;

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
    object = new AssociationReferenceDescriptionRefSetMemberJpa();
    // Set up some objects
    d1 = new DescriptionJpa();
    d1.setId(1L);
    d1.setTerminologyId("1");
    d1.setTypeId("1");
    d2 = new DescriptionJpa();
    d2.setId(2L);
    d2.setTerminologyId("2");
    d2.setTypeId("2");

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet013() throws Exception {
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
  public void testModelEqualsHashcode013() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode013");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("active");
    tester.include("moduleId");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("refSetId");
    tester.include("description");
    // needed for generic refset class
    tester.include("component");
    tester.include("targetComponentId");

    // Set up some objects
    tester.proxy(Description.class, 1, d1);
    tester.proxy(Description.class, 2, d2);

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
  public void testModelCopy013() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy009");
    CopyConstructorTester tester = new CopyConstructorTester(object);

    // Set up some objects
    tester.proxy(Description.class, 1, d1);
    tester.proxy(Description.class, 2, d2);

    assertTrue(tester
        .testCopyConstructor(AssociationReferenceDescriptionRefSetMember.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization013() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient013");
    XmlSerializationTester tester = new XmlSerializationTester(object);

    // Set up some objects
    Description d = new DescriptionJpa();
    d.setId(1L);
    d.setTerminology("1");
    d.setTerminologyId("1");
    d.setVersion("1");
    d.setTerm("1");
    tester.proxy(Description.class, 1, d);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test concept reference in XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient013() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient009");
    Description d = new DescriptionJpa();
    d.setId(1L);
    d.setTerminologyId("1");
    d.setTerm("1");
    AssociationReferenceDescriptionRefSetMember member =
        new AssociationReferenceDescriptionRefSetMemberJpa();
    member.setId(1L);
    member.setTerminologyId("1");
    member.setDescription(d);
    d.addAssociationReferenceRefSetMember(member);
    String xml = ConfigUtility.getStringForGraph(member);
    assertTrue(xml.contains("<descriptionId>"));
    assertTrue(xml.contains("<descriptionTerminologyId>"));
    assertTrue(xml.contains("<descriptionTerm>"));
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField013() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField013");
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
    tester.include("targetComponentId");

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
