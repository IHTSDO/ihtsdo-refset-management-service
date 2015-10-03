/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IndexedFieldTester;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link TranslationJpa}.
 */
public class ModelUnit035Test {

  /** The model object to test. */
  private Translation object;

  /** The test fixture r1. */
  private Refset r1;

  /** The test fixture r2. */
  private Refset r2;

  /** The test fixture r1. */
  private Project p1;

  /** The test fixture r2. */
  private Project p2;

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
    object = new TranslationJpa();
    ProxyTester tester = new ProxyTester(new RefsetJpa());
    r1 = (RefsetJpa) tester.createObject(1);
    r2 = (RefsetJpa) tester.createObject(2);
    tester = new ProxyTester(new ProjectJpa());
    p1 = (ProjectJpa) tester.createObject(1);
    p2 = (ProjectJpa) tester.createObject(2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet030() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet030");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode030() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode030");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("active");
    tester.include("moduleId");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("description");
    tester.include("public");
    tester.include("language");
    tester.include("name");

    // Set up objects
    tester.proxy(Refset.class, 1, r1);
    tester.proxy(Refset.class, 2, r2);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);

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
  public void testModelCopy030() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy030");
    CopyConstructorTester tester = new CopyConstructorTester(object);

    // Set up objects
    tester.proxy(Refset.class, 1, r1);
    tester.proxy(Refset.class, 2, r2);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);

    assertTrue(tester.testCopyConstructor(Translation.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization030() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization030");
    XmlSerializationTester tester = new XmlSerializationTester(object);

    // Set up objects
    Refset r = new RefsetJpa();
    r.setId(1L);
    tester.proxy(Refset.class, 1, r);
    Project p = new ProjectJpa();
    p.setId(1L);
    tester.proxy(Project.class, 1, p);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField030() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField030");
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

    tester.include("name");
    tester.include("description");
    tester.include("isPublic");
    tester.include("workflowStatus");
    tester.include("workflowPath");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields034() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelIndexedFields034");

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    tester.include("name");
    tester.include("description");
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
    tester.include("language");
    tester.include("refsetId");
    tester.include("projectId");
    tester.include("workflowStatus");

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
