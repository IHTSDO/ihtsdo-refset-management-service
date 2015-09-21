/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;

/**
 * JAXB enabled implementation of {@link ProjectList}.
 */
@XmlRootElement(name = "projectList")
public class ProjectListJpa extends AbstractResultList<Project> implements
    ProjectList {

  /* see superclass */
  @Override
  @XmlElement(type = ProjectJpa.class, name = "project")
  public List<Project> getObjects() {
    return super.getObjectsTransient();
  }

}
