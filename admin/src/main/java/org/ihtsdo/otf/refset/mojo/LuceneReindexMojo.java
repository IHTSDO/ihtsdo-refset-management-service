/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.mojo;

import java.text.MessageFormat;
import java.util.Properties;

import javax.xml.bind.annotation.XmlSchema;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.impl.ProjectServiceRestImpl;
import org.ihtsdo.otf.refset.services.SecurityService;

/**
 * Goal which makes lucene indexes based on hibernate-search annotations.
 * 
 * See admin/pom.xml for sample usage
 *
 */
@Mojo(name = "reindex", defaultPhase = LifecyclePhase.PACKAGE)
public class LuceneReindexMojo extends AbstractMojo {

  /**
   * The specified objects to index.
   *
   */
  @Parameter
  private String indexedObjects;

  /**
   * Batch size to load objects for Full Text Entity Manager. Default is 100.
   */
  @Parameter
  private Integer batchSizeToLoadObjects;

  /**
   * Threads used for loading objects for Full Text Entity Manager. Default is
   * 4.
   */
  @Parameter
  private Integer threadsToLoadObjects;

  /**
   * Whether to run this mojo against an active server.
   *
   */
  @Parameter
  private boolean server = false;

  /**
   * Instantiates a {@link LuceneReindexMojo} from the specified parameters.
   */
  public LuceneReindexMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    setupBindInfoPackage();
    
    try {
      getLog().info("Lucene reindexing called via mojo.");
      getLog().info("  Indexed objects : " + indexedObjects);
      getLog().info("  Expect server up: " + server);
      Properties properties = ConfigUtility.getConfigProperties();

      boolean serverRunning = ConfigUtility.isServerActive();

      getLog()
          .info("Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));

      if (serverRunning && !server) {
        throw new MojoFailureException(
            "Mojo expects server to be down, but server is running");
      }

      if (!serverRunning && server) {
        throw new MojoFailureException(
            "Mojo expects server to be running, but server is down");
      }

      // authenticate
      SecurityService service = new SecurityServiceJpa();
      String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      if (!serverRunning) {
        getLog().info("Running directly");

        ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
        contentService.luceneReindex(indexedObjects, batchSizeToLoadObjects,
            threadsToLoadObjects, authToken);

      } else {
        getLog().info("Running against server");

        ProjectClientRest client = new ProjectClientRest(properties);
        client.luceneReindex(indexedObjects, batchSizeToLoadObjects,
            threadsToLoadObjects, authToken);
      }

    } catch (Exception e) {
      Logger.getLogger(getClass()).error("Unexpected exception", e);
      throw new MojoFailureException("Unexpected exception:", e);
    }

  }

  void setupBindInfoPackage() {
    String nsuri = "http://www.hibernate.org/xsd/orm/hbm";
    String packageInfoClassName = "org.hibernate.boot.jaxb.hbm.spi.package-info";
    getLog().info("  running setup bind info package");
    
    try {
        final Class<?> packageInfoClass = Class
                .forName(packageInfoClassName);
        final XmlSchema xmlSchema = packageInfoClass
                .getAnnotation(XmlSchema.class);
        if (xmlSchema == null) {
            this.getLog().warn(MessageFormat.format(
                    "Class [{0}] is missing the [{1}] annotation. Processing bindings will probably fail.",
                    packageInfoClassName, XmlSchema.class.getName()));
        } else {
            final String namespace = xmlSchema.namespace();
            if (nsuri.equals(namespace)) {
                this.getLog().warn(MessageFormat.format(
                        "Namespace of the [{0}] annotation does not match [{1}]. Processing bindings will probably fail.",
                        XmlSchema.class.getName(), nsuri));
            }
        }
    } catch (ClassNotFoundException cnfex) {
        this.getLog().warn(MessageFormat.format(
                "Class [{0}] could not be found. Processing bindings will probably fail.",
                packageInfoClassName), cnfex);
    }
}  
  
}
