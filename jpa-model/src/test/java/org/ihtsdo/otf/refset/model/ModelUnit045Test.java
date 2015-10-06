/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.ConceptDiffReport;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.ConceptDiffReportJpa;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ConceptDiffReportJpa}.
 */
public class ModelUnit045Test extends ModelUnitSupport {

  /** The model object to test. */
  private ConceptDiffReportJpa object;

  /** the test fixture t1 */
  private Translation t1;

  /** the test fixture t2 */
  private Translation t2;

  /** the test fixture l1 */
  private List<Concept> l1;

  /** the test fixture l2 */
  private List<Concept> l2;

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
    object = new ConceptDiffReportJpa();

    ProxyTester tester = new ProxyTester(new TranslationJpa());
    t1 = (TranslationJpa) tester.createObject(1);
    t1.setProject(new ProjectJpa());
    t2 = (TranslationJpa) tester.createObject(2);
    t2.setProject(new ProjectJpa());
    tester = new ProxyTester(new ConceptJpa());

    Concept cr1 = (Concept) tester.createObject(1);
    cr1.setTranslation(t1);
    Concept cr2 = (Concept) tester.createObject(2);
    cr2.setTranslation(t2);
    l1 = new ArrayList<>();
    l2 = new ArrayList<>();
    l1.add(cr1);
    l2.add(cr2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet044() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("valid");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode044() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("oldTranslation");
    tester.include("newTranslation");
    tester.include("oldNotNew");
    tester.include("newNotOld");
    tester.include("activeNowInactive");

    tester.proxy(Translation.class, 1, t1);
    tester.proxy(Translation.class, 2, t2);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);

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
  public void testModelCopy044() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Translation.class, 1, t1);
    tester.proxy(Translation.class, 2, t2);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);

    assertTrue(tester.testCopyConstructor(ConceptDiffReport.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization044() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);

    ConceptDiffReport report = new ConceptDiffReportJpa();
    Translation t1 = new TranslationJpa();
    t1.setId(1L);
    report.setOldTranslation(t1);
    report.setNewTranslation(t1);
    report.setOldNotNew(l1);
    report.setNewNotOld(l1);
    report.setActiveNowInactive(l1);
    tester.proxy(ConceptDiffReport.class, 1, report);
    tester.proxy(Translation.class, 1, t1);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test concept reference in XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient044() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    ConceptDiffReport report = new ConceptDiffReportJpa();

    Translation t1 = new TranslationJpa();
    t1.setId(1L);
    report.setOldTranslation(t1);
    report.setNewTranslation(t1);
    report.setOldNotNew(l1);
    report.setNewNotOld(l1);
    report.setActiveNowInactive(l1);

    String xml = ConfigUtility.getStringForGraph(report);
    assertTrue(xml.contains("<oldTranslationId>"));
    assertTrue(xml.contains("<newTranslationId>"));
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
