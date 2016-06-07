/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.refset.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ProjectServiceRest;
import org.ihtsdo.otf.refset.jpa.services.rest.RefsetServiceRest;
import org.ihtsdo.otf.refset.jpa.services.rest.ReleaseServiceRest;
import org.ihtsdo.otf.refset.jpa.services.rest.SecurityServiceRest;
import org.ihtsdo.otf.refset.jpa.services.rest.TranslationServiceRest;
import org.ihtsdo.otf.refset.jpa.services.rest.ValidationServiceRest;
import org.ihtsdo.otf.refset.rest.impl.ProjectServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.RefsetServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.ReleaseServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.SecurityServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.TranslationServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.ValidationServiceRestImpl;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Goal which generates PROD data in an empty database. Uses JPA services
 * directly, no need for REST layer.
 * 
 * See admin/pom.xml for sample usage
 * 
 * @goal prod-data
 * @phase package
 */
@Mojo( name = "prod-data", defaultPhase = LifecyclePhase.PACKAGE )
public class GenerateProdDataMojo extends AbstractMojo {

  /**
   * Mode - for recreating db.
   *
   * @parameter
   */
  private String mode = null;

  /**
   * Instantiates a {@link GenerateSampleDataMojo} from the specified
   * parameters.
   */
  public GenerateProdDataMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    try {
      getLog().info("Generate sample data");
      getLog().info("  mode = " + mode);

      // Handle creating the database if the mode parameter is set
      Properties properties = ConfigUtility.getConfigProperties();
      if (mode != null && mode.equals("create")) {
        getLog().info("Recreate database");
        // This will trigger a rebuild of the db
        properties.setProperty("hibernate.hbm2ddl.auto", mode);
        // Trigger a JPA event
        new ProjectServiceJpa().close();
        properties.remove("hibernate.hbm2ddl.auto");
      }

      // force lookups not in background
      properties.setProperty("lookup.background", "false");
      // force "assignNames" to true
      properties.setProperty("terminology.handler.DEFAULT.assignNames", "true");

      // authenticate
      SecurityService service = new SecurityServiceJpa();
      String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      // Handle reindexing database if mode is set
      if (mode != null && mode.equals("create")) {
        ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
        contentService.luceneReindex(null, authToken);
      }

      boolean serverRunning = ConfigUtility.isServerActive();
      getLog()
          .info("Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));
      if (serverRunning) {
        throw new Exception("Server must not be running to generate data");
      }

      // Load the data
      loadProdData();

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }

  }

