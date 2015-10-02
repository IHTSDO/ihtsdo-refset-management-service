/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Properties;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
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
    concept.getTranslation().getRefset().getProject().getNamespace();
    // partitionId 00 or 10
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Description description) throws Exception {
    description.getConcept().getTranslation().getRefset().getProject()
        .getNamespace();
    // partitionId 01 or 11
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(DescriptionTypeRefsetMember member)
    throws Exception {
    // UUID.randomUUID().toString();
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(LanguageRefsetMember member) throws Exception {
    // UUID.randomUUID().toString();
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(ModuleDependencyRefsetMember member)
    throws Exception {
    // UUID.randomUUID().toString();
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(RefsetDescriptorRefsetMember member)
    throws Exception {
    // UUID.randomUUID().toString();
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(ConceptRefsetMember member) throws Exception {
    // UUID.randomUUID().toString();
    return null;
  }

  /* see superclass */
  @Override
  public boolean allowIdChangeOnUpdate() {
    return false;
  }

  /* see superclass */
  @Override
  public boolean allowConceptIdChangeOnUpdate() {
    return false;
  }

  @Override
  public String getName() {
    return "IHTSDO Component Identifier Service handler";
  }

  @Override
  public String getTerminologyId(Refset refset) throws Exception {
    // use concept id assignment
    // getProject().getNamespace()
    return null;
  }

  @Override
  public String getTerminologyId(Translation translation) throws Exception {
    // must be user assigned, simply return translation.getTerminologyId();
    // may also use getRefset().getTerminologyId()
    //
    return null;
  }

}
