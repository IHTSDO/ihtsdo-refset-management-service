/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ValidationServiceRest;
import org.ihtsdo.otf.refset.services.SecurityService;

import com.wordnik.swagger.annotations.Api;

/**
 * REST implementation for {@link ValidationServiceRest}.
 */
@Path("/validation")
@Api(value = "/validation", description = "Operations providing terminology validation")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class ValidationServiceRestImpl extends RootServiceRestImpl implements
    ValidationServiceRest {

  /** The security service. */
  @SuppressWarnings("unused")
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link ValidationServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ValidationServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  // TODO: need impl

}
