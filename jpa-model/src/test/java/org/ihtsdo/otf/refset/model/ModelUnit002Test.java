/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ReleaseInfoJpa}.
 */
public class ModelUnit002Test {

  /** The model object to test. */
  private ReleaseInfoJpa object;

  /** The r1. */
  private Refset r1;

  /** The r2. */
  private Refset r2;

  /** The t1. */
  private Translation t1;

  /** The t2. */
  private Translation t2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    object = new ReleaseInfoJpa();

    ProxyTester tester1 = new ProxyTester(new RefsetJpa());
    r1 = (RefsetJpa) tester1.createObject(1);
    r2 = (RefsetJpa) tester1.createObject(2);

    tester1 = new ProxyTester(new TranslationJpa());
    t1 = (TranslationJpa) tester1.createObject(1);
    t2 = (TranslationJpa) tester1.createObject(2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet002");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode002");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("description");
    tester.include("name");
    tester.include("planned");
    tester.include("published");
    tester.include("terminology");
    tester.include("version");
    tester.include("refset");
    tester.include("translation");

    tester.proxy(Refset.class, 1, r1);
    tester.proxy(Refset.class, 2, r2);
    tester.proxy(Translation.class, 1, t1);
    tester.proxy(Translation.class, 2, t2);
    
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
  public void testModelCopy002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy002");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Refset.class, 1, r1);
    tester.proxy(Refset.class, 2, r2);
    tester.proxy(Translation.class, 1, t1);
    tester.proxy(Translation.class, 2, t2);
    assertTrue(tester.testCopyConstructor(ReleaseInfo.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient002");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(Refset.class, 1, r1);
    tester.proxy(Refset.class, 2, r2);
    tester.proxy(Translation.class, 1, t1);
    tester.proxy(Translation.class, 2, t2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test concept reference in XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient010() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient010");
    ReleaseInfo info = new ReleaseInfoJpa();
    info.setRefset(r1);
    info.setTranslation(t1);
    String xml = ConfigUtility.getStringForGraph(info);
    assertTrue(xml.contains("<refsetId>"));
    assertTrue(xml.contains("<translationId>"));
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField002");
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("name");
    tester.include("description");
    tester.include("planned");
    tester.include("published");
    tester.include("terminology");
    tester.include("version");
    tester.include("lastModified");
    tester.include("lastModifiedBy");

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
