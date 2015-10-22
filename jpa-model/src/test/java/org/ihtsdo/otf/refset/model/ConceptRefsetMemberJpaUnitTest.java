/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ConceptRefsetMemberJpa}.
 */
public class ConceptRefsetMemberJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private ConceptRefsetMemberJpa object;

  /** The r1. */
  private RefsetJpa r1;

  /** The r2. */
  private RefsetJpa r2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // n/a
  }

  /**
   * Setup.
   * @throws Exception 
   */
  @Before
  public void setup() throws Exception {
    object = new ConceptRefsetMemberJpa();

    ProxyTester tester = new ProxyTester(new RefsetJpa());
    r1 = (RefsetJpa) tester.createObject(1);
    r2 = (RefsetJpa) tester.createObject(2);
    r1.setProject(new ProjectJpa());
    r2.setProject(new ProjectJpa());
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("active");
    tester.include("moduleId");
    tester.include("terminology");
    tester.include("terminologyId");
    //tester.include("version");
    //tester.include("refset");
    tester.include("conceptId");
    tester.include("conceptName");
    tester.include("memberType");

    tester.proxy(Refset.class, 1, r1);
    tester.proxy(Refset.class, 1, r2); 
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
  public void testModelCopy021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Refset.class, 1, r1);
    tester.proxy(Refset.class, 1, r2);    
    assertTrue(tester.testCopyConstructor(ConceptRefsetMember.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);

    Refset r = new RefsetJpa();
    r.setId(1L);
    tester.proxy(Refset.class, 1, r);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
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
    tester.include("memberType");

    tester.include("conceptId");
    tester.include("conceptName");
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
