/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IndexedFieldTester;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefsetMemberJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link RefsetJpa}.
 */
public class RefsetJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private Refset object;

  /** The test fixture p1. */
  private Project p1;

  /** The test fixture p2. */
  private Project p2;

  /** The test fixture r1. */
  private RefsetDescriptorRefsetMember r1;

  /** The test fixture r2. */
  private RefsetDescriptorRefsetMember r2;

  /** The test fixture l1. */
  @SuppressWarnings("rawtypes")
  private List l1;

  /** The test fixture l2. */
  @SuppressWarnings("rawtypes")
  private List l2;

  /** The test fixture s1. */
  private Set<Refset.FeedbackEvent> s1;

  /** The test fixture s2. */
  private Set<Refset.FeedbackEvent> s2;

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
  @SuppressWarnings({
      "rawtypes", "cast", "unchecked"
  })
  @Before
  public void setup() throws Exception {
    object = new RefsetJpa();
    ProxyTester tester = new ProxyTester(new ProjectJpa());
    p1 = (ProjectJpa) tester.createObject(1);
    p2 = (ProjectJpa) tester.createObject(2);
    tester = new ProxyTester(new RefsetDescriptorRefsetMemberJpa());
    r1 = (RefsetDescriptorRefsetMemberJpa) tester.createObject(1);
    r2 = (RefsetDescriptorRefsetMemberJpa) tester.createObject(2);
    l1 = (List) new ArrayList();
    l1.add(null);
    l2 = (List) new ArrayList();
    s1 = (Set) new HashSet<>();
    s1.add(Refset.FeedbackEvent.DEFINITION_CHANGE);
    s2 = (Set) new HashSet<>();
    s2.add(Refset.FeedbackEvent.MEMBER_ADD);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet036() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("staged");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode036() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("active");
    tester.include("moduleId");
    tester.include("terminology");
    tester.include("terminologyId");
    // tester.include("version");
    tester.include("definitionClauses");
    tester.include("description");
    tester.include("externalUrl");
    tester.include("forTranslation");
    tester.include("public");
    tester.include("namespace");
    tester.include("domain");
    tester.include("name");
    tester.include("project");
    tester.include("type");
    tester.include("inPublicationProcess");
    tester.include("lookupInProgress");
    tester.include("revision");
    tester.include("localSet");

    // Set up objects
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);
    tester.proxy(RefsetDescriptorRefsetMember.class, 1, r1);
    tester.proxy(RefsetDescriptorRefsetMember.class, 1, r2);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy(Set.class, 1, s1);
    tester.proxy(Set.class, 2, s2);

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
  public void testModelCopy036() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);

    // Set up objects
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);
    tester.proxy(RefsetDescriptorRefsetMember.class, 1, r1);
    tester.proxy(RefsetDescriptorRefsetMember.class, 1, r2);

    assertTrue(tester.testCopyConstructor(Refset.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization036() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);

    // Set up objects
    Project p = new ProjectJpa();
    p.setId(1L);
    tester.proxy(Project.class, 1, p);
    tester.proxy(RefsetDescriptorRefsetMember.class, 1, r1);
    tester.proxy(Set.class, 1, s1);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField036() throws Exception {
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
    tester.include("localSet");
    tester.include("name");
    tester.include("description");
    tester.include("isPublic");
    tester.include("provisional");
    tester.include("type");
    tester.include("forTranslation");
    tester.include("workflowStatus");
    tester.include("refsetDescriptorUuid");
    tester.include("inPublicationProcess");
    tester.include("lookupInProgress");
    tester.include("lookupRequired");
    tester.include("revision");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields036() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    tester.include("name");
    tester.include("description");
    tester.include("userRoleMap");
    tester.include("userAnyRole");
    tester.include("organization");
    
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    assertTrue(tester.testAnalyzedIndexedFields());
    tester = new IndexedFieldTester(object);
    tester.include("id");
    tester.include("localSet");
    tester.include("effectiveTime");
    tester.include("terminology");
    tester.include("version");
    tester.include("terminologyId");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("moduleId");
    tester.include("type");
    tester.include("externalUrl");
    tester.include("workflowStatus");
    tester.include("organizationSort");
    tester.include("nameSort");
    tester.include("public");
    tester.include("provisional");
    tester.include("descriptionsort");

    tester.include("type");
    tester.include("workflowStatus");
    tester.include("projectId");
    tester.include("namespace");
    tester.include("domain");
    tester.include("revision");
    tester.include("active");

    assertTrue(tester.testNotAnalyzedIndexedFields());

  }

  /**
   * Test compute definition
   *
   * @throws Exception the exception
   */
  @Test
  public void testComputeDefinition() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    //
    // Test these cases:
    // <<195967001
    // <<195967001 MINUS <<304527002
    // <<195967001 OR <<304527002
    // (<<195967001 OR <<304527002) MINUS <<370218001
    // (<<195967001 OR <<304527002) MINUS (<<370218001 OR <<389145006)
    // <<195967001 MINUS (<<370218001 OR <<389145006)
    // (<<195967001 OR <<304527002 OR <<370218001) MINUS (<<370218001 OR
    // <<389145006 OR <<195967001)
    //
    // Also test the minus ones with a project exclusion clause
    //

    Project project = new ProjectJpa();
    project.setExclusionClause(null);

    Refset refset = new RefsetJpa();
    refset.setProject(project);

    // <<195967001
    DefinitionClause clause1 = new DefinitionClauseJpa();
    clause1.setValue("<<195967001");
    clause1.setNegated(false);
    refset.getDefinitionClauses().add(clause1);
    assertEquals("<<195967001", refset.computeDefinition(null, null));

    // <<195967001 with project exclusion clause
    project.setExclusionClause("<<304527002");
    assertEquals("<<195967001 MINUS <<304527002",
        refset.computeDefinition(null, null));

    // <<195967001 MINUS <<304527002
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    DefinitionClause clause2 = new DefinitionClauseJpa();
    clause2.setValue("<<304527002");
    clause2.setNegated(true);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    assertEquals("<<195967001 MINUS <<304527002",
        refset.computeDefinition(null, null));

    // Try adding clauses in the reverse order
    refset.getDefinitionClauses().clear();
    refset.getDefinitionClauses().add(clause2);
    refset.getDefinitionClauses().add(clause1);
    assertEquals("<<195967001 MINUS <<304527002",
        refset.computeDefinition(null, null));

    // <<195967001 MINUS <<304527002 with project exclusion clause
    project.setExclusionClause("<<370218001");
    assertEquals("<<195967001 MINUS (<<304527002 OR <<370218001)",
        refset.computeDefinition(null, null));

    // (<<195967001 OR <<304527002) MINUS <<370218001
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    clause2.setNegated(false);
    DefinitionClause clause3 = new DefinitionClauseJpa();
    clause3.setValue("<<370218001");
    clause3.setNegated(true);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    refset.getDefinitionClauses().add(clause3);
    assertEquals("(<<195967001 OR <<304527002) MINUS <<370218001",
        refset.computeDefinition(null, null));

    // (<<195967001 OR <<304527002) MINUS (<<370218001 OR <<389145006)
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    DefinitionClause clause4 = new DefinitionClauseJpa();
    clause4.setValue("<<389145006");
    clause4.setNegated(true);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    refset.getDefinitionClauses().add(clause3);
    refset.getDefinitionClauses().add(clause4);
    assertEquals(
        "(<<195967001 OR <<304527002) MINUS (<<370218001 OR <<389145006)",
        refset.computeDefinition(null, null));

    // (<<195967001 OR <<304527002) MINUS (<<370218001 OR <<389145006) with
    // project exclusion
    project.setExclusionClause("<<12345");
    assertEquals(
        "(<<195967001 OR <<304527002) MINUS (<<370218001 OR <<389145006 OR <<12345)",
        refset.computeDefinition(null, null));

    // <<195967001 MINUS (<<370218001 OR <<389145006)
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause3);
    refset.getDefinitionClauses().add(clause4);
    assertEquals("<<195967001 MINUS (<<370218001 OR <<389145006)",
        refset.computeDefinition(null, null));

    // (<<195967001 OR <<304527002 OR <<370218001) MINUS (<<370218001 OR
    // <<389145006 OR <<195967001)
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    DefinitionClause clause3b = new DefinitionClauseJpa(clause3);
    clause3b.setNegated(false);
    refset.getDefinitionClauses().add(clause3b);
    refset.getDefinitionClauses().add(clause3);
    refset.getDefinitionClauses().add(clause4);
    DefinitionClause clause1b = new DefinitionClauseJpa(clause1);
    clause1b.setNegated(true);
    refset.getDefinitionClauses().add(clause1b);
    assertEquals(
        "(<<195967001 OR <<304527002 OR <<370218001) MINUS (<<370218001 OR <<389145006 OR <<195967001)",
        refset.computeDefinition(null, null));

    // Test role restriction cases too
    // < 19829001 |disorder of lung|: 116676008 |associated morphology| =
    // 79654002 |edema|
    // (< 19829001 |disorder of lung|: 116676008 |associated morphology| =
    // 79654002 |edema|) OR <<409623005 | Respiratory insufficiency (disorder) |
    //

    // < 19829001 |disorder of lung|: 116676008 |associated morphology| =
    // 79654002 |edema|
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    clause1 = new DefinitionClauseJpa();
    clause1.setValue(
        "< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|");
    clause1.setNegated(false);
    refset.getDefinitionClauses().add(clause1);
    assertEquals(
        "< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|",
        refset.computeDefinition(null, null));

    // with project exclusion clause
    project.setExclusionClause("<<304527002");
    assertEquals(
        "(< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|) MINUS <<304527002",
        refset.computeDefinition(null, null));

    // (< 19829001 |disorder of lung|: 116676008 |associated morphology| =
    // 79654002 |edema|) OR <<409623005 | Respiratory insufficiency (disorder) |
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    clause2 = new DefinitionClauseJpa();
    clause2.setValue("<<409623005 | Respiratory insufficiency (disorder) |");
    clause1.setNegated(false);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    assertEquals(
        "(< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|) "
            + "OR <<409623005 | Respiratory insufficiency (disorder) |",
        refset.computeDefinition(null, null));

    // with project exclusion clause
    project.setExclusionClause("<<304527002");
    assertEquals(
        "((< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|) "
            + "OR <<409623005 | Respiratory insufficiency (disorder) |) MINUS <<304527002",
        refset.computeDefinition(null, null));

    // < 19829001 |disorder of lung|: 116676008 |associated morphology| =
    // 79654002 |edema|
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    clause1 = new DefinitionClauseJpa();
    clause1.setValue("< 19829001 : 116676008 = 79654002");
    clause1.setNegated(false);
    refset.getDefinitionClauses().add(clause1);
    assertEquals("< 19829001 : 116676008 = 79654002",
        refset.computeDefinition(null, null));

    // with project exclusion clause
    project.setExclusionClause("<<304527002");
    assertEquals("(< 19829001 : 116676008 = 79654002) MINUS <<304527002",
        refset.computeDefinition(null, null));

    // (< 19829001 |disorder of lung|: 116676008 |associated morphology| =
    // 79654002 |edema|) OR <<409623005 | Respiratory insufficiency (disorder) |
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    clause2 = new DefinitionClauseJpa();
    clause2.setValue("<<409623005");
    clause1.setNegated(false);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    assertEquals("(< 19829001 : 116676008 = 79654002) OR <<409623005",
        refset.computeDefinition(null, null));

    // with project exclusion clause
    project.setExclusionClause("<<304527002");
    assertEquals(
        "((< 19829001 : 116676008 = 79654002) OR <<409623005) MINUS <<304527002",
        refset.computeDefinition(null, null));

    //
    // Now, test compound clauses (containing AND, OR, or MINUS) - wrap with
    // parens.
    // < 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive
    // system (procedure)|
    // OR
    // < 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial
    // pneumonia (disorder)|
    //
    // => (a) OR (b)
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    String a =
        "< 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive system (procedure)|";
    String b =
        "< 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial pneumonia (disorder)|";
    clause1 = new DefinitionClauseJpa();
    clause1.setValue(a);
    clause1.setNegated(false);
    clause2 = new DefinitionClauseJpa();
    clause2.setValue(b);
    clause2.setNegated(false);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    assertEquals("(" + a + ") OR (" + b + ")",
        refset.computeDefinition(null, null));

    //
    // Now, test compound clauses (containing AND, OR, or MINUS) - wrap with
    // parens.
    // < 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive
    // system (procedure)|
    // MINUS
    // < 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial
    // pneumonia (disorder)|
    //
    // => (a) minus (b)
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    a = "< 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive system (procedure)|";
    b = "< 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial pneumonia (disorder)|";
    clause1 = new DefinitionClauseJpa();
    clause1.setValue(a);
    clause1.setNegated(false);
    clause2 = new DefinitionClauseJpa();
    clause2.setValue(b);
    clause2.setNegated(true);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    assertEquals("(" + a + ") MINUS (" + b + ")",
        refset.computeDefinition(null, null));

    //
    // Now, test compound clauses (containing AND, OR, or MINUS) - wrap with
    // parens.
    // 65801008
    // < 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive
    // system (procedure)|
    // < 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial
    // pneumonia (disorder)|
    //
    // => a or (b) or (c)
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    a = "65801008";
    b = "< 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive system (procedure)|";
    String c =
        "< 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial pneumonia (disorder)|";
    clause1 = new DefinitionClauseJpa();
    clause1.setValue(a);
    clause1.setNegated(false);
    clause2 = new DefinitionClauseJpa();
    clause2.setValue(b);
    clause2.setNegated(false);
    clause3 = new DefinitionClauseJpa();
    clause3.setValue(c);
    clause3.setNegated(false);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    refset.getDefinitionClauses().add(clause3);

    assertEquals(a + " OR (" + b + ") OR (" + c + ")",
        refset.computeDefinition(null, null));

    //
    // Now, test compound clauses (containing AND, OR, or MINUS) - wrap with
    // parens.
    // 65801008
    // < 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive
    // system (procedure)|
    // MINUS
    // < 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial
    // pneumonia (disorder)|
    // 233604007
    //
    // => (a or (b)) minus ((c) or d)
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    a = "65801008";
    b = "< 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive system (procedure)|";
    c = "< 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial pneumonia (disorder)|";
    String d = "233604007";
    clause1 = new DefinitionClauseJpa();
    clause1.setValue(a);
    clause1.setNegated(false);
    clause2 = new DefinitionClauseJpa();
    clause2.setValue(b);
    clause2.setNegated(false);
    clause3 = new DefinitionClauseJpa();
    clause3.setValue(c);
    clause3.setNegated(true);
    clause4 = new DefinitionClauseJpa();
    clause4.setValue(d);
    clause4.setNegated(true);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    refset.getDefinitionClauses().add(clause3);
    refset.getDefinitionClauses().add(clause4);
    assertEquals("(" + a + " OR (" + b + ")) MINUS ((" + c + ") OR " + d + ")",
        refset.computeDefinition(null, null)); // TODO: test

    //
    // Now, test compound clauses (containing AND, OR, or MINUS) - wrap with
    // parens.
    // < 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive
    // system (procedure)|
    // < 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive
    // system (procedure)|
    // MINUS
    // < 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial
    // pneumonia (disorder)|
    // < 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial
    // pneumonia (disorder)|
    //
    // => ((a) or (b)) minus ((c) or (d))
    refset.getDefinitionClauses().clear();
    project.setExclusionClause(null);
    a = "< 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive system (procedure)|";
    b = a;
    c = "< 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial pneumonia (disorder)|";
    d = c;
    clause1 = new DefinitionClauseJpa();
    clause1.setValue(a);
    clause1.setNegated(false);
    clause2 = new DefinitionClauseJpa();
    clause2.setValue(b);
    clause2.setNegated(false);
    clause3 = new DefinitionClauseJpa();
    clause3.setValue(c);
    clause3.setNegated(true);
    clause4 = new DefinitionClauseJpa();
    clause4.setValue(d);
    clause4.setNegated(true);
    refset.getDefinitionClauses().add(clause1);
    refset.getDefinitionClauses().add(clause2);
    refset.getDefinitionClauses().add(clause3);
    refset.getDefinitionClauses().add(clause4);
    assertEquals(
        "((" + a + ") OR (" + b + ")) MINUS ((" + c + ") OR (" + d + "))",
        refset.computeDefinition(null, null)); // TODO: test

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
