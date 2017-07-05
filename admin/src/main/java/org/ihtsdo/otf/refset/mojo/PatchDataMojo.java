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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.WorkflowServiceJpa;
import org.ihtsdo.otf.refset.rest.impl.ProjectServiceRestImpl;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.WorkflowService;

/**
 * Goal which generates sample data in an empty database. Uses JPA services
 * directly, no need for REST layer.
 * 
 * See admin/pom.xml for sample usage
 * 
 */
@Mojo(name = "patch", defaultPhase = LifecyclePhase.PACKAGE)
public class PatchDataMojo extends AbstractMojo {

  /** The start. */
  @Parameter
  String start;

  /** The end. */
  @Parameter
  String end;

  /**
   * Instantiates a {@link GenerateSampleDataMojo} from the specified
   * parameters.
   */
  public PatchDataMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    try {
      getLog().info("Patch data");
      getLog().info("  start = " + start);
      getLog().info("  end = " + end);

      final WorkflowService workflowService = new WorkflowServiceJpa();
      final TranslationService translationService = new TranslationServiceJpa();

      // Patch 1000001
      // Set project handler key/url for all projects
      if ("20161215".compareTo(start) >= 0 && "20161215".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20161215 - set project terminology handler key/url");
        for (final Project project : workflowService.findProjectsForQuery(null, null)
            .getObjects()) {
          project.setTerminologyHandlerKey("BROWSER");
          project
              .setTerminologyHandlerUrl("https://sct-rest.ihtsdotools.org/api");
          getLog().info(
              "  project = " + project.getId() + ", " + project.getName());
          workflowService.updateProject(project);
        }

        // project needs handler key and URL set
        getLog().info("  Set projects terminology handler key/url");
        final ProjectList list = workflowService.getProjects();
        for (final Project project : list.getObjects()) {
          project.setTerminologyHandlerKey("BROWSER");
          project
              .setTerminologyHandlerUrl("https://sct-rest.ihtsdotools.org/api");
          workflowService.updateProject(project);
        }

      }

      // Patch 20170110
      // Set project handler key/url for all projects
      if ("20170110".compareTo(start) >= 0 && "20170110".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20170110 - set project terminology handler key/url"); // Patch

        // PRIOR to this patch update DB and run admin/src/main/resources/patch20170110.sql

        // This patch requires an "Updatedb"
        // authors_ORDER column (for tracking_record_authors)
        // reviewers_ORDER column (for tracking_record_reviewers)
        // default value should be 1
        // workflowPath column for projects

        // Set projects default
        for (final Project project : workflowService.findProjectsForQuery(null, null)
            .getObjects()) {
          project.setWorkflowPath("DEFAULT");
          getLog().info(
              "  project = " + project.getId() + ", " + project.getName());
          workflowService.updateProject(project);
        }
      }
      
      // Patch 20170706
      // Remove fsns on translation descriptions only in starter set translations
      if ("20170706".compareTo(start) >= 0 && "20170706".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20170706 - Remove fsns on translation descriptions"); // Patch
        int ct = 0;
        translationService.setTransactionPerOperation(false);
        translationService.beginTransaction();
        for (Translation trans : translationService.getTranslations().getObjects()) {
          Translation translation = translationService.getTranslation(trans.getId());
          if (translation.getRefset().getTerminologyId().equals("722128001") || 
              translation.getRefset().getTerminologyId().equals("722131000") || 
              translation.getRefset().getTerminologyId().equals("722130004") || 
              translation.getRefset().getTerminologyId().equals("722129009"))
          for (Concept cpt : translation.getConcepts()) {
            ct++;
            List<Description> newDescriptionList = new ArrayList<>();
            Concept concept = translationService.getConcept(cpt.getId());
            for (Description description : concept.getDescriptions()) {  
                // if fsn, remove language refset members and description
                if (description.getTypeId().equals("900000000000003001")) {
                  description.setLanguageRefsetMembers(new ArrayList<LanguageRefsetMember>());
                  for (LanguageRefsetMember lrm : description.getLanguageRefsetMembers()) {
                    translationService.removeLanguageRefsetMember(lrm.getId());
                  }  
                  translationService.removeDescription(description.getId());
                } else {
                  newDescriptionList.add(description);
                }
                
            }
            concept.setDescriptions(newDescriptionList);
            translationService.updateConcept(concept);
            
            if (ct % 10 == 0) {
              getLog().info("  ct = " + ct);
              translationService.commitClearBegin();
            }
          }
        }
        translationService.commit();
      }

      // Reindex
      getLog().info("  Reindex");
      // login as "admin", use token
      final Properties properties = ConfigUtility.getConfigProperties();
      SecurityService securityService = new SecurityServiceJpa();
      String authToken =
          securityService.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
      contentService.luceneReindex(null, authToken);

      workflowService.close();
      translationService.close();
      getLog().info("Done ...");
    } catch (Exception e) {
      Logger.getLogger(getClass()).error("Unexpected exception", e);
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

}