  /**
   * Load sample data.
   *
   * @throws Exception the exception
   */
  private void loadProdData() throws Exception {

    try {

      // Initialize
      getLog().info("Authenticate admin user");
      SecurityServiceRest security = new SecurityServiceRestImpl();
      ProjectServiceRest project = new ProjectServiceRestImpl();
      User admin = security.authenticate("admin", "admin");

      //
      // Add "prod" users
      //
      final BufferedReader in = new BufferedReader(new FileReader(
          new File("../config/prod/src/main/resources/users.txt")));
      String line;
      while ((line = in.readLine()) != null) {
        final String[] tokens = FieldedStringTokenizer.split(line, "|");
        // Sample line:
        // USER|***REMOVED***|Brian Carlsen|bcarlsen|
        final UserJpa user = new UserJpa();
        user.setApplicationRole(UserRole.valueOf(tokens[0]));
        user.setEmail(tokens[1]);
        user.setName(tokens[2]);
        user.setUserName(tokens[3]);
        getLog().info("   Add " + user);
        security = new SecurityServiceRestImpl();
        security.addUser(user, admin.getAuthToken());
      }
      in.close();

      //
      // Add Projects (say one for each NRC)
      // TODO: need info from ***REMOVED***
      //
      getLog().info("Add new projects");
      ProjectJpa project1 = makeProject("IHTSDO Project", null, admin);

      //
      // Assign project roles
      //
      getLog().info("Assign users to projects");

      // IHTSDO Project
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(), "rdavidson",
          UserRole.ADMIN.toString(), admin.getAuthToken());
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(), "bcarlsen",
          UserRole.ADMIN.toString(), admin.getAuthToken());
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(), "dshapiro",
          UserRole.ADMIN.toString(), admin.getAuthToken());

      /**
       * Add Refsets and translations
       * 
       * <pre>
       *  - Project 1 - IHTSDO
       *     - Refset 1: extensional GP/FP
       *     - Refset 2: extensional Spanish Translation
       * </pre>
       */

      // Create IHTSDO refsets
      getLog().info("Create IHTSDO refsets");

      // GP/FP
      RefsetJpa gpfp =
          makeRefset("General Practice / Family Practice reference set", null,
              "gpfp", "xder2_Refset_GPFPSimpleSnapshot_INT_20150930.txt",
              "en-edition", "20150731", Refset.Type.EXTENSIONAL, project1,
              "450970008", null, "900000000000207008", false, admin);

      // RELEASE refsets -- good test of release process.
      getLog().info("Release IHTSDO refsets");
      releaseRefset(gpfp, "20150930", true, admin);

      // Create IHTSDO Translations
      getLog().info("Create IHTSDO translations");

      // Spanish edition
      // No members needed, just load the translation
      RefsetJpa sctspa = makeRefset("SNOMED CT Spanish Edition", null, null,
          null, "en-edition", "20160131", Refset.Type.EXTENSIONAL, project1,
          null, null, "449081005", false, admin);

      // Create spanish translation
      TranslationJpa sctspaTranslation = makeTranslation(
          "SNOMED CT Spanish Edition", sctspa.getTerminologyId(), sctspa,
          project1, "sctspa", "SnomedSpanishTranslation.zip", "es", admin);

      // RELEASE translations
      getLog().info("Release IHTSDO translations");
      releaseTranslation(sctspaTranslation, "20151031", true, admin);

      getLog().info("Done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  /**
   * Make project.
   *
   * @param name the name
   * @param namespaceId the namespace id
   * @param auth the auth
   * @return the project jpa
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private ProjectJpa makeProject(String name, String namespaceId, User auth)
    throws Exception {
    final ProjectJpa project = new ProjectJpa();
    project.setName(name);
    project.setDescription("Description of project " + name);
    project.setLastModified(new Date());
    project.setTerminology("en-edition");
    project.setTerminologyId("");
    project.setVersion("");
    project.setOrganization("IHTSDO");
    // This is the only namespace configured in the sample id generation service
    // when there are others, we can play with this
    project.setNamespace(namespaceId);
    return (ProjectJpa) new ProjectServiceRestImpl().addProject(project,
        auth.getAuthToken());
  }

  /**
   * Make refset.
   *
   * @param name the name
   * @param definition the definition
   * @param dir the dir
   * @param file the file
   * @param edition the edition
   * @param version the version
   * @param type the type
   * @param project the project
   * @param refsetId the refset id
   * @param namespaceId the namespace id
   * @param moduleId the module id
   * @param forTranslation the for translation
   * @param auth the auth
   * @return the refset jpa
   * @throws Exception the exception
   */
  private RefsetJpa makeRefset(String name, String definition, String dir,
    String file, String edition, String version, Refset.Type type,
    Project project, String refsetId, String namespaceId, String moduleId,
    boolean forTranslation, User auth) throws Exception {
    getLog().info("  refset = " + name);
    final RefsetJpa refset = new RefsetJpa();
    refset.setActive(true);
    refset.setType(type);
    refset.setName(name);
    refset.setDescription("Description of refset " + name);
    if (definition != null && type == Refset.Type.INTENSIONAL) {
      List<DefinitionClause> definitionClauses =
          new ArrayList<DefinitionClause>();
      DefinitionClause clause = new DefinitionClauseJpa();
      clause.setValue(definition);
      clause.setNegated(false);
      definitionClauses.add(clause);
      refset.setDefinitionClauses(definitionClauses);
    }
    refset.setExternalUrl(null);
    refset.setFeedbackEmail("techsupport@ihtsdo.org");
    refset.getEnabledFeedbackEvents().add(FeedbackEvent.MEMBER_ADD);
    refset.getEnabledFeedbackEvents().add(FeedbackEvent.MEMBER_REMOVE);
    refset.setForTranslation(forTranslation);
    refset.setLastModified(new Date());
    refset.setLookupInProgress(false);
    refset.setNamespace(namespaceId);
    refset.setModuleId(moduleId);
    refset.setProject(project);
    refset.setPublishable(true);
    refset.setPublished(true);
    refset.setInPublicationProcess(false);
    refset.setTerminology(edition);
    refset.setTerminologyId(refsetId);
    refset.setEffectiveTime(null);
    // This is an opportunity to use "branch"
    refset.setVersion(version);
    refset.setWorkflowPath("DEFAULT");
    refset.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    RefsetServiceRest refsetService = new RefsetServiceRestImpl();

    ValidationServiceRest validation = new ValidationServiceRestImpl();

    // Validate refset
    ValidationResult result = validation.validateRefset(refset,
        refset.getProject().getId(), auth.getAuthToken());
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset
    refsetService.addRefset(refset, auth.getAuthToken());
    refsetService = new RefsetServiceRestImpl();

    // Handle importing a refset file
    if (type == Refset.Type.EXTENSIONAL && file != null
        && file.contains("Refset")) {
      // Import members (from file)
      ValidationResult vr = refsetService.beginImportMembers(refset.getId(),
          "DEFAULT", auth.getAuthToken());
      if (!vr.isValid()) {
        throw new Exception("import staging is invalid - " + vr);
      }
      refsetService = new RefsetServiceRestImpl();
      InputStream in = new FileInputStream(
          new File("../config/src/main/resources/data/" + dir + "/" + file));
      refsetService.finishImportMembers(null, in, refset.getId(), "DEFAULT",
          auth.getAuthToken());
      in.close();
    }

    // Otherwise, assume a file of ids, and add each member
    else if (type == Refset.Type.EXTENSIONAL && file != null) {
      final BufferedReader in = new BufferedReader(new FileReader(
          new File("../config/src/main/resources/data/" + dir, file)));
      String line;
      while ((line = in.readLine()) != null) {
        line = line.replace("\r", "");
        // Create and add a member for each id
        ConceptRefsetMemberJpa member = new ConceptRefsetMemberJpa();
        member.setActive(true);
        member.setConceptId(line);
        member.setEffectiveTime(null);
        member.setMemberType(Refset.MemberType.MEMBER);
        member.setModuleId(moduleId);
        member.setPublished(false);
        member.setPublishable(true);
        member.setRefset(refset);
        refsetService = new RefsetServiceRestImpl();
        refsetService.addRefsetMember(member, auth.getAuthToken());
      }
      in.close();
    }

    return (RefsetJpa) new RefsetServiceRestImpl().getRefset(refset.getId(),
        auth.getAuthToken());
  }

  /**
   * Release refset.
   *
   * @param refset the refset
   * @param effectiveTime the effective time
   * @param finishFlag the finish flag
   * @param auth the auth
   * @return the refset jpa
   * @throws Exception the exception
   */
  public RefsetJpa releaseRefset(Refset refset, String effectiveTime,
    boolean finishFlag, User auth) throws Exception {
    ReleaseServiceRest releaseService = new ReleaseServiceRestImpl();
    getLog().info("  refset = " + refset.getName());
    // Begin release
    getLog().info("    begin");
    releaseService.beginRefsetRelease(refset.getId(), effectiveTime,
        auth.getAuthToken());
    // Validate release
    getLog().info("    validate");
    releaseService = new ReleaseServiceRestImpl();
    releaseService.validateRefsetRelease(refset.getId(), auth.getAuthToken());
    // Beta release
    getLog().info("    beta");
    releaseService = new ReleaseServiceRestImpl();
    releaseService.betaRefsetRelease(refset.getId(), "DEFAULT",
        auth.getAuthToken());

    // Finish release
    if (finishFlag) {
      getLog().info("  finish");
      releaseService = new ReleaseServiceRestImpl();
      releaseService.finishRefsetRelease(refset.getId(), auth.getAuthToken());
    }

    return (RefsetJpa) new RefsetServiceRestImpl().getRefset(refset.getId(),
        auth.getAuthToken());
  }

  /**
   * Make translation.
   *
   * @param name the name
   * @param terminologyId the terminology id
   * @param refset the refset
   * @param project the project
   * @param dir the dir
   * @param file the file
   * @param lang the lang
   * @param auth the auth
   * @return the translation jpa
   * @throws Exception the exception
   */
  private TranslationJpa makeTranslation(String name, String terminologyId,
    Refset refset, Project project, String dir, String file, String lang,
    User auth) throws Exception {
    getLog().info("  translation = " + name);
    final TranslationJpa translation = new TranslationJpa();
    translation.setName(name);
    translation
        .setDescription("Description of translation " + translation.getName());
    translation.setActive(true);
    translation.setEffectiveTime(null);
    translation.setLanguage(lang);
    translation.setModuleId(refset.getModuleId());
    translation.setProject(project);
    translation.setPublic(true);
    translation.setPublishable(true);
    translation.setRefset(refset);
    translation.setTerminology(refset.getTerminology());
    translation.setVersion(refset.getVersion());
    translation.setTerminologyId(terminologyId);
    translation.setWorkflowPath("DEFAULT");
    translation.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    translation.setEffectiveTime(null);
    TranslationServiceRest translationService =
        new TranslationServiceRestImpl();
    // Add translation
    translationService.addTranslation(translation, auth.getAuthToken());

    // Import members (from file) - switch file based on counter
    translationService = new TranslationServiceRestImpl();
    ValidationResult vr = translationService.beginImportConcepts(
        translation.getId(), "DEFAULT", auth.getAuthToken());
    if (!vr.isValid()) {
      throw new Exception("translation staging is not valid - " + vr);
    }
    translationService = new TranslationServiceRestImpl();
    InputStream in = new FileInputStream(
        new File("../config/src/main/resources/data/" + dir + "/" + file));
    translationService.finishImportConcepts(null, in, translation.getId(),
        "DEFAULT", auth.getAuthToken());
    in.close();

    final TranslationJpa retval =
        (TranslationJpa) new TranslationServiceRestImpl()
            .getTranslation(translation.getId(), auth.getAuthToken());
    retval.setSpellingDictionary(null);
    retval.setPhraseMemory(null);
    return retval;
  }

  /**
   * Release translation.
   *
   * @param translation the translation
   * @param effectiveTime the effective time
   * @param finishFlag the finish flag
   * @param auth the auth
   * @return the translation jpa
   * @throws Exception the exception
   */
  public TranslationJpa releaseTranslation(Translation translation,
    String effectiveTime, boolean finishFlag, User auth) throws Exception {
    ReleaseServiceRest releaseService = new ReleaseServiceRestImpl();
    getLog().info("  translation = " + translation.getName());
    // Begin release
    getLog().info("    begin");
    releaseService.beginTranslationRelease(translation.getId(), effectiveTime,
        auth.getAuthToken());
    // Validate release
    getLog().info("    validate");
    releaseService = new ReleaseServiceRestImpl();
    releaseService.validateTranslationRelease(translation.getId(),
        auth.getAuthToken());
    // Beta release
    getLog().info("    beta");
    releaseService = new ReleaseServiceRestImpl();
    releaseService.betaTranslationRelease(translation.getId(), "DEFAULT",
        auth.getAuthToken());

    // Finish release
    if (finishFlag) {
      getLog().info("  finish");
      releaseService = new ReleaseServiceRestImpl();
      releaseService.finishTranslationRelease(translation.getId(),
          auth.getAuthToken());
    }

    return (TranslationJpa) new TranslationServiceRestImpl()
        .getTranslation(translation.getId(), auth.getAuthToken());
  }

  /**
   * Redefine.
   *
   * @param refset the refset
   * @param definition the definition
   * @param auth the auth
   * @return the refset
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public Refset redefine(Refset refset, String definition, User auth)
    throws Exception {
    RefsetServiceRest refsetService = new RefsetServiceRestImpl();
    DefinitionClause clause = new DefinitionClauseJpa();
    clause.setValue(definition);
    clause.setNegated(false);
    refset.getDefinitionClauses().add(clause);
    refsetService.updateRefset((RefsetJpa) refset, auth.getAuthToken());
    refsetService = new RefsetServiceRestImpl();
    return refsetService.getRefset(refset.getId(), auth.getAuthToken());
  }

  /**
   * Migrate.
   *
   * @param refset the refset
   * @param auth the auth
   * @return the refset
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public Refset migrate(Refset refset, User auth) throws Exception {
    RefsetServiceRest refsetService = new RefsetServiceRestImpl();
    refsetService.beginMigration(refset.getId(), "en-edition", "20150731",
        auth.getAuthToken());
    refsetService = new RefsetServiceRestImpl();
    refsetService.finishMigration(refset.getId(), auth.getAuthToken());
    refsetService = new RefsetServiceRestImpl();
    return refsetService.getRefset(refset.getId(), auth.getAuthToken());
  }
}
