/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.mojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.services.RootService;
import org.ihtsdo.otf.refset.services.handlers.ExceptionHandler;

/**
 * QA Check for Properly Numbered Map Groups
 * 
 * See admin/qa/pom.xml for a sample execution.
 * 
 */
@Mojo(name = "qa-database", defaultPhase = LifecyclePhase.PACKAGE)
public class QaDatabase extends AbstractRttMojo {

  /** The queries. */
  @Parameter(required = true)
  private Properties queries;

  /** The manager. */
  EntityManager manager;

  /**
   * Executes the plugin.
   *
   * @throws MojoExecutionException the mojo execution exception
   * @throws MojoFailureException the mojo failure exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Starting database QA");

    try {
      setupBindInfoPackage();

      // Obtain an entity manager;
      RootService service = new RootServiceJpa() {
        {
          QaDatabase.this.manager = manager;
        }

        @Override
        public void refreshCaches() throws Exception {
          // n/a
        }
      };

      Map<String, List<String>> errors = new HashMap<>();

      // Iterate through queries, execute and report
      for (Object property : queries.keySet()) {
        String queryStr =
            queries.getProperty(property.toString()).replace(";", "");
        getLog().info("  " + property);
        getLog().info("    " + queryStr);

        // Get and execute query (truncate any trailing semi-colon)
        Query query = manager.createNativeQuery(queryStr);
        query.setMaxResults(10);
        List<Object[]> objects = query.getResultList();

        // Expect zero count, any results are failures
        if (objects.size() > 0) {
          List<String> results = new ArrayList<>();
          for (Object[] array : objects) {
            StringBuilder sb = new StringBuilder();
            for (Object o : array) {
              sb.append((o != null ? o.toString() : "null")).append(",");
            }
            results.add(sb.toString().replace(",$", ""));
          }
          errors.put(property.toString(), results);
        }

      }

      // Check for errors and report the
      if (!errors.isEmpty()) {
        StringBuilder msg = new StringBuilder();
        msg.append("\r\n");
        msg.append(
            "The automated database QA mojo has found some issues with the following checks:\r\n");
        msg.append("\r\n");

        for (String key : errors.keySet()) {
          msg.append("  CHECK: ").append(key).append("\r\n");
          msg.append("  QUERY: ").append(queries.getProperty(key))
              .append("\r\n");
          for (String result : errors.get(key)) {
            msg.append("    " + result).append("\r\n");
          }
          if (errors.get(key).size() > 9) {
            msg.append("    ... ");
            // the true count is not known because setMaxResults(10) is used.
          }

        }

        Properties config = ConfigUtility.getConfigProperties();
        if (config.getProperty("mail.enabled") != null
            && config.getProperty("mail.enabled").equals("true")
            && config.getProperty("mail.smtp.to") != null) {
          String from = null;
          if (config.containsKey("mail.smtp.from")) {
            from = config.getProperty("mail.smtp.from");
          } else {
            from = config.getProperty("mail.smtp.user");
          }

          ConfigUtility.sendEmail("[Refset Server] Database QA Results", from,
              config.getProperty("mail.smtp.to"), msg.toString(), config,
              "true".equals(config.get("mail.smtp.auth")));
        }

      } else {
        getLog().info("  NO errors");
      }

      // cleanup
      service.close();
      getLog().info("Done ...");

    } catch (Exception e) {
      // Send email if something went wrong
      try {
        ExceptionHandler.handleException(e, "Error running QA Checks");
      } catch (Exception e1) {
        Logger.getLogger(getClass()).error("Unexpected exception", e);
        throw new MojoFailureException(e.getMessage());
      }

    }

  }
}