/**
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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.rest.client.TranslationClientRest;
import org.ihtsdo.otf.refset.rest.impl.TranslationServiceRestImpl;
import org.ihtsdo.otf.refset.services.SecurityService;

/**
 * Goal for processing a translation delta (e.g. for SNOMED spanish).
 * 
 * See admin/pom.xml for sample usage
 * 
 * @goal delta-translation
 * @phase package
 */
public class DeltaTranslationMojo extends AbstractRttMojo {

  /**
   * File for import.
   *
   * @required
   * @parameter
   */
  private String file;

  /**
   * Importer key.
   *
   * @required
   * @parameter
   */
  private String importer;

  /**
   * Translation id.
   *
   * @required
   * @parameter
   */
  private Long translationId;

  /**
   * Instantiates an empty {@link DeltaTranslationMojo}.
   */
  public DeltaTranslationMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {
      getLog().info("Delta Translation Import");
      getLog().info("  file = " + file);
      getLog().info("  translationId = " + translationId);
      getLog().info("  importer = " + importer);

      setupBindInfoPackage();

      if (!new File(file).exists()) {
        throw new Exception("Specified file does not exist - " + file);
      }
      final Properties properties = ConfigUtility.getConfigProperties();
      // Ensure name lookups are not in the background...
      properties.setProperty("lookup.background", "false");

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

      if (serverRunning) {
        TranslationClientRest translationService =
            new TranslationClientRest(properties);

        // Get IO handler id
        String ioHandlerInfoId = null;
        for (final IoHandlerInfo info : translationService
            .getImportTranslationHandlers(authToken).getObjects()) {
          if (info.getId().equals(importer)) {
            ioHandlerInfoId = info.getId();
          }
        }
        if (ioHandlerInfoId == null) {
          throw new Exception("Unable to find desired delta importer");
        }

        // Begin import
        translationService = new TranslationClientRest(properties);
        translationService.beginImportConcepts(translationId, ioHandlerInfoId,
            authToken);

        // Finish import - the name lookup should block here until finished
        // (e.g. not run in the background)
        final FileInputStream in = new FileInputStream(new File(file));
        translationService = new TranslationClientRest(properties);
        translationService.finishImportConcepts(null, in, translationId,
            ioHandlerInfoId, null, authToken);

      } else {

        TranslationServiceRestImpl translationService =
            new TranslationServiceRestImpl();

        // Get IO handler id
        String ioHandlerInfoId = null;
        for (final IoHandlerInfo info : translationService
            .getImportTranslationHandlers(authToken).getObjects()) {
          if (info.getId().equals(importer)) {
            ioHandlerInfoId = info.getId();
          }
        }
        if (ioHandlerInfoId == null) {
          throw new Exception("Unable to find desired delta importer");
        }

        // Begin import
        translationService = new TranslationServiceRestImpl();
        translationService.beginImportConcepts(translationId, ioHandlerInfoId,
            authToken);

        // Finish import - the name lookup should block here until finished
        // (e.g. not run in the background)
        final FileInputStream in = new FileInputStream(new File(file));
        translationService = new TranslationServiceRestImpl();
        translationService.finishImportConcepts(null, in, translationId,
            ioHandlerInfoId, null, authToken);

      }
      // Done
      getLog().info("... done");

    } catch (Exception e) {
      Logger.getLogger(getClass()).error("Unexpected exception", e);
      throw new MojoFailureException("Unexpected exception:", e);
    }

  }
}
