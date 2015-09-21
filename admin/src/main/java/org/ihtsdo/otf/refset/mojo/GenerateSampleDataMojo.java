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

import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;

/**
 * Goal which generates sample data in an empty database. Uses JPA services
 * directly, no need for REST layer.
 * 
 * See admin/pom.xml for sample usage
 * 
 * @goal sample-data
 * @phase package
 */
public class GenerateSampleDataMojo extends AbstractMojo {

  /**
   * Instantiates a {@link GenerateSampleDataMojo} from the specified
   * parameters.
   */
  public GenerateSampleDataMojo() {
    // do nothing
  }

  /* see superclass */
  @SuppressWarnings("unused")
  @Override
  public void execute() throws MojoFailureException {
    getLog().info("Start generating sample data");

    try {

      Properties properties = ConfigUtility.getConfigProperties();

      // TODO:
      // Create users
      // Create projects
      // Create refsets
      // Create Translations
      //

      //
      // consider different versions of same edition
      // consider different editions
      // consider with/without translations
      // consider multiple translations per refset
      // ..etc
      // consider intensional, extensional, external
      //

      getLog().info("Done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }
}
