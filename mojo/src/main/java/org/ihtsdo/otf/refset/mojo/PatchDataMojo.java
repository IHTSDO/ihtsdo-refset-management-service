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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.refset.ConceptRefsetMemberSynonym;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.jpa.ConceptRefsetMemberSynonymJpa;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.WorkflowServiceJpa;
import org.ihtsdo.otf.refset.rest.impl.ProjectServiceRestImpl;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.RootService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.TrackingRecordListJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Goal which generates sample data in an empty database. Uses JPA services
 * directly, no need for REST layer.
 * 
 * See admin/pom.xml for sample usage
 * 
 */
@Mojo(name = "patch", defaultPhase = LifecyclePhase.PACKAGE)
public class PatchDataMojo extends AbstractRttMojo {

  /** The start. */
  @Parameter
  String start;

  /** The end. */
  @Parameter
  String end;

  /**
   * The refsetId. A comma-delimited list of refset ids
   */
  @Parameter
  String refsetIds = null;

  /**
   * The projectId. A comma-delimited list of project ids
   */
  @Parameter
  String projectIds = null;
  
  /**
   * The specified input file (for driving action)
   */
  @Parameter
  private String input = null;

  /** The already reviewed japanese. */
  String[] alreadyReviewedJapanese = {
      "56265001", "247479008", "60098005", "46322009", "95800001", "6234006",
      "95087004", "9763007", "91251008", "74715005", "9468002", "40541001",
      "71938000", "42800007", "429418007", "68142008", "64531003", "50063009",
      "82525005", "54840006", "405737000", "90834002", "63234004", "77265008",
      "805002", "404904002", "83225003", "89765005", "72298008", "83901003",
      "416030007", "52254009", "6740003", "6383007", "62452009", "77377001",
      "412750001", "48123002", "88616000", "54385001", "50582007", "68555003",
      "428570002", "428194005", "59214008", "64156001", "84027009", "40956001",
      "422338006", "433144002", "71642004", "50642008", "49708008", "9418005",
      "81405006", "67569000", "50417007", "88157006", "90036004", "48638002",
      "79410001", "88889000", "64540004", "50563003", "46085004", "46557008",
      "429650005", "70076002", "68913001", "41497008", "427327003", "444862003",
      "426174008", "91138005", "409796004", "42284007", "43491000", "53132006",
      "66383009", "52404001", "65636009", "40070004", "4788002", "53936005",
      "77116006", "88092000", "76956004", "63988001", "91930004", "68226007",
      "78948009", "82639001", "93880001", "85102008", "967006", "84568007",
      "91857003", "76612001", "78435003", "86481000", "79883001", "82302008",
      "81576005", "80193009", "86299006", "89138009", "90935002", "84466009",
      "85007004", "9209005", "82272006", "93922007", "9389005", "90823000",
      "89362005", "82649003", "88902008", "93749002", "93459000", "87628006",
      "93974005", "84138006", "81004002", "89266005", "9177003", "77416000",
      "92517006", "95668009", "90446007", "87557004", "83291003", "87858002",
      "91514001", "80384002", "86128003", "87763006", "86380000", "8137003",
      "85005007", "88518009", "91489000", "9707006", "91588005", "95217000",
      "81000006", "93796005", "92206006", "8414002", "82065001", "84828003",
      "85216006", "7973008", "9957009", "79879001", "88531004", "91374005",
      "83458005", "81094005", "7632005", "85828009", "84299009", "92824003",
      "80068009", "80712009", "85922006", "77299006", "76272004", "90507008",
      "90927000", "95486002", "92097004", "83421005", "93642000", "77176002",
      "84984002", "85189001", "85777005", "80394007", "87486003", "95806007",
      "9014002", "92186001", "94225005", "86198006", "93781006", "76865005",
      "94351005", "81681009", "83330001", "76742009", "85746008", "80515008",
      "95344007", "82545002", "80142000", "95415006", "94347008", "85419002",
      "78868004", "78267003", "9682006", "79626009", "95823003", "82675004",
      "811004", "84849002", "85232009", "8422009", "83620003", "9740002",
      "90673000", "86735004", "81712001", "95711003", "93646002", "94181007",
      "91273001", "9061001", "90176007", "90325002", "82035006", "87860000",
      "90460009", "82632005", "83128009", "91947003", "83074005", "8220004",
      "87513003", "86765009", "80659006", "81703003", "83746006", "95726001",
      "89656008", "86477000", "95563007", "83119008", "93616000", "83132003",
      "84445001", "89322006", "79720007", "80201000119103", "78092008",
      "8357008", "84149000", "95321009", "98641000119100", "76844004",
      "90392009", "85848002", "77493009", "80327007", "93870000", "83546008",
      "90584004", "85224001", "92384009", "83898004", "91356001", "93163002",
      "7792000", "90968009", "77090002", "8765009", "7916009", "91442002",
      "85407005", "82991003", "90244007", "8517006", "81996005", "83270006",
      "87522002", "95319004", "88151007", "78314001", "8098009", "95850008",
      "95855003", "9794007", "89797005", "81935006", "78420004", "89461002",
      "81877007", "95770005", "95657009", "9560007", "92059004", "8847002",
      "88610006", "8009008", "9078005", "9826008", "89105000", "95677002",
      "76067001", "86041002", "95325000", "91943004", "8943002", "76916001",
      "78370002", "85502002", "9126005", "94397007", "95898004", "7895008",
      "93423006", "86203003", "86933000", "92207002", "91613004", "88223008",
      "87991007", "8619003", "84416003", "77248004", "94381002", "93761005",
      "8229003", "95324001", "77543007", "79012001", "83607001", "76902006",
      "78275009", "80645004", "84857004", "86094006", "95820000", "86423004",
      "82998009", "94392001", "83561009", "82423001", "81308009", "76583009",
      "84471002", "90979004", "79631006", "84292000", "80640009", "8510008",
      "84611003", "91175000", "94627008", "8217007", "79267007", "95453001",
      "94391008", "77547008", "82119001", "92358003", "9991008", "91936005",
      "95570007", "81418003", "87065009", "91357005", "89242004", "91603007",
      "95655001", "86414002", "9631008", "77945009", "78623009", "89748001",
      "80281008", "76783007", "93689003", "82562007", "93143009", "89164003",
      "8186001", "78875003", "7768008", "83536006", "76376003", "90678009",
      "79015004", "92468007", "80756009", "92102001", "84494001", "77803008",
      "965003", "79922009", "95891005", "818005", "95279007", "91669008",
      "80182007", "95675005", "86466006", "85636003", "88850006", "93651008",
      "90470006", "77075001", "76581006", "81498004", "92166000", "80967001",
      "815008", "93727008", "88968005", "79342006", "86489003", "89452002",
      "78768009", "81060008", "94087009", "92653004", "85898001", "94222008",
      "95673003", "8635005", "81704009", "90539001", "95529005", "76668005",
      "79619009", "813001", "93469006", "80060002", "84089009", "9713002",
      "90458007", "93025007", "81371004", "8493009", "87132004", "86022000",
      "82313006", "87118001", "95421005", "93471006", "81512004", "94057003",
      "86044005", "80313002", "82314000", "78455002", "92103006", "78975002",
      "78516000", "94730005", "77329001", "76976005", "93458008", "82999001",
      "79890006", "91957002", "82300000", "86378006", "88424000", "77252004",
      "76107001", "79411002", "78077007", "78004001", "92247009", "80910005",
      "91302008", "89458003", "94767002", "78580004", "77971008", "84758004",
      "9431000", "95566004", "86133004", "7951001", "91037003", "78667006",
      "84114007", "82576008", "86276007", "89627008", "87979003", "91487003",
      "76498008", "90708001", "79740000", "8644006", "88805009", "83469008",
      "76593002", "78048006", "94147001", "92354001", "86981007", "93478000",
      "88906006", "91019004", "7751009", "88264003", "91861009", "90199006",
      "95722004", "81817003", "80183002", "85057007", "95812002", "78622004",
      "87376003", "88111009", "82271004", "76571007", "87551000119101",
      "85769006", "80141007", "9414007", "81564005", "88121000119101",
      "91992005", "80146002", "95801002", "85280007", "86030004", "91221002",
      "85915003", "85551004", "93655004", "86142006", "90127001", "90688005",
      "84480002", "82297005", "95315005", "92829008", "88032003", "82971005",
      "82403002", "8367003", "82473003", "89155008", "78514002", "95270006",
      "92564006", "8034008", "90207007", "95214007", "93849006", "81119003",
      "8913004", "77489003", "87317003", "80585000", "88348008", "82196007",
      "81102000", "87715008", "8501000119104", "89091004", "79962008",
      "90560007", "93934004", "92764008", "76616003", "91637004", "76462000",
      "7674000", "84229001", "76807004", "81639003", "7620006", "87527008",
      "84172003", "89538001", "92051001", "86708008", "88213004", "89820008",
      "87778004", "93641007", "87433001", "792004", "83414005", "95814001",
      "92664001", "80423007", "95418008", "77880009", "87414006", "76601001",
      "8722008", "93824007", "92749008", "78031003", "82608003", "86406008",
      "76799001", "80495009", "84757009", "78809005", "90128006", "84619001",
      "85670002", "79298009", "84677008", "83943005", "961000119104",
      "78745000", "87872006", "76618002", "95922009", "93984006", "95345008",
      "85649008", "86279000", "91935009", "87282003", "79471008", "87269006",
      "95559000", "92814006", "78141002", "90739004", "88318005", "8004003",
      "91478007", "95417003", "81680005", "85782003", "77599005", "77911002",
      "81902001", "88594005", "7765006", "88415009", "82127005", "90235006",
      "77506005", "77507001", "87343002", "9651007", "84017003", "95828007",
      "8011004", "92439006", "77692006", "81516001", "79348005", "87614000",
      "85059005", "77386006", "94022001"
  };

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

