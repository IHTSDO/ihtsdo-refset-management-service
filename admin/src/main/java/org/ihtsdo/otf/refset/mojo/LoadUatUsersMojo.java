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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.services.SecurityService;

/**
 * Goal which loads UAT users.
 * 
 * See admin/pom.xml for sample usage
 * 
 * @goal load-uat-users
 * @phase package
 */
public class LoadUatUsersMojo extends AbstractMojo {

  /**
   * UAT Users file.
   * @parameter
   */
  private String file;

  /**
   * Instantiates an empty {@link LoadUatUsersMojo}.
   */
  public LoadUatUsersMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    try {
      getLog().info("Load UAT users");
      getLog().info("  file = " + file);

      final SecurityService service = new SecurityServiceJpa();
      final BufferedReader in =
          new BufferedReader(new FileReader(new File(file)));
      String line;
      while ((line = in.readLine()) != null) {
        final String[] tokens = FieldedStringTokenizer.split(line, "|");
        // Sample line:
        // USER|***REMOVED***|Brian Carlsen|bcarlsen|
        final User user = new UserJpa();
        user.setApplicationRole(UserRole.valueOf(tokens[0]));
        user.setEmail(tokens[1]);
        user.setName(tokens[2]);
        user.setUserName(tokens[3]);
        getLog().info("   Add " + user);
        service.addUser(user);
      }
      in.close();
      service.close();
      getLog().info("Done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

}