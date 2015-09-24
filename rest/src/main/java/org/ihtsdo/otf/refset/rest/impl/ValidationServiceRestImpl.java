/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.ValidationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ValidationServiceRest;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.SecurityService;
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

  /**
   * Instantiates an empty {@link ValidationServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ValidationServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  @Override
  @POST
  @Path("/concept")
  @ApiOperation(value = "Validate Concept", notes = "Validates a concept", response = ValidationResult.class)
  public ValidationResult validateConcept(
    @ApiParam(value = "Concept", required = true) ConceptJpa concept,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /concept " + concept);

    ValidationService validationService = new ValidationServiceJpa();
    try {
      authenticate(securityService, authToken, "validate concept",
          UserRole.VIEWER);

      return validationService.validateConcept(concept);
    } catch (Exception e) {
      handleException(e, "trying to validate concept");
      return null;
    } finally {
      validationService.close();
      securityService.close();
    }

  }

  @Override
  @POST
  @Path("/refset")
  @ApiOperation(value = "Validate Refset", notes = "Validates a refset", response = ValidationResult.class)
  public ValidationResult validateRefset(
    @ApiParam(value = "Refset", required = true) RefsetJpa refset,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /refset " + refset);

    ValidationService validationService = new ValidationServiceJpa();
    try {
      authenticate(securityService, authToken, "validate refset",
          UserRole.VIEWER);

      return validationService.validateRefset(refset);
    } catch (Exception e) {
      handleException(e, "trying to validate refset");
      return null;
    } finally {
      validationService.close();
      securityService.close();
    }
  }
  
  @Override
  @POST
  @Path("/translation")
  @ApiOperation(value = "Validate Translation", notes = "Validates a translation", response = ValidationResult.class)
  public ValidationResult validateTranslation(
    @ApiParam(value = "Translation", required = true) TranslationJpa translation,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /translation " + translation);

    ValidationService validationService = new ValidationServiceJpa();
    try {
      authenticate(securityService, authToken, "validate translation",
          UserRole.VIEWER);

      return validationService.validateTranslation(translation);
    } catch (Exception e) {
      handleException(e, "trying to validate translation");
      return null;
    } finally {
      validationService.close();
      securityService.close();
    }

  }
  
  @Override
  @POST
  @Path("/member")
  @ApiOperation(value = "Validate SimpleRefsetMember", notes = "Validates a simpleRefSetMember", response = ValidationResult.class)
  public ValidationResult validateSimpleRefsetMember(
    @ApiParam(value = "SimpleRefsetMember", required = true) SimpleRefsetMemberJpa member,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /member " + member);

    ValidationService validationService = new ValidationServiceJpa();
    try {
      authenticate(securityService, authToken, "validate member",
          UserRole.VIEWER);

      return validationService.validateMember(member);
    } catch (Exception e) {
      handleException(e, "trying to validate simpleRefSetMember");
      return null;
    } finally {
      validationService.close();
      securityService.close();
    }

  }
}