    setupBindInfoPackage();

    try (final WorkflowService workflowService = new WorkflowServiceJpa();
        final TranslationService translationService =
            new TranslationServiceJpa();
        final RefsetService refsetService = new RefsetServiceJpa();) {

      // Default to false as there is at least one case where full reindexing
      // not needed
      boolean fullReindex = false;

      getLog().info("Patch data");
      getLog().info("  start = " + start);
      getLog().info("  end = " + end);
      getLog().info("  projectIds = " + projectIds);
      getLog().info("  refsetIds = " + refsetIds);

      // Patch 1000001
      // Set project handler key/url for all projects
      if ("20161215".compareTo(start) >= 0 && "20161215".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20161215 - set project terminology handler key/url");

        patch1000001(workflowService);
        fullReindex = true;
      }

      // Patch 20170110
      // Set project handler key/url for all projects
      if ("20170110".compareTo(start) >= 0 && "20170110".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20170110 - set project terminology handler key/url"); // Patch

        patch20170110(workflowService);
        fullReindex = true;
      }

      // Patch 20170706
      // Remove fsns on translation descriptions only in starter set
      // translations
      if ("20170706".compareTo(start) >= 0 && "20170706".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20170706 - Remove fsns on translation descriptions"); // Patch

        patch20170706(translationService);
        fullReindex = true;
      }

      // Patch 20170920
      // Remove fsns on translation descriptions only in starter set
      // translations
      // 90979004 Chronic tonsillitis (disorder) example of Published concept
      // already reviewed
      // 57759005 First degree perineal laceration (disorder) example of
      // Published concept not previously reviewed
      if ("20170920".compareTo(start) >= 0 && "20170920".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20170920 - Move unreviewed Japanese Starter Set Translation concepts back to EDITING_DONE state"); // Patch

        patch20170920(workflowService, translationService);
        fullReindex = true;
      }

      // Patch 20171113
      // refset member ids set to referencedComponentIds instead of UUIDs
      // when adding subtrees
      // fix to give random UUID
      if ("20171113".compareTo(start) >= 0 && "20171113".compareTo(end) <= 0) {
        getLog()
            .info("Processing patch 20171113 - Fix member ids that need UUIDs"); // Patch

        patch20171113(translationService);
        fullReindex = true;
      }

      // Patch 20180316
      // remove list of concepts from Starter Set translations
      if ("20180316".compareTo(start) >= 0 && "20180316".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20180316 - *Remove translation concepts that were removed from corresponding refset"); // Patch

        patch20180316(translationService);
        fullReindex = true;
      }

      // Patch 20190708
      // Update browser url to snowstorm and update refset and terminology
      // versions
      // to snowstorm version formating
      if ("20190708".compareTo(start) >= 0 && "20190708".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20190708 - Updating browser to snowstorm APIs"); // Patch

        patch20190708(translationService);
        fullReindex = true;
      }

      // Patch 20190916
      // Update browser url to snowstorm and update refset and terminology
      // versions to snowstorm version formating
      // This is an interim step prior to the rest of the Refset Enhancmement
      // being published
      if ("20190916".compareTo(start) >= 0 && "20190916".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20190916 - Updating browser to snowstorm APIs"); // Patch

        patch20190916(translationService);
        fullReindex = true;
      }

      // Patch 20190728
      // Patch to populate the new field in ConceptRefsetMemberJpa (synonyms)
      // Done once per production system. Only need to reindex one thing, so
      // unless others asking for index, will index within patch
      if ("20190728".compareTo(start) >= 0 && "20190728".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20190728 - Adding new ConceptRefsetMemberJpa 'Synonyms' table"); // Patch
        refsetIds = "24357942,24357945,24931373,24931381,23871126";
        projectIds = "1701,2053";
        patch20190728(workflowService, refsetService, fullReindex);
      }

      // Patch 20190917
      // Update terminology handlers in project
      if ("20190917".compareTo(start) >= 0 && "20190917".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20190917 - Update terminology handlers in project"); // Patch

        patch20190917(fullReindex);
      }

      // Patch current PROD setup to new JDK-11 Refset Enhancement setup
      if ("20190923".compareTo(start) >= 0 && "20190923".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20190923 - Update PROD database to Refset Enhancement setup"); // Patch

        patch20190923(translationService, fullReindex);
      }

      // Patch current PROD setup to new JDK-11 Refset Enhancement setup
      // (updated due to changes made to PROD)
      if ("20191009".compareTo(start) >= 0 && "20191009".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20191009 - Update PROD database to Refset Enhancement setup"); // Patch

        patch20191009(translationService, fullReindex);
      }

      // Patch to remove /v2 from the end of terminologyHandlerURLs for
      // non-MANAGED-SERVICE projects
      if ("20191125".compareTo(start) >= 0 && "20191125".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20191125 - remove /v2 from the end of all handler URLs"); // Patch

        patch20191125(fullReindex);
      }

      // Patch to change the terminologyHandlerURLs for AUTHORING-INTL projects
      if ("20200512".compareTo(start) >= 0 && "20200512".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20200512 - changing the terminologyHandlerURL for AUTHORING-INTL projects"); // Patch

        patch20200512(fullReindex);
      } 
      
      // Patch to change the terminologyHandlerURLs for AUTHORING-INTL projects
      if ("20200603".compareTo(start) >= 0 && "20200603".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20200603 - updating MANAGED-SERVICE projects from Snowowl to Snowstorm"); // Patch

        patch20200603(translationService, workflowService, refsetService, fullReindex);
      } 
      
      // Patch 20200611
      // Concept name and synonym lookups for the following projects and/or refsets
      if ("20200611".compareTo(start) >= 0 && "20200611".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20200611 - Concept name and synonym lookups for the following projects and/or refsets"); // Patch
        //refsetIds = "24357942,24357945,24931373,24931381,23871126";
        //projectIds = "1701,2053";
        patch20200611(workflowService, refsetService, fullReindex);
      }
      
      // Patch 20200730
      // Add new role LEAD - move users from Admin to Lead with exclusions.
      if ("20200730".compareTo(start) >= 0 && "20200730".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20200730 - Add new role LEAD - move users from Admin to Lead with exclusions"); // Patch
        patch20200730( fullReindex);
      }
      

      // Patch 20200805
      // Update Norway language codes
      if ("20200805".compareTo(start) >= 0 && "20200805".compareTo(end) <= 0) {
        getLog().info(
            "Processing patch 20200805 - Update Norway language codes"); // Patch
        patch20200805(workflowService, translationService);
      }

      // Reindex
      if (fullReindex) {
        getLog().info("  Reindex");
        // login as "admin", use token
        final Properties properties = ConfigUtility.getConfigProperties();

        try (
            final SecurityService securityService = new SecurityServiceJpa();) {
          String authToken =
              securityService.authenticate(properties.getProperty("admin.user"),
                  properties.getProperty("admin.password")).getAuthToken();
          ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
          contentService.luceneReindex(null, null, null, authToken);
        }
      }

      getLog().info("Done ...");
    } catch (Exception e) {
      Logger.getLogger(getClass()).error("Unexpected exception", e);
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  /**
   * Patch 20190708.
   *
   * @param translationService the translation service
   * @throws Exception the exception
   */
  private void patch20190708(TranslationService translationService)
    throws Exception {
    int ct = 0;
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    for (Project prj : translationService.getProjects().getObjects()) {
      Project project = translationService.getProject(prj.getId());
      if (project.getTerminologyHandlerKey().equals("BROWSER")) {
        ct++;
        project.setTerminologyHandlerUrl(
            "https://prod-browser.ihtsdotools.org/snowstorm/snomed-ct/v2");
        if (project.getTerminology().equals("se-edition")) {
          project.setTerminology("SNOMEDCT-SE");
        } else if (project.getTerminology().equals("nl-edition")) {
          project.setTerminology("SNOMEDCT-NL");
        } else if (project.getTerminology().equals("ca-edition")) {
          project.setTerminology("SNOMEDCT-CA");
        } else {
          project.setTerminology("SNOMEDCT");
        }
        project.setTerminologyHandlerKey("PUBLIC-BROWSER");
        translationService.updateProject(project);

        if (ct % 100 == 0) {
          getLog().info("projects updated  ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("projects updated final ct = " + ct);
    ct = 0;
    for (Translation trans : translationService.getTranslations()
        .getObjects()) {
      Translation translation =
          translationService.getTranslation(trans.getId());
      if (translation.getProject().getTerminologyHandlerKey().equals("BROWSER")
          && translation.getVersion().length() == 8) {
        ct++;
        String old_version = translation.getVersion();
        translation.setVersion("MAIN/" + old_version.substring(0, 4) + "-"
            + old_version.substring(4, 6) + "-" + old_version.substring(6, 8));
        if (translation.getTerminology().equals("se-edition")) {
          translation.setTerminology("SNOMEDCT-SE");
        } else if (translation.getTerminology().equals("nl-edition")) {
          translation.setTerminology("SNOMEDCT-NL");
        } else if (translation.getTerminology().equals("ca-edition")) {
          translation.setTerminology("SNOMEDCT-CA");
        } else {
          translation.setTerminology("SNOMEDCT");
        }
        translationService.updateTranslation(translation);

        if (ct % 100 == 0) {
          getLog().info("translations updated  ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("translations updated final ct = " + ct);
    ct = 0;
    for (Refset ref : translationService.getRefsets().getObjects()) {
      Refset refset = translationService.getRefset(ref.getId());
      if (refset.getProject().getTerminologyHandlerKey().equals("BROWSER")
          && refset.getVersion().length() == 8) {
        ct++;
        String old_version = refset.getVersion();
        refset.setVersion("MAIN/" + old_version.substring(0, 4) + "-"
            + old_version.substring(4, 6) + "-" + old_version.substring(6, 8));
        if (refset.getTerminology().equals("se-edition")) {
          refset.setTerminology("SNOMEDCT-SE");
        } else if (refset.getTerminology().equals("nl-edition")) {
          refset.setTerminology("SNOMEDCT-NL");
        } else if (refset.getTerminology().equals("ca-edition")) {
          refset.setTerminology("SNOMEDCT-CA");
        } else {
          refset.setTerminology("SNOMEDCT");
        }
        translationService.updateRefset(refset);

        if (ct % 100 == 0) {
          getLog().info("refsets updated ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("refsets updated final ct = " + ct);
    translationService.commit();

  }

  /**
   * Patch 20190916.
   *
   * @param translationService the translation service
   * @throws Exception the exception
   */
  private void patch20190916(TranslationService translationService)
    throws Exception {
    int ct = 0;
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    for (Project prj : translationService.getProjects().getObjects()) {
      Project project = translationService.getProject(prj.getId());
      if (project.getTerminologyHandlerKey().equals("BROWSER")) {
        ct++;
        project.setTerminologyHandlerUrl(
            "https://prod-browser.ihtsdotools.org/snowstorm/snomed-ct/v2");
        if (project.getTerminology().equals("se-edition")) {
          project.setTerminology("SNOMEDCT-SE");
        } else if (project.getTerminology().equals("nl-edition")) {
          project.setTerminology("SNOMEDCT-NL");
        } else if (project.getTerminology().equals("ca-edition")) {
          project.setTerminology("SNOMEDCT-CA");
        } else {
          project.setTerminology("SNOMEDCT");
        }
        project.setTerminologyHandlerKey("SNOWSTORM");
        translationService.updateProject(project);

        if (ct % 100 == 0) {
          getLog().info("projects updated  ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("projects updated final ct = " + ct);
    ct = 0;
    for (Translation trans : translationService.getTranslations()
        .getObjects()) {
      Translation translation =
          translationService.getTranslation(trans.getId());
      if ((translation.getProject().getTerminologyHandlerKey().equals("BROWSER")
          || translation.getProject().getTerminologyHandlerKey()
              .equals("SNOWSTORM"))
          && translation.getVersion().length() == 8) {
        ct++;
        String old_version = translation.getVersion();
        translation.setVersion("" + old_version.substring(0, 4) + "-"
            + old_version.substring(4, 6) + "-" + old_version.substring(6, 8));
        if (translation.getTerminology().equals("se-edition")) {
          translation.setTerminology("SNOMEDCT-SE");
        } else if (translation.getTerminology().equals("nl-edition")) {
          translation.setTerminology("SNOMEDCT-NL");
        } else if (translation.getTerminology().equals("ca-edition")) {
          translation.setTerminology("SNOMEDCT-CA");
        } else {
          translation.setTerminology("SNOMEDCT");
        }
        translationService.updateTranslation(translation);

        if (ct % 100 == 0) {
          getLog().info("translations updated  ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("translations updated final ct = " + ct);
    ct = 0;
    for (Refset ref : translationService.getRefsets().getObjects()) {
      Refset refset = translationService.getRefset(ref.getId());
      if ((refset.getProject().getTerminologyHandlerKey().equals("BROWSER")
          || refset.getProject().getTerminologyHandlerKey().equals("SNOWSTORM"))
          && refset.getVersion().length() == 8) {
        ct++;
        String old_version = refset.getVersion();
        refset.setVersion("" + old_version.substring(0, 4) + "-"
            + old_version.substring(4, 6) + "-" + old_version.substring(6, 8));
        if (refset.getTerminology().equals("se-edition")) {
          refset.setTerminology("SNOMEDCT-SE");
        } else if (refset.getTerminology().equals("nl-edition")) {
          refset.setTerminology("SNOMEDCT-NL");
        } else if (refset.getTerminology().equals("ca-edition")) {
          refset.setTerminology("SNOMEDCT-CA");
        } else {
          refset.setTerminology("SNOMEDCT");
        }
        translationService.updateRefset(refset);

        if (ct % 100 == 0) {
          getLog().info("refsets updated ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("refsets updated final ct = " + ct);
    translationService.commit();
  }

  /**
   * Patch 20180316.
   *
   * @param translationService the translation service
   * @throws Exception the exception
   */
  private void patch20180316(TranslationService translationService)
    throws Exception {
    String[] translationConceptsToRemoveArray = new String[] {
        "95801002", "91588005", "81877007", "81371004", "8137003", "81102000",
        "77299006", "73879007", "72298008", "69124005", "68525005", "67415000",
        "66064007", "64756007", "6408001", "60782007", "58850003", "50845008",
        "50548001", "5015009", "500000", "4946002", "47268002", "441702008",
        "439575008", "430950005", "430949005", "4296008", "429275000",
        "428392002", "427354000", "426041005", "424989000", "421707005",
        "421668005", "419769007", "419686005", "419258005", "417130004",
        "410567004", "40913006", "402121009", "40129004", "401123009",
        "400192002", "399625000", "398175007", "397683000", "390845001",
        "37757003", "371330000", "371093006", "367522007", "35731002",
        "34140002", "32935005", "3226008", "31829008", "312403005", "30961001",
        "308551004", "304388009", "30211000119106", "281657000", "276789009",
        "269813009", "268808004", "268465005", "268300003", "267796002",
        "267129008", "267094008", "267052005", "266364000", "262951009",
        "26174007", "239148005", "238402004", "236704009", "236425005",
        "23346002", "22913005", "220000", "211964006", "208647006", "201836008",
        "200627004", "199516000", "195747001", "192839001", "192000006",
        "190784001", "190392008", "182782007", "171073000", "169851005",
        "169850006", "16863000", "162249002", "161684005", "161612000",
        "161591004", "161590003", "15387003", "1508000", "128079007",
        "11991005", "119424003", "119415007", "111726004", "111181004",
        "108267006", "106130002", "105592009", "102602003"
    };
    Set<String> translationConceptsToRemove =
        new HashSet<String>(Arrays.asList(translationConceptsToRemoveArray));
    String refsetId = "733876003"; // Starter set refset
    translationService.setTransactionPerOperation(true);
    getLog().info("#translations:"
        + translationService.getTranslations().getTotalCount());
    for (Translation translation : translationService.getTranslations()
        .getObjects()) {
      getLog().info("refset tid:" + translation.getRefset().getTerminologyId());
      if (!translation.getRefset().getTerminologyId().equals(refsetId)) {
        continue;
      }
      ConceptList conceptList = translationService
          .findConceptsForTranslation(translation.getId(), "", null);
      getLog().info("conceptList size:" + conceptList.getTotalCount());
      int ct = 1;
      for (Concept concept : conceptList.getObjects()) {
        if (translationConceptsToRemove.contains(concept.getTerminologyId())) {
          try {
            translationService.setTransactionPerOperation(true);

            translationService.removeConcept(concept.getId(), true);
            getLog().info("Removing translation concept: " + ct++ + " "
                + concept.toString());
          } catch (Exception e) {
            getLog().info("Unable to remove concept: " + concept.toString());
          }
        }

      }

    }
  }

  /**
   * Patch 20171113.
   *
   * @param translationService the translation service
   * @throws Exception the exception
   */
  private void patch20171113(TranslationService translationService)
    throws Exception {
    int ct = 1;
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();

    for (Refset rfst : translationService.getRefsets().getObjects()) {
      Refset refset = translationService.getRefset(rfst.getId());
      // only execute this on the CDAC India project where refsets have
      // not yet been published
      if (!refset.getProject().getName().contains("CDAC")) {
        continue;
      }
      for (ConceptRefsetMember member : refset.getMembers()) {
        if (member.getTerminologyId().equals(member.getConceptId())) {
          member.setTerminologyId(UUID.randomUUID().toString());
          ct++;
          translationService.updateMember(member);
          getLog().info("Fixing member: " + member.getConceptId() + " in "
              + refset.getTerminologyId());
        }

        if (ct % 100 == 0) {
          getLog().info("  ct = " + ct);
          translationService.commitClearBegin();
        }
      }

    }
    translationService.commit();
  }

  /**
   * Patch 20170920.
   *
   * @param workflowService the workflow service
   * @param translationService the translation service
   * @throws Exception the exception
   */
  private void patch20170920(WorkflowService workflowService,
    TranslationService translationService) throws Exception {
    int ct = 0;
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    for (Translation trans : translationService.getTranslations()
        .getObjects()) {
      Translation translation =
          translationService.getTranslation(trans.getId());
      if (translation.getRefset().getTerminologyId().equals("722129009")) {
        for (Concept concept : translation.getConcepts()) {
          // if it isn't already reviewed, but is READY_FOR_PUBLICATION,
          // reassign
          if (!Arrays.asList(alreadyReviewedJapanese)
              .contains(concept.getTerminologyId())
              && concept
                  .getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION) {

            ct++;
            TrackingRecordList recordList = new TrackingRecordListJpa();
            // do not perform a lookup if concept is new
            if (concept != null && concept.getId() != null) {
              recordList = workflowService.findTrackingRecordsForQuery(
                  "conceptId:" + ((concept == null || concept.getId() == null)
                      ? -1 : concept.getId()),
                  null);
            }
            TrackingRecord record = null;
            if (recordList.getCount() == 1) {
              record = recordList.getObjects().get(0);
              workflowService.handleLazyInit(record);
              if (record.getConcept() != null) {
                workflowService.handleLazyInit(record.getConcept());
              }
            } else if (recordList.getCount() > 1) {
              throw new LocalException(
                  "Unexpected number of tracking records for "
                      + concept.getTerminologyId());
            }

            // Author case
            if (record == null) {
              // Add the concept itself (if not already
              // exists)
              if (concept.getId() == null) {
                concept.setTranslation(translation);
                concept.setModuleId(translation.getModuleId());
                concept.setEffectiveTime(null);
                concept.setDefinitionStatusId("UNKNOWN");
                concept.setLastModifiedBy("dshapiro");
                workflowService.addConcept(concept);
              }
              // Create a tracking record, fill it out,
              // and add it.
              TrackingRecord record2 = new TrackingRecordJpa();
              record2.getAuthors().add("dshapiro");
              record2.setForAuthoring(true);
              record2.setForReview(false);
              record2.setLastModifiedBy("dshapiro");
              record2.setTranslation(translation);
              record2.setConcept(concept);
              if (concept
                  .getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION) {
                record2.setRevision(true);
                record2.setOriginRevision(
                    workflowService.getConceptRevisionNumber(concept.getId()));
                concept.setRevision(true);
              }
              record = record2;
              workflowService.addTrackingRecord(record2);
            }

            // Reviewer case
            else {
              record.setForAuthoring(false);
              record.setForReview(true);
              // Set the review origin revision, so we can
              // revert on unassign
              record.setReviewOriginRevision(
                  workflowService.getConceptRevisionNumber(concept.getId()));
              record.getReviewers().add("dshapiro");
              record.setLastModifiedBy("dshapiro");
              concept.setWorkflowStatus(WorkflowStatus.REVIEW_NEW);

            }
          }
          translationService.updateConcept(concept);

          if (ct % 100 == 0) {
            getLog().info("  ct = " + ct);
            translationService.commitClearBegin();
          }
        }
      }
    }
    translationService.commit();
  }

  /**
   * Patch 20170706.
   *
   * @param translationService the translation service
   * @throws Exception the exception
   */
  private void patch20170706(TranslationService translationService)
    throws Exception {
    int ct = 0;
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    for (Translation trans : translationService.getTranslations()
        .getObjects()) {
      Translation translation =
          translationService.getTranslation(trans.getId());
      if (translation.getRefset().getTerminologyId().equals("722128001")
          || translation.getRefset().getTerminologyId().equals("722131000")
          || translation.getRefset().getTerminologyId().equals("722130004")
          || translation.getRefset().getTerminologyId().equals("722129009")) {
        for (Concept cpt : translation.getConcepts()) {
          ct++;
          List<Description> newDescriptionList = new ArrayList<>();
          Concept concept = translationService.getConcept(cpt.getId());
          if (concept == null) {
            continue;
          }
          for (Description description : concept.getDescriptions()) {
            // if fsn, remove language refset members and description
            if (description.getTypeId().equals("900000000000003001")) {
              description.setLanguageRefsetMembers(
                  new ArrayList<LanguageRefsetMember>());
              for (LanguageRefsetMember lrm : description
                  .getLanguageRefsetMembers()) {
                translationService.removeLanguageRefsetMember(lrm.getId());
              }
              translationService.removeDescription(description.getId());
            } else {
              newDescriptionList.add(description);
            }
          }
          concept.setDescriptions(newDescriptionList);
          translationService.updateConcept(concept);

          if (ct % 100 == 0) {
            getLog().info("  ct = " + ct);
            translationService.commitClearBegin();
          }
        }
      }
    }
    translationService.commit();
  }

  /**
   * Patch 20170110.
   *
   * @param workflowService the workflow service
   * @throws Exception the exception
   */
  private void patch20170110(WorkflowService workflowService) throws Exception {
    // PRIOR to this patch update DB and run
    // admin/src/main/resources/patch20170110.sql

    // This patch requires an "Updatedb"
    // authors_ORDER column (for tracking_record_authors)
    // reviewers_ORDER column (for tracking_record_reviewers)
    // default value should be 1
    // workflowPath column for projects

    // Set projects default
    for (final Project project : workflowService
        .findProjectsForQuery(null, null).getObjects()) {
      project.setWorkflowPath("DEFAULT");
      getLog()
          .info("  project = " + project.getId() + ", " + project.getName());
      workflowService.updateProject(project);
    }
  }

  /**
   * Patch 1000001.
   *
   * @param workflowService the workflow service
   * @throws Exception the exception
   */
  private void patch1000001(WorkflowService workflowService) throws Exception {
    for (final Project project : workflowService
        .findProjectsForQuery(null, null).getObjects()) {
      project.setTerminologyHandlerKey("BROWSER");
      project.setTerminologyHandlerUrl("https://sct-rest.ihtsdotools.org/api");
      getLog()
          .info("  project = " + project.getId() + ", " + project.getName());
      workflowService.updateProject(project);
    }

    // project needs handler key and URL set
    getLog().info("  Set projects terminology handler key/url");
    final ProjectList list = workflowService.getProjects();
    for (final Project project : list.getObjects()) {
      project.setTerminologyHandlerKey("BROWSER");
      project.setTerminologyHandlerUrl("https://sct-rest.ihtsdotools.org/api");
      workflowService.updateProject(project);
    }

  }

  /**
   * Patch 20190728.
   *
   * @param workflowService the workflow service
   * @param refsetService the refset service
   * @param fullReindex the full reindex
   * @throws Exception the exception
   */
  private void patch20190728(WorkflowService workflowService,
    RefsetService refsetService, boolean fullReindex) throws Exception {
    Set<Long> uniqueProjects = new HashSet<>();
    // Map of (ConceptId+"|"+RefsetVersion to Set of synonyms)
    Map<String, Set<ConceptRefsetMemberSynonym>> conceptVersionSynonymsMap =
        new HashMap<>();

    // Turn the project/refset id strings into lists, if specified
    List<String> projectIdsList = new ArrayList<>();
    List<String> refsetIdsList = new ArrayList<>();
    if (projectIds != null) {
      projectIdsList = Arrays.asList(projectIds.split(",", -1));
    }
    if (refsetIds != null) {
      refsetIdsList = Arrays.asList(refsetIds.split(",", -1));
    }

    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();
    for (final Project project : workflowService
        .findProjectsForQuery(null, null).getObjects()) {
      if (!uniqueProjects.contains(project.getId())) {
        getLog().info("About to start updating project: " + project.getName());
        uniqueProjects.add(project.getId());

        // Only run on subset of projects, if specified
        if (projectIds != null
            && !projectIdsList.contains(project.getId().toString())) {
          continue;
        }

        TerminologyHandler handler =
            refsetService.getTerminologyHandler(project, null);

        for (Refset refset : project.getRefsets()) {
          // Only run on subset of refsets, if specified
          if (refsetIds != null
              && !refsetIdsList.contains(refset.getId().toString())) {
            continue;
          }

          getLog().info("About to start updating refset: " + refset.getId());

          // If the refset's branch is invalid (e.g. a very old version, etc.),
          // do not lookup members/synonyms
          // Test by trying to retrieve the top-level Snomed concept:
          // "138875005 | SNOMED CT Concept (SNOMED RT+CTV3) |"
          Concept testConcept = null;

          try {
            testConcept = handler.getConcept("138875005",
                refset.getTerminology(), refset.getVersion());
          } catch (Exception e) {
            // n/a
          }

          if (testConcept == null) {
            continue;
          }

          // handle lazy init
          for (ConceptRefsetMember member : refset.getMembers()) {
            refsetService.handleLazyInit(member);
          }

          int count = 0;
          for (ConceptRefsetMember member : refset.getMembers()) {

            // For any concept that has already had its synonyms looked up, add
            // them from the cache
            if (conceptVersionSynonymsMap.get(
                member.getConceptId() + "|" + refset.getVersion()) != null) {
              // Create new synonyms based on the cache and wire to this member
              for (ConceptRefsetMemberSynonym synonym : conceptVersionSynonymsMap
                  .get(member.getConceptId() + "|" + refset.getVersion())) {
                ConceptRefsetMemberSynonym newSynonym =
                    new ConceptRefsetMemberSynonymJpa(synonym);
                newSynonym.setMember(member);
                // Make sure to only add unique synonyms. Block duplicates
                if (!member.getSynonyms().contains(newSynonym)) {
                  refsetService.addConceptRefsetMemberSynonym(newSynonym);
                  member.getSynonyms().add(newSynonym);
                }
              }
            }
            // Otherwise, flag the concept as needing synonyms looked up
            else {
              member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
            }
            refsetService.updateMember(member);
            count++;
            if (count % RootService.commitCt == 0) {
              refsetService.commitClearBegin();
            }
          }
          refsetService.commitClearBegin();

          // Okay, you can actually lookup the synonyms now.
          refsetService.lookupMemberNames(refset.getId(),
              "initial population of synonyms for refset=" + refset.getId(),
              false, true);

          refsetService.commitClearBegin();
          // Finally, add all newly looked up synonyms to the cache
          Refset refsetWithSynonyms = refsetService.getRefset(refset.getId());
          for (ConceptRefsetMember member : refsetWithSynonyms.getMembers()) {
            // Create a new synonym and clear out the member associated with it,
            // since it will be different when getting reused
            Set<ConceptRefsetMemberSynonym> synonymsToCache = new HashSet<>();
            for (ConceptRefsetMemberSynonym synonym : member.getSynonyms()) {
              ConceptRefsetMemberSynonym synonymToCache =
                  new ConceptRefsetMemberSynonymJpa(synonym);
              synonymToCache.setMember(null);
              synonymsToCache.add(synonymToCache);
            }
            conceptVersionSynonymsMap.put(
                member.getConceptId() + "|" + refset.getVersion(),
                synonymsToCache);
          }
        }

        getLog().info(" Completed project: " + project.getName());
      }
    }

    // identify projects via get operation
    getLog().info("  Identify projects using getProjects()");
    final ProjectList list = workflowService.getProjects();
    for (final Project project : list.getObjects()) {
      if (!uniqueProjects.contains(project.getId())) {
        getLog().info("About to start updating project: " + project.getName());
        uniqueProjects.add(project.getId());

        // Only run on subset of projects, if specified
        if (projectIds != null
            && !projectIdsList.contains(project.getId().toString())) {
          continue;
        }

        TerminologyHandler handler =
            refsetService.getTerminologyHandler(project, null);

        for (Refset refset : project.getRefsets()) {
          // Only run on subset of refsets, if specified
          if (refsetIds != null
              && !refsetIdsList.contains(refset.getId().toString())) {
            continue;
          }

          getLog().info("About to start updating refset: " + refset.getId());

          // If the refset's branch is invalid (e.g. a very old version, etc.),
          // do not lookup members/synonyms
          // Test by trying to retrieve the top-level Snomed concept:
          // "138875005 | SNOMED CT Concept (SNOMED RT+CTV3) |"
          Concept testConcept = null;

          try {
            testConcept = handler.getConcept("138875005",
                refset.getTerminology(), refset.getVersion());
          } catch (Exception e) {
            // n/a
          }

          if (testConcept == null) {
            continue;
          }

          // handle lazy init
          for (ConceptRefsetMember member : refset.getMembers()) {
            refsetService.handleLazyInit(member);
          }

          int count = 0;
          for (ConceptRefsetMember member : refset.getMembers()) {

            // For any concept that has already had its synonyms lookup up, add
            // them from the cache
            if (conceptVersionSynonymsMap.get(
                member.getConceptId() + "|" + refset.getVersion()) != null) {
              // Create new synonyms based on the cache and wire to this member
              for (ConceptRefsetMemberSynonym synonym : conceptVersionSynonymsMap
                  .get(member.getConceptId() + "|" + refset.getVersion())) {
                ConceptRefsetMemberSynonym newSynonym =
                    new ConceptRefsetMemberSynonymJpa(synonym);
                newSynonym.setMember(member);
                // Make sure to only add unique synonyms. Block duplicates
                if (!member.getSynonyms().contains(newSynonym)) {
                  refsetService.addConceptRefsetMemberSynonym(newSynonym);
                  member.getSynonyms().add(newSynonym);
                }
              }
            }
            // Otherwise, flag the concept as needing synonyms looked up
            else {
              member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
            }
            refsetService.updateMember(member);
            count++;
            if (count % RootService.commitCt == 0) {
              refsetService.commitClearBegin();
            }
          }
          refsetService.commitClearBegin();

          // Okay, you can actually lookup the synonyms now.
          refsetService.lookupMemberNames(refset.getId(),
              "initial population of synonyms for refset=" + refset.getId(),
              false, true);

          refsetService.commitClearBegin();

          // Finally, add all newly looked up synonyms to the cache
          Refset refsetWithSynonyms = refsetService.getRefset(refset.getId());
          for (ConceptRefsetMember member : refsetWithSynonyms.getMembers()) {
            // Create a new synonym and clear out the member associated with it,
            // since it will be different when getting reused
            Set<ConceptRefsetMemberSynonym> synonymsToCache = new HashSet<>();
            for (ConceptRefsetMemberSynonym synonym : member.getSynonyms()) {
              ConceptRefsetMemberSynonym synonymToCache =
                  new ConceptRefsetMemberSynonymJpa(synonym);
              synonymToCache.setMember(null);
              synonymsToCache.add(synonymToCache);
            }
            conceptVersionSynonymsMap.put(
                member.getConceptId() + "|" + refset.getVersion(),
                synonymsToCache);
          }
        }

        getLog().info(" Completed project: " + project.getName());
      }
    }

    getLog().info("Completed operation across all project-refset pairs");
    refsetService.commit();

    // no reindex needed for this patch
    if (!fullReindex) {
      // getLog().info(" Reindex Refset Members");
      //
      // // login as "admin", use token
      // final Properties properties = ConfigUtility.getConfigProperties();
      // try (final SecurityService securityService = new SecurityServiceJpa();)
      // {
      // String authToken =
      // securityService.authenticate(properties.getProperty("admin.user"),
      // properties.getProperty("admin.password")).getAuthToken();
      // ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
      // contentService.luceneReindex("ConceptRefsetMemberJpa", null, null,
      // authToken);
      // }
    }
  }

  /**
   * Adds the synonyms.
   *
   * @param project the project
   * @param conceptSynonymsMapCache the concept synonyms map cache
   * @param refsetService the refset service
   * @throws Exception the exception
   */
  private void addSynonyms(Project project,
    Map<String, Map<String, List<ConceptRefsetMemberSynonym>>> conceptSynonymsMapCache,
    RefsetService refsetService) throws Exception {
    /*-
    *
    * Adds the synonyms as long as they are not Definition Type of Descriptions
    */
    // TerminologyHandler termHandler =
    // refsetService.getTerminologyHandler(project, null);
    //
    // for (Refset refset : project.getRefsets()) {
    // getLog().info("Working on members in refset (" + refset.getId() + "): "
    // + refset.getName() + " with " + refset.getMembers().size()
    // + " members");
    //
    // int count = 0;
    // for (ConceptRefsetMember member : refset.getMembers()) {
    // String conId = member.getConceptId();
    //
    // if (!conceptSynonymsMapCache.keySet().contains(conId)) {
    // // Lookup synonyms and add to member
    // Map<String, List<ConceptRefsetMemberSynonym>> versionSynonymsMap =
    // new HashMap<>();
    // conceptSynonymsMapCache.put(conId, versionSynonymsMap);
    // }
    //
    // if (!conceptSynonymsMapCache.get(conId)
    // .containsKey(refset.getVersion())) {
    // List<ConceptRefsetMemberSynonym> synonyms = new ArrayList<>();
    // conceptSynonymsMapCache.get(conId).put(refset.getVersion(), synonyms);
    // }
    //
    // if (conceptSynonymsMapCache.get(conId).get(refset.getVersion())
    // .isEmpty()) {
    // List<ConceptRefsetMemberSynonym> synonyms =
    // conceptSynonymsMapCache.get(conId).get(refset.getVersion());
    //
    // try {
    // // Identify Concept's synonyms
    // final Concept con = termHandler.getFullConcept(conId,
    // refset.getTerminology(), refset.getVersion());
    //
    // for (Description d : con.getDescriptions()) {
    // if (d.isActive() && !d.getTypeId().equals("900000000000550004") //
    // DEFINITION
    // && !synonyms.contains(d.getTerm())) {
    // if (d.getTerm().length() > 256) {
    // throw new Exception("Description '" + d.getTerm()
    // + "' is longer than permitted lenght of 256. It's size is: "
    // + d.getTerm().length());
    // }
    // synonyms.add(d.getTerm());
    // }
    // }
    //
    // member.setSynonyms(synonyms);
    // refsetService.updateMember(member);
    // } catch (NullPointerException e) {
    // // Have Concept Id that doesn't exist on terminology server
    // getLog().info("Concept id '" + conId + "' not found");
    // }
    // } else {
    // // Have previously completed the Lookup of synonyms. Just need to add
    // // them to member
    // member.setSynonyms(
    // conceptSynonymsMapCache.get(conId).get(refset.getVersion()));
    // refsetService.updateMember(member);
    // }
    //
    // // Commit every 50 to minimize memory utilization
    // if (++count % 50 == 0) {
    // getLog().info("Completed " + count + " out of the "
    // + refset.getMembers().size() + " members to process");
    // try {
    // refsetService.commitClearBegin();
    // } catch (Exception e) {
    // getLog().info(
    // "Failed on committing batch of 50 synonyms with error message: "
    // + e.getMessage());
    // refsetService.setTransactionPerOperation(false);
    // refsetService.beginTransaction();
    // }
    // }
    // }
    //
    // // Commit to catch final (and less-than-fifty) members
    // try {
    // refsetService.commitClearBegin();
    // } catch (Exception e) {
    // getLog().info(
    // "Failed on committing final batch of synonyms with error message: "
    // + e.getMessage());
    // }
    //
    // getLog().info(" Completed refset: " + refset.getName());
    // }
  }

  private void patch20190917(boolean fullReindex) {

    try (ProjectService projectService = new ProjectServiceJpa();) {

      projectService.setTransactionPerOperation(false);
      projectService.beginTransaction();

      projectService.getProjects().getObjects().forEach(project -> {

        // TODO: set project.setTerminologyHandlerUrl too?
        if ("BROWSER".equalsIgnoreCase(project.getTerminologyHandlerKey())
            || "SNOWSTORM"
                .equalsIgnoreCase(project.getTerminologyHandlerKey())) {
          project.setTerminologyHandlerKey("PUBLIC-BROWSER");
          project.setTerminologyHandlerUrl(
              "https://prod-browser.ihtsdotools.org/snowstorm/snomed-ct/v2");
        } else if ("SNOWOWL"
            .equalsIgnoreCase(project.getTerminologyHandlerKey())) {
          project.setTerminologyHandlerKey("AUTHORING-INTL");
        } else if ("SNOWOWL-MS"
            .equalsIgnoreCase(project.getTerminologyHandlerKey())) {
          project.setTerminologyHandlerKey("MANAGED-SERVICE");
        }

        getLog()
            .info("  project = " + project.getId() + ", " + project.getName());
        try {
          projectService.updateProject(project);
        } catch (Exception e) {
          getLog().error(
              "patch20190915 : Failed to update project " + project.getId(), e);
        }
      });

      projectService.commit();

      if (!fullReindex) {
        getLog().info("  Projects");

        // login as "admin", use token
        final Properties properties = ConfigUtility.getConfigProperties();
        try (
            final SecurityService securityService = new SecurityServiceJpa();) {
          String authToken =
              securityService.authenticate(properties.getProperty("admin.user"),
                  properties.getProperty("admin.password")).getAuthToken();
          ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
          contentService.luceneReindex("ProjectJpa", null, null, authToken);
        }
      }

    } catch (Exception e) {
      getLog().error("patch20190915 : Failed to update all projects", e);
    }

  }

  private void patch20190923(TranslationService translationService,
    boolean fullReindex) throws Exception {
    int ct = 0;
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    for (Project prj : translationService.getProjects().getObjects()) {
      Project project = translationService.getProject(prj.getId());
      if (project.getTerminologyHandlerKey().equals("SNOWSTORM")) {
        ct++;
        project.setTerminologyHandlerKey("PUBLIC-BROWSER");
        translationService.updateProject(project);

        if (ct % 100 == 0) {
          getLog().info("projects updated  ct = " + ct);
          translationService.commitClearBegin();
        }
      } else if ("SNOWOWL"
          .equalsIgnoreCase(project.getTerminologyHandlerKey())) {
        project.setTerminologyHandlerKey("AUTHORING-INTL");
      } else if ("SNOWOWL-SE"
          .equalsIgnoreCase(project.getTerminologyHandlerKey())) {
        project.setTerminologyHandlerKey("MANAGED-SERVICE");
      }
    }
    getLog().info("projects updated final ct = " + ct);
    ct = 0;
    for (Translation trans : translationService.getTranslations()
        .getObjects()) {
      Translation translation =
          translationService.getTranslation(trans.getId());
      if (translation.getProject().getTerminologyHandlerKey().equals(
          "PUBLIC-BROWSER") && translation.getVersion().length() == 10) {
        ct++;
        String old_version = translation.getVersion();
        translation.setVersion("MAIN/" + old_version);
        translationService.updateTranslation(translation);

        if (ct % 100 == 0) {
          getLog().info("translations updated  ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("translations updated final ct = " + ct);
    ct = 0;
    for (Refset ref : translationService.getRefsets().getObjects()) {
      Refset refset = translationService.getRefset(ref.getId());
      if (refset.getProject().getTerminologyHandlerKey()
          .equals("PUBLIC-BROWSER") && refset.getVersion().length() == 10) {
        ct++;
        String old_version = refset.getVersion();
        refset.setVersion("MAIN/" + old_version);
        translationService.updateRefset(refset);

        if (ct % 100 == 0) {
          getLog().info("refsets updated ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("refsets updated final ct = " + ct);
    translationService.commit();

    if (!fullReindex) {
      getLog().info("  Projects, Translations, Refsets");

      // login as "admin", use token
      final Properties properties = ConfigUtility.getConfigProperties();
      try (final SecurityService securityService = new SecurityServiceJpa();) {
        String authToken =
            securityService.authenticate(properties.getProperty("admin.user"),
                properties.getProperty("admin.password")).getAuthToken();
        ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
        contentService.luceneReindex("ProjectJpa,TranslationJpa,RefsetJpa",
            null, null, authToken);
      }
    }

  }

  /**
   * Patch 20191009.
   *
   * @param translationService the translation service
   * @param fullReindex the full reindex
   * @throws Exception the exception
   */
  private void patch20191009(TranslationService translationService,
    boolean fullReindex) throws Exception {
    int ct = 0;
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    for (Project prj : translationService.getProjects().getObjects()) {
      Project project = translationService.getProject(prj.getId());
      if (project.getTerminologyHandlerKey().equals("SNOWSTORM")) {
        ct++;
        project.setTerminologyHandlerKey("PUBLIC-BROWSER");
        translationService.updateProject(project);

        if (ct % 100 == 0) {
          getLog().info("projects updated  ct = " + ct);
          translationService.commitClearBegin();
        }
      } else if ("SNOWOWL"
          .equalsIgnoreCase(project.getTerminologyHandlerKey())) {
        project.setTerminologyHandlerKey("AUTHORING-INTL");
      } else if ("SNOWOWL-SE"
          .equalsIgnoreCase(project.getTerminologyHandlerKey())) {
        project.setTerminologyHandlerKey("MANAGED-SERVICE");
      }
    }
    getLog().info("projects updated final ct = " + ct);
    ct = 0;
    for (Translation trans : translationService.getTranslations()
        .getObjects()) {
      Translation translation =
          translationService.getTranslation(trans.getId());
      if ((translation.getProject().getTerminologyHandlerKey()
          .equals("PUBLIC-BROWSER")
          || translation.getProject().getTerminologyHandlerKey()
              .equals("AUTHORING-INTL"))
          && translation.getVersion().length() == 10) {
        ct++;
        String old_version = translation.getVersion();
        if (translation.getTerminology().equals("SNOMEDCT")) {
          translation.setVersion("MAIN/" + old_version);
        } else {
          translation.setVersion(
              "MAIN/" + translation.getTerminology() + "/" + old_version);
        }
        translationService.updateTranslation(translation);

        if (ct % 100 == 0) {
          getLog().info("translations updated  ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("translations updated final ct = " + ct);
    ct = 0;
    for (Refset ref : translationService.getRefsets().getObjects()) {
      Refset refset = translationService.getRefset(ref.getId());
      if ((refset.getProject().getTerminologyHandlerKey()
          .equals("PUBLIC-BROWSER")
          || refset.getProject().getTerminologyHandlerKey()
              .equals("AUTHORING-INTL"))
          && refset.getVersion().length() == 10) {
        ct++;
        String old_version = refset.getVersion();
        if (refset.getTerminology().equals("SNOMEDCT")) {
          refset.setVersion("MAIN/" + old_version);
        } else {
          refset.setVersion(
              "MAIN/" + refset.getTerminology() + "/" + old_version);
        }
        translationService.updateRefset(refset);

        if (ct % 100 == 0) {
          getLog().info("refsets updated ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("refsets updated final ct = " + ct);
    translationService.commit();

    if (!fullReindex) {
      getLog().info("  Projects, Translations, Refsets");

      // login as "admin", use token
      final Properties properties = ConfigUtility.getConfigProperties();
      try (final SecurityService securityService = new SecurityServiceJpa();) {
        String authToken =
            securityService.authenticate(properties.getProperty("admin.user"),
                properties.getProperty("admin.password")).getAuthToken();
        ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
        contentService.luceneReindex("ProjectJpa,TranslationJpa,RefsetJpa",
            null, null, authToken);
      }
    }
  }

  /**
   * Patch 20191125.
   *
   * @param fullReindex the full reindex
   */
  private void patch20191125(boolean fullReindex) {

    try (ProjectService projectService = new ProjectServiceJpa();) {

      projectService.setTransactionPerOperation(false);
      projectService.beginTransaction();

      projectService.getProjects().getObjects().forEach(project -> {

        if (!project.getTerminologyHandlerKey().equals("MANAGED-SERVICE")
            && project.getTerminologyHandlerUrl().endsWith("/v2")) {
          getLog().info("  updating terminology handler URL for project = "
              + project.getId() + ", " + project.getName());
          final String terminologyHandlerUrl =
              project.getTerminologyHandlerUrl().substring(0,
                  project.getTerminologyHandlerUrl().length() - 3);
          project.setTerminologyHandlerUrl(terminologyHandlerUrl);
        }
        try {
          projectService.updateProject(project);
        } catch (Exception e) {
          getLog().error(
              "patch20191125 : Failed to update project " + project.getId(), e);
        }
      });

      projectService.commit();

      if (!fullReindex) {
        getLog().info("  Projects");

        // login as "admin", use token
        final Properties properties = ConfigUtility.getConfigProperties();
        try (
            final SecurityService securityService = new SecurityServiceJpa();) {
          String authToken =
              securityService.authenticate(properties.getProperty("admin.user"),
                  properties.getProperty("admin.password")).getAuthToken();
          ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
          contentService.luceneReindex("ProjectJpa", null, null, authToken);
        }
      }

    } catch (Exception e) {
      getLog().error("patch20191125 : Failed to update all projects", e);
    }

  }

  /**
   * Patch 20200512.
   *
   * @param fullReindex the full reindex
   */
  private void patch20200512(boolean fullReindex) {

    try (ProjectService projectService = new ProjectServiceJpa();) {

      projectService.setTransactionPerOperation(false);
      projectService.beginTransaction();

      projectService.getProjects().getObjects().forEach(project -> {

        if (project.getTerminologyHandlerKey().equals("AUTHORING-INTL")) {
          getLog().info("  updating terminology handler URL for project = "
              + project.getId() + ", " + project.getName());
          project.setTerminologyHandlerUrl("https://prod-snowstorm.ihtsdotools.org/snowstorm/snomed-ct");
        }
        try {
          projectService.updateProject(project);
        } catch (Exception e) {
          getLog().error(
              "patch20200512 : Failed to update project " + project.getId(), e);
        }
      });

      projectService.commit();

      if (!fullReindex) {
        getLog().info("  Projects");

        // login as "admin", use token
        final Properties properties = ConfigUtility.getConfigProperties();
        try (
            final SecurityService securityService = new SecurityServiceJpa();) {
          String authToken =
              securityService.authenticate(properties.getProperty("admin.user"),
                  properties.getProperty("admin.password")).getAuthToken();
          ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
          contentService.luceneReindex("ProjectJpa", null, null, authToken);
        }
      }

    } catch (Exception e) {
      getLog().error("patch20200512 : Failed to update all projects", e);
    }

  }

  /**
   * Patch 20200603.
   *
   * @param translationService the translation service
   * @throws Exception the exception
   */
  private void patch20200603(TranslationService translationService, WorkflowService workflowService, 
    RefsetService refsetService, boolean fullReindex)
    throws Exception {
    int ct = 0;
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    StringBuilder projectIdStringBuilder = new StringBuilder();
    for (Project prj : translationService.getProjects().getObjects()) {
      Project project = translationService.getProject(prj.getId());
      
      // Update SNOMED International Project to be an AUTHORING-INTL project
      if(project.getId().equals(1L)) {
        ct++;
        project.setTerminologyHandlerKey("AUTHORING-INTL");
        project.setTerminologyHandlerUrl("https://prod-snowstorm.ihtsdotools.org/snowstorm/snomed-ct");
        translationService.updateProject(project);
      }
      
      // Change all MANAGED-SERVICE projects to point to prod-snowstorm
      else if (project.getTerminologyHandlerKey().equals("MANAGED-SERVICE")) {
        ct++;
        project.setTerminologyHandlerUrl(
            "https://prod-snowstorm.ihtsdotools.org/snowstorm/snomed-ct");
     
        translationService.updateProject(project);
        projectIdStringBuilder.append(project.getId().toString()).append(",");

        if (ct % 100 == 0) {
          getLog().info("projects updated  ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("projects updated final ct = " + ct);

    
    // Read in from branch map file
    final Map<String, String> refsetIdToBranch = new HashMap<>();
    final Map<String, String> refsetIdToEdition = new HashMap<>();
    System.out.println("input dir: " + input);
    try (final BufferedReader in =
        new BufferedReader(new FileReader(new File(input)))) {
      String line = null;
      while ((line = in.readLine()) != null) {
        String tokens[] = line.split("\t");
        refsetIdToBranch.put(tokens[2], tokens[7]);
        refsetIdToEdition.put(tokens[2], tokens[6]);
        System.out.println("map: " + tokens[2] + " " + tokens[6] + " " + tokens[7]);
      }
    }

    // update MANAGED-SERVICE refset terminology and version branches
    ct = 0;
    for (Refset ref : translationService.getRefsets().getObjects()) {
      Refset refset = translationService.getRefset(ref.getId());
      if (refset.getProject().getTerminologyHandlerKey().equals("MANAGED-SERVICE")) {
        ct++;
        String old_version = refset.getVersion();
        System.out.println("REFSET " + refset.getId() + " Old terminology: " + refset.getTerminology() + " Old version: " + old_version);
        if (old_version.contains("SNOMEDCT-")) {
          Matcher matcher =
              Pattern.compile("(.*)(SNOMEDCT-\\w{2})(.*)").matcher(old_version);
          matcher.find();
          String edition = matcher.group(2);
          refset.setVersion("MAIN/" + edition);
          refset.setTerminology(edition);
          System.out.println("REFSET " + refset.getId() + " New terminology: " + edition + " New version: " + "MAIN/" + edition);
        } else if (refsetIdToBranch.containsKey(refset.getId().toString())) {
          refset.setVersion(refsetIdToBranch.get(refset.getId().toString()));
          refset.setTerminology(refsetIdToEdition.get(refset.getId().toString()));
          System.out.println("REFSET*" + refset.getId() + " New terminology: " + refsetIdToEdition.get(refset.getId().toString()) + " New version: " + refsetIdToBranch.get(refset.getId().toString()));
        } else if (!old_version.contains("SNOMEDCT-") && old_version.indexOf("-") == 9) {
          refset.setVersion("MAIN");
          System.out.println("REFSET " + refset.getId() + " New terminology: " + refset.getTerminology() + " New version: " + "MAIN");
        }
        translationService.updateRefset(refset);

        if (ct % 100 == 0) {
          getLog().info("refsets updated ct = " + ct);
          translationService.commitClearBegin();
        }
      }
    }
    getLog().info("refsets updated final ct = " + ct);
    translationService.commitClearBegin();
    
    // update MANAGED-SERVICE translation terminology and version branches
    ct = 0;
    for (Translation trans : translationService.getTranslations()
        .getObjects()) {
      Translation translation =
          translationService.getTranslation(trans.getId());
      if (translation.getProject().getTerminologyHandlerKey().equals("MANAGED-SERVICE")) {
        ct++;
        String old_version = translation.getVersion();

          System.out.println("TRANSLATION " + translation.getId() + " Old terminology: " + translation.getTerminology() + " Old version: " + old_version );

          Refset underlyingRefset = translation.getRefset();
          translation.setVersion(underlyingRefset.getVersion());
          translation.setTerminology(underlyingRefset.getTerminology());
          translationService.updateTranslation(translation);

          System.out.println("TRANSLATION " + translation.getId() + " New terminology: " + translation.getTerminology() + " New version: " + old_version );

          if (ct % 100 == 0) {
            getLog().info("translations updated ct = " + ct);
            translationService.commitClearBegin();
          }
        
      }
    }
    getLog().info("translations updated final ct = " + ct); 

    translationService.commit();
    
    // rerun all concept name lookups for the projects that were updated
    projectIds = projectIdStringBuilder.toString();
    System.out.println("projectIds " + projectIds);
    patch20200611(workflowService, refsetService, fullReindex);
  } 
  
  /**
   * Patch 20200611.
   *
   * @param workflowService the workflow service
   * @param refsetService the refset service
   * @param fullReindex the full reindex
   * @throws Exception the exception
   */
  private void patch20200611(WorkflowService workflowService,
    RefsetService refsetService, boolean fullReindex) throws Exception {
    Set<Long> uniqueProjects = new HashSet<>();
    // Map of (ConceptId+"|"+RefsetVersion to Set of synonyms)
    Map<String, Set<ConceptRefsetMemberSynonym>> conceptVersionSynonymsMap =
        new HashMap<>();

    // Turn the project/refset id strings into lists, if specified
    List<String> projectIdsList = new ArrayList<>();
    List<String> refsetIdsList = new ArrayList<>();
    if (projectIds != null) {
      projectIdsList = Arrays.asList(projectIds.split(",", -1));
    }
    if (refsetIds != null) {
      refsetIdsList = Arrays.asList(refsetIds.split(",", -1));
    }

    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();

    // identify projects via get operation
    getLog().info("  Identify projects using getProjects()");
    final ProjectList list = workflowService.getProjects();
    for (final Project project : list.getObjects()) {
      if (!uniqueProjects.contains(project.getId())) {
        getLog().info("About to start updating project: " + project.getName());
        uniqueProjects.add(project.getId());

        // Only run on subset of projects, if specified
        if (projectIds != null
            && !projectIdsList.contains(project.getId().toString())) {
          continue;
        }

        TerminologyHandler handler =
            refsetService.getTerminologyHandler(project, null);

        for (Refset refset : project.getRefsets()) {
          // Only run on subset of refsets, if specified
          if (refsetIds != null
              && !refsetIdsList.contains(refset.getId().toString())) {
            continue;
          }

          getLog().info("About to start updating refset: " + refset.getId());

          // If the refset's branch is invalid (e.g. a very old version, etc.),
          // do not lookup members/synonyms
          // Test by trying to retrieve the top-level Snomed concept:
          // "138875005 | SNOMED CT Concept (SNOMED RT+CTV3) |"
          Concept testConcept = null;

          try {
            testConcept = handler.getConcept("138875005",
                refset.getTerminology(), refset.getVersion());
          } catch (Exception e) {
            // n/a
          }

          if (testConcept == null) {
            System.out.println("test concept failed on " + refset.getTerminology() + " " + refset.getVersion());
            continue;
          }

          // handle lazy init
          for (ConceptRefsetMember member : refset.getMembers()) {
            refsetService.handleLazyInit(member);
          }

          int count = 0;
          for (ConceptRefsetMember member : refset.getMembers()) {

            member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
            
            refsetService.updateMember(member);
            count++;
            if (count % RootService.commitCt == 0) {
              refsetService.commitClearBegin();
            }
          }
          refsetService.commitClearBegin();

          // Okay, you can actually lookup the synonyms now.
          refsetService.lookupMemberNames(refset.getId(),
              "initial population of synonyms for refset=" + refset.getId(),
              false, true);

          refsetService.commitClearBegin();

        }

        getLog().info(" Completed project: " + project.getName());
      }
    }

    getLog().info("Completed operation across all project-refset pairs");
    refsetService.commit();

  }

  /**
   * Patch 20200730.
   *
   * @param boolean fullReindex
   * @throws Exception the exception
   */
  private void patch20200730(boolean fullReindex) {
    
    try (final ProjectService projectService = new ProjectServiceJpa();
        final SecurityService securityService = new  SecurityServiceJpa()) {
      
      final List<String> excludeUsernameList = 
          Arrays.asList("aatkinson", "tshird", "rdavidson","cmorris", "jschofield", "bcarlsen","rwood", 
              "nmarques", "dshapiro","jefron"); 
      
      List<Project> projects = projectService.getProjects().getObjects();
      
      for(Project project : projects) {

        getLog().info("Project " + project.getName());
        Project projectCopy = new ProjectJpa(project);

        if ("AUTHORING-INTL".equalsIgnoreCase(projectCopy.getTerminologyHandlerKey())
            || "MANAGED-SERVICE".equalsIgnoreCase(projectCopy.getTerminologyHandlerKey())) {
          
          try {
            projectCopy.getUserRoleMap().forEach((user, userRole) -> {
              User userCopy = null;
              try {
                userCopy = new UserJpa(securityService.getUser(user.getUserName()));
              }
              catch (Exception e) {
                //noop
              }
              
              if (!excludeUsernameList.contains(user.getUserName()) && userRole == UserRole.ADMIN) {
                
                getLog().info("  Update " + user.getUserName() + " to " + UserRole.LEAD.toString());
                
                try {
                  
                  if (userCopy != null) {                          
                    projectCopy.getUserRoleMap().remove(userCopy, UserRole.ADMIN);
                    projectCopy.getUserRoleMap().put(userCopy, UserRole.LEAD);
                    projectCopy.setLastModifiedBy("nmarques");
                    projectService.updateProject(projectCopy);
                    userCopy.getProjectRoleMap().remove(projectCopy, UserRole.ADMIN);
                    userCopy.getProjectRoleMap().put(projectCopy, UserRole.LEAD);
                    securityService.updateUser(userCopy);
                  }
                                  
                } catch (Exception e) {
                  getLog().error(
                      "patch20200730 : Failed to update ", e);
                }              
              }
            });
          } catch (ConcurrentModificationException cme) {
            getLog().info(cme.getMessage());
          }
        }
      }

      if (!fullReindex) {
        getLog().info("  Projects, Users");

        // login as "admin", use token
        final Properties properties = ConfigUtility.getConfigProperties();
        String authToken =
            securityService.authenticate(properties.getProperty("admin.user"),
                properties.getProperty("admin.password")).getAuthToken();
        ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
        contentService.luceneReindex("ProjectJpa, UserJpa", null, null, authToken);
      }

    } catch (Exception e) {
      getLog().error("patch20200730 : Failed to update all projects", e);
    }
  }

  
  private void patch20200805(WorkflowService workflowService,
    TranslationService translationService) throws Exception {
    getLog().info("Patch 20200805: Updating Norway language codes");
    int ct = 0;
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    for (Translation trans : translationService.getTranslations()
        .getObjects()) {
      Translation translation =
          translationService.getTranslation(trans.getId());

      getLog().info("translation " + translation.getName() + " " + translation.getLanguage());
      translationService.setTransactionPerOperation(false);
      String oldLanguage = translation.getLanguage();
      if (oldLanguage.contentEquals("nb") || oldLanguage.equals("nn") || 
          oldLanguage.contentEquals("nb_NO") || oldLanguage.equals("nn_NO")) {
        getLog().info("Updating translation language code " + translation.toString());
        if (oldLanguage.length() == 2) {
          translation.setLanguage(oldLanguage + "-NO");
        } else if (oldLanguage.length() == 5) {
          translation.setLanguage(oldLanguage.replace('_', '-'));
        }
        for (Concept cpt : translation.getConcepts()) {
          Concept concept = translationService.getConcept(cpt.getId());
          if (concept == null) {
            continue;
          }
          for (Description desc : concept.getDescriptions()) {
            desc.setLanguageCode(translation.getLanguage());
            translationService.updateDescription(desc);
          }
          translationService.updateConcept(concept);
          ct++;
          if (ct % 100 == 0) {
            getLog().info("  ct = " + ct);
            translationService.commitClearBegin();
          }
        }
      }
    }
    translationService.commit();
  }
}