/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Properties;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.rf2.SimpleRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;

/**
 * Implementation of {@link IdentifierAssignmentHandler} that interacts with the
 * IHTSDO component identifier service.
 */
public class IhtsdoComponentIdentifierServiceHandler implements
    IdentifierAssignmentHandler {

  /** The url. */
  @SuppressWarnings("unused")
  private String url;

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // Obtain URL for the component id service
    if (p.containsKey("url")) {
      this.url = p.getProperty("url");
    } else {
      throw new Exception("Required property url not specified.");
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Concept concept) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Description description) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Relationship relationship) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(DescriptionTypeRefsetMember member)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(LanguageRefsetMember member) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(ModuleDependencyRefsetMember member)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(RefsetDescriptorRefsetMember member)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(SimpleRefsetMember member) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public boolean allowIdChangeOnUpdate() {
    // TODO Auto-generated method stub
    return false;
  }

  /* see superclass */
  @Override
  public boolean allowConceptIdChangeOnUpdate() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getName() {
    return "IHTSDO Component Identifier Service handler";
  }

  @Override
  public String getTerminologyId(Refset refset) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(Translation translation) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
