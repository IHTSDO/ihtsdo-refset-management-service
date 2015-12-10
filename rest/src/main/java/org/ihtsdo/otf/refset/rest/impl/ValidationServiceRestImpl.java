/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.ConceptValidationResult;
import org.ihtsdo.otf.refset.MemberValidationResult;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConceptValidationResultList;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.MemberValidationResultList;
import org.ihtsdo.otf.refset.jpa.ConceptValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.MemberValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptValidationResultListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.MemberValidationResultListJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.ValidationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ValidationServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.ValidationService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link ValidationServiceRest}.
 */
@Path("/validate")
@Api(value = "/validate", description = "Operations providing terminology validation")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class ValidationServiceRestImpl extends RootServiceRestImpl implements
    ValidationServiceRest {

  /** The security service. */
  private SecurityService securityService;
  
  /** The project service. */
  private ProjectService projectService;

  /**
   * Instantiates an empty {@link ValidationServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ValidationServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
    projectService = new ProjectServiceJpa();
  }

  /* see superclass */
  @Override
  @POST
  @Path("/concept")
  @ApiOperation(value = "Validate Concept", notes = "Validates a concept", response = ValidationResult.class)
  public ValidationResult validateConcept(
    @ApiParam(value = "Concept", required = true) ConceptJpa concept,
    @ApiParam(value = "Project id, e.g. 8", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /concept " + concept);

    ValidationService validationService = new ValidationServiceJpa();
    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "validate concept", UserRole.AUTHOR);

      Project project = projectService.getProject(projectId);
      return validationService.validateConcept(concept, project, translationService);
    } catch (Exception e) {
      handleException(e, "trying to validate concept");
      return null;
    } finally {
      validationService.close();
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/refset/{projectId}")
  @ApiOperation(value = "Validate Refset", notes = "Validates a refset", response = ValidationResult.class)
  public ValidationResult validateRefset(
    @ApiParam(value = "Refset", required = true) RefsetJpa refset,
    @ApiParam(value = "Project id, e.g. 8", required = false) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /refset " + refset);
    ValidationService validationService = new ValidationServiceJpa();
    RefsetService refsetService = new RefsetServiceJpa();

    try {
      authorizeProject(projectService, refset.getProjectId(), securityService, authToken,
          "validate a refset", UserRole.AUTHOR);

      Project project = projectService.getProject(projectId);
      return validationService.validateRefset(refset, project, refsetService);
    } catch (Exception e) {
      e.printStackTrace();
      handleException(e, "trying to validate refset");
      return null;
    } finally {
      validationService.close();
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation")
  @ApiOperation(value = "Validate Translation", notes = "Validates a translation", response = ValidationResult.class)
  public ValidationResult validateTranslation(
    @ApiParam(value = "Translation", required = true) TranslationJpa translation,
    @ApiParam(value = "Project id, e.g. 8", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /translation " + translation);

    ValidationService validationService = new ValidationServiceJpa();
    TranslationService translationService = new TranslationServiceJpa();

    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "validate translation", UserRole.AUTHOR);

      Project project = projectService.getProject(projectId);
      return validationService.validateTranslation(translation,
          project, translationService);
    } catch (Exception e) {
      handleException(e, "trying to validate translation");
      return null;
    } finally {
      validationService.close();
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/member")
  @ApiOperation(value = "Validate refset member", notes = "Validates a refset member", response = ValidationResult.class)
  public ValidationResult validateMember(
    @ApiParam(value = "Refset member", required = true) ConceptRefsetMemberJpa member,
    @ApiParam(value = "Project id, e.g. 8", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /member " + member);

    ValidationService validationService = new ValidationServiceJpa();
    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "validate member", UserRole.AUTHOR);
      
      Project project = projectService.getProject(projectId);      
      return validationService.validateMember(member, project, refsetService);
    } catch (Exception e) {
      handleException(e, "trying to validate refset member");
      return null;
    } finally {
      validationService.close();
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  /* see superclass */
  @Override
  @GET
  @Path("/concepts")
  @ApiOperation(value = "Validate all translation concept member", notes = "Validates all of the concept members of the specified translation", response = ConceptValidationResultListJpa.class)
  public ConceptValidationResultList validateAllConcepts(
    @ApiParam(value = "Translation id, e.g. 10", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /concepts " + translationId);

    ValidationService validationService = new ValidationServiceJpa();
    TranslationService translationService = new TranslationServiceJpa();
    try {
      Translation translation = translationService.getTranslation(translationId);
      authorizeProject(projectService, translation.getProject().getId(), securityService, authToken,
          "validate all concepts", UserRole.AUTHOR);
      
      authorizeApp(securityService, authToken, "validate all concepts",
          UserRole.ADMIN);

      ConceptValidationResultList list = new ConceptValidationResultListJpa();
      ConceptList concepts =
          translationService.findConceptsForTranslation(translationId, null,
              null);
      for (Concept concept : concepts.getObjects()) {
        ValidationResult result =
            validationService.validateConcept(concept, null, translationService);
        if (!result.isValid()) {
          ConceptValidationResult cvr = new ConceptValidationResultJpa(result);
          cvr.setConcept(concept);
          list.getObjects().add(cvr);
        }
      }
      list.setTotalCount(list.getCount());
      return list;
    } catch (Exception e) {
      handleException(e, "trying to validate all concept");
      return null;
    } finally {
      validationService.close();
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/members")
  @ApiOperation(value = "Validate all refset members", notes = "Validates all of the members of the specified refset", response = MemberValidationResultListJpa.class)
  public MemberValidationResultList validateAllMembers(
    @ApiParam(value = "Refset id, e.g. 10", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /members " + refsetId);

    ValidationService validationService = new ValidationServiceJpa();
    RefsetService refsetService = new RefsetServiceJpa();
    try {
      Refset refset = refsetService.getRefset(refsetId);
      authorizeProject(projectService, refset.getProject().getId(), securityService, authToken,
          "validate all members", UserRole.AUTHOR);

      MemberValidationResultList list = new MemberValidationResultListJpa();
      ConceptRefsetMemberList members =
          refsetService.findMembersForRefset(refsetId, null, null);
      for (ConceptRefsetMember member : members.getObjects()) {
        ValidationResult result =
            validationService.validateMember(member, null, refsetService);
        if (!result.isValid()) {
          MemberValidationResult mvr = new MemberValidationResultJpa(result);
          mvr.setMember(member);
          list.getObjects().add(mvr);
        }
      }
      list.setTotalCount(list.getCount());
      return list;
    } catch (Exception e) {
      handleException(e, "trying to validate all concept");
      return null;
    } finally {
      refsetService.close();
      validationService.close();
      securityService.close();
    }
  }
  /* see superclass */
  @Override
  @GET
  @Path("/checks")
  @ApiOperation(value = "Gets all validation checks", notes = "Gets all validation checks", response = KeyValuePairList.class)
  public KeyValuePairList getValidationChecks(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /checks ");

    ValidationService validationService = new ValidationServiceJpa();
    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get validation checks",
          UserRole.VIEWER);
      
      KeyValuePairList list = validationService.getValidationCheckNames();
      return list;
    } catch (Exception e) {
      handleException(e, "trying to validate all concept");
      return null;
    } finally {
      refsetService.close();
      validationService.close();
      securityService.close();
    }
  }

}
