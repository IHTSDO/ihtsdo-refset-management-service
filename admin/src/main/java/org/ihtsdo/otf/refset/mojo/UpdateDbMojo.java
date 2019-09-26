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
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;

/**
 * Goal which updates the db to sync it with the model via JPA.
 * 
 * See admin/pom.xml for sample usage
 * 
 */
@Mojo( name = "updatedb", defaultPhase = LifecyclePhase.PACKAGE)
public class UpdateDbMojo extends AbstractMojo {

  /**
   * Mode: create or update
   */
  @Parameter( required = true )	
  public String mode;

  /**
   * Instantiates a {@link UpdateDbMojo} from the specified parameters.
   * 
   */
  public UpdateDbMojo() {
    // do nothing
  }

  @Override
  public void execute() throws MojoFailureException {
    getLog().info("Start updating database schema...");
    getLog().info("  mode = " + mode);
    
    setupBindInfoPackage();
    
    try {
      if (!mode.equals("update") && !mode.equals("create")) {
        throw new Exception("Mode has illegal value: " + mode);
      }
      Properties config = ConfigUtility.getConfigProperties();
      config.setProperty("hibernate.hbm2ddl.auto", mode);

      // Trigger a JPA event
      new RootServiceJpa() {
        @Override
        public void refreshCaches() throws Exception {
          // n/a
        }
      }.close();
      getLog().info("done ...");
    } catch (Exception e) {
      Logger.getLogger(getClass()).error("Unexpected exception", e);
      throw new MojoFailureException("Unexpected exception:", e);
    }

  }
  
  void setupBindInfoPackage() {
    String nsuri = "http://www.hibernate.org/xsd/orm/hbm";
    String packageInfoClassName = "org.hibernate.boot.jaxb.hbm.spi.package-info";
    getLog().info("  runing setup bind info package");
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
