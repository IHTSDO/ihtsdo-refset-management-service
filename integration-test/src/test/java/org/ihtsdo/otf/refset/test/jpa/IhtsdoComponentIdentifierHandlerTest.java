/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.jpa;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.DefaultTerminologyHandler;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Some initial testing for {@link DefaultTerminologyHandler}. Assumes stock dev
 * load.
 */
public class IhtsdoComponentIdentifierHandlerTest extends JpaSupport {

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    // do nothing
  }

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // n/a
  }

  /**
   * Test getting descriptions from Snow Owl.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetAssignIds() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();
    Project project = new ProjectJpa();
    project.setNamespace("1000124");
    Refset refset = new RefsetJpa();
    refset.setProject(project);
    Translation translation = new TranslationJpa();
    translation.setProject(project);
    translation.setRefset(refset);
    Concept concept = new ConceptJpa();
    concept.setTranslation(translation);
    Description description = new DescriptionJpa();
    description.setConcept(concept);
    String terminologyId =
        service.getIdentifierAssignmentHandler("DEFAULT").getTerminologyId(
            concept);
    Logger.getLogger(getClass()).info("  concept = " + terminologyId);

    terminologyId =
        service.getIdentifierAssignmentHandler("DEFAULT").getTerminologyId(
            description);
    Logger.getLogger(getClass()).info("  description = " + terminologyId);

    terminologyId =
        service.getIdentifierAssignmentHandler("DEFAULT").getTerminologyId(
            refset);
    Logger.getLogger(getClass()).info("  refset = " + terminologyId);

    terminologyId =
        service.getIdentifierAssignmentHandler("DEFAULT").getTerminologyId(
            translation);
    Logger.getLogger(getClass()).info("  translation = " + terminologyId);

    terminologyId =
        service.getIdentifierAssignmentHandler("DEFAULT").getTerminologyId(
            new ConceptRefsetMemberJpa());
    Logger.getLogger(getClass()).info(
        "  concept refset member = " + terminologyId);

    terminologyId =
        service.getIdentifierAssignmentHandler("DEFAULT").getTerminologyId(
            new LanguageRefsetMemberJpa());
    Logger.getLogger(getClass()).info(
        "  language refset member = " + terminologyId);

    service.close();
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // n/a
  }

  /**
   * Teardown class.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void teardownClass() throws Exception {
    // do nothing
  }

}
