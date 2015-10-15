/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IndexedFieldTester;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ConceptJpa}.
 */
public class ModelUnit037Test extends ModelUnitSupport {

  /** The model object to test. */
  private ConceptJpa object;

  /** Test fixture t1 */
  private Translation t1;

  /** Test fixture t2 */
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
    object = new ConceptJpa();
    ProxyTester tester = new ProxyTester(new TranslationJpa());
    t1 = (TranslationJpa) tester.createObject(1);
    t2 = (TranslationJpa) tester.createObject(2);
    t1.setRefset(new RefsetJpa());
    t2.setRefset(new RefsetJpa());
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet037() throws Exception {
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
  public void testModelEqualsHashcode037() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("active");
    tester.include("moduleId");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("definitionStatusId");

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
  public void testModelCopy037() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructorDeep(Concept.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization037() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(Translation.class, 1, t1);
    tester.proxy(Translation.class, 2, t2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField037() throws Exception {
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
    tester.include("definitionStatusId");
    tester.include("name");
    tester.include("workflowStatus");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields037() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    tester.include("name");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    assertTrue(tester.testAnalyzedIndexedFields());
    tester = new IndexedFieldTester(object);
    tester.include("terminologyId");
    tester.include("terminology");
    tester.include("version");
    tester.include("effectiveTime");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("moduleId");
    tester.include("definitionStatusId");
    tester.include("workflowStatus");
    tester.include("nameSort");
    tester.include("translationId");

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
