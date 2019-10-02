/*
 *    Copyright 2019 West Coast Informatics, LLC
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

import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ReleaseServiceRest;
import org.ihtsdo.otf.refset.jpa.services.rest.TranslationServiceRest;
import org.ihtsdo.otf.refset.rest.client.ReleaseClientRest;
import org.ihtsdo.otf.refset.rest.client.TranslationClientRest;
import org.ihtsdo.otf.refset.rest.impl.ReleaseServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.TranslationServiceRestImpl;
import org.ihtsdo.otf.refset.services.SecurityService;

/**
 * Goal for processing a translation release (e.g. for SNOMED spanish).
 * 
 * See admin/pom.xml for sample usage
 * 
 * @goal release-translation
 * @phase package
 */
public class ReleaseTranslationMojo extends AbstractRttMojo {

  /**
   * Translation id.
   *
   * @required
   * @parameter
   */
  private Long translationId;

  /**
   * Effective time - YYYYMMDD.
   *
   * @required 
   * @parameter 
   */
  private String effectiveTime;

  /**
   * Exporter key.
   *
   * @required
   * @parameter
   */
  private String exporter;

  /**
   * Instantiates an empty {@link ReleaseTranslationMojo}.
   */
  public ReleaseTranslationMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {
      getLog().info("Release Translation Import");
      getLog().info("  translationId = " + translationId);
      getLog().info("  effectiveTime = " + effectiveTime);
      getLog().info("  exporter = " + exporter);
      
      setupBindInfoPackage();

      final Properties properties = ConfigUtility.getConfigProperties();
      // Ensure name lookups are not in the background...
      properties.setProperty("lookup.background", "false");

      // Validate effective time parameter
      try {
        ConfigUtility.DATE_FORMAT.parse(effectiveTime);
      } catch (Exception e) {
        throw new Exception("Invalid effective time format");
      }

      // Verify server is not up
      boolean serverRunning = ConfigUtility.isServerActive();
      getLog()
          .info("Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));

      // if (serverRunning) {
      // throw new Exception(
      // "Server must not be running to perform delta translation import");
      // }

      // Authenticate
      final SecurityService service = new SecurityServiceJpa();
      final String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      if (!serverRunning) {
        TranslationServiceRest translationService =
            new TranslationServiceRestImpl();

        // Get IO handler id
        String ioHandlerInfoId = null;
        for (final IoHandlerInfo info : translationService
            .getExportTranslationHandlers(authToken).getObjects()) {
          if (info.getId().equals(exporter)) {
            ioHandlerInfoId = info.getId();
          }
        }
        if (ioHandlerInfoId == null) {
          throw new Exception("Unable to find desired release exporter");
        }

        // begin release
        getLog().info("  Begin translation release");
        ReleaseServiceRest releaseService = new ReleaseServiceRestImpl();
        releaseService.beginTranslationRelease(translationId, ioHandlerInfoId,
            authToken);

        // validate release
        getLog().info("  Validate translation release");
        releaseService = new ReleaseServiceRestImpl();
        releaseService.validateTranslationRelease(translationId, authToken);

        // beta release
        getLog().info("  Beta translation release");
        releaseService = new ReleaseServiceRestImpl();
        releaseService.betaTranslationRelease(translationId, ioHandlerInfoId,
            authToken);

        // finish release
        getLog().info("  Finish translation release");
        releaseService = new ReleaseServiceRestImpl();
        releaseService.finishTranslationRelease(translationId, authToken);

      } else {

        TranslationClientRest translationService =
            new TranslationClientRest(properties);

        // Get IO handler id
        String ioHandlerInfoId = null;
        for (final IoHandlerInfo info : translationService
            .getExportTranslationHandlers(authToken).getObjects()) {
          if (info.getId().equals(exporter)) {
            ioHandlerInfoId = info.getId();
          }
        }
        if (ioHandlerInfoId == null) {
          throw new Exception("Unable to find desired release exporter");
        }

        // begin release
        getLog().info("  Begin translation release");
        ReleaseClientRest releaseService = new ReleaseClientRest(properties);
        releaseService.beginTranslationRelease(translationId, ioHandlerInfoId,
            authToken);

        // validate release
        getLog().info("  Validate translation release");
        releaseService = new ReleaseClientRest(properties);
        releaseService.validateTranslationRelease(translationId, authToken);

        // beta release
        getLog().info("  Beta translation release");
        releaseService = new ReleaseClientRest(properties);
        releaseService.betaTranslationRelease(translationId, ioHandlerInfoId,
            authToken);

        // finish release
        getLog().info("  Finish translation release");
        releaseService = new ReleaseClientRest(properties);
        releaseService.finishTranslationRelease(translationId, authToken);

      }

      // Done
      getLog().info("... done");

    } catch (Exception e) {
      Logger.getLogger(getClass()).error("Unexpected exception", e);
      throw new MojoFailureException("Unexpected exception:", e);
    }

  }
}
