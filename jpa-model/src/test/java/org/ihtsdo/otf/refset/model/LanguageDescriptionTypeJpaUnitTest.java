/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageDescriptionTypeJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link LanguageDescriptionTypeJpa}.
 */
public class LanguageDescriptionTypeJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private LanguageDescriptionTypeJpa object;

  /** The test fixture d1. */
  private DescriptionTypeJpa d1;

  /** The test fixture d2. */
  private DescriptionTypeJpa d2;

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
    object = new LanguageDescriptionTypeJpa();

    ProxyTester tester = new ProxyTester(new DescriptionTypeJpa());
    d1 = (DescriptionTypeJpa) tester.createObject(1);
    d2 = (DescriptionTypeJpa) tester.createObject(2);

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet017() throws Exception {
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
  public void testModelEqualsHashcode017() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("refsetId");
    tester.include("name");
    tester.include("descriptionType");

    tester.proxy(DescriptionType.class, 1, d1);
    tester.proxy(DescriptionType.class, 2, d2);

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
  public void testModelCopy017() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);

    tester.proxy(DescriptionType.class, 1, d1);
    tester.proxy(DescriptionType.class, 2, d2);

    assertTrue(tester.testCopyConstructor(LanguageDescriptionType.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization017() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(DescriptionType.class, 1, d1);
    tester.proxy(DescriptionType.class, 2, d2);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField017() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("refsetId");
    tester.include("name");
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
