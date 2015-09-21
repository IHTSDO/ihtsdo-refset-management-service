/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.HistoryServiceRest;
import org.ihtsdo.otf.refset.services.SecurityService;

import com.wordnik.swagger.annotations.Api;

/**
 * REST implementation for {@link HistoryServiceRest}.
 */
@Path("/history")
@Api(value = "/history", description = "Operations for accessing prior editions of domain model objects and interacting with release info.")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class HistoryServiceRestImpl extends RootServiceRestImpl implements
    HistoryServiceRest {

  /** The security service. */
  @SuppressWarnings("unused")
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link HistoryServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public HistoryServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  // TODO: need impl

}
