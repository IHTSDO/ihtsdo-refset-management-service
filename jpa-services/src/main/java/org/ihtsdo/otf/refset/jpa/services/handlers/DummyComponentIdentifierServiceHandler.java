/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Properties;
import java.util.UUID;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;

/**
 * Implementation of {@link IdentifierAssignmentHandler} that interacts with the
 * IHTSDO component identifier service.
 */
public class DummyComponentIdentifierServiceHandler implements
    IdentifierAssignmentHandler {

  /** The ct. */
  private int ct = 111111;

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Concept concept) throws Exception {
    if (concept.getTerminologyId() != null) {
      return concept.getTerminologyId();
    }
    ct++;
    return ct + "000";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Description description) throws Exception {
    if (description.getTerminologyId() != null) {
      return description.getTerminologyId();
    }
    ct++;
    return ct + "011";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(DescriptionType member) throws Exception {
    if (member.getTerminologyId() != null
        && !member.getTerminologyId().isEmpty()) {
      return member.getTerminologyId();
    }
    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(LanguageRefsetMember member) throws Exception {
    if (member.getTerminologyId() != null
        && !member.getTerminologyId().isEmpty()) {
      return member.getTerminologyId();
    }
    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(ModuleDependencyRefsetMember member)
    throws Exception {
    if (member.getTerminologyId() != null
        && !member.getTerminologyId().isEmpty()) {
      return member.getTerminologyId();
    }
    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(RefsetDescriptorRefsetMember member)
    throws Exception {
    if (member.getTerminologyId() != null
        && !member.getTerminologyId().isEmpty()) {
      return member.getTerminologyId();
    }
    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(ConceptRefsetMember member) throws Exception {
    if (member.getTerminologyId() != null
        && !member.getTerminologyId().isEmpty()) {
      return member.getTerminologyId();
    }
    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Refset refset) throws Exception {
    // If already assigned, reuse it
    if (refset.getTerminologyId() != null
        && !refset.getTerminologyId().isEmpty()) {
      return refset.getTerminologyId();
    }
    // Reuse concept logic
    Concept concept = new ConceptJpa();
    Translation translation = new TranslationJpa();
    concept.setTranslation(translation);
    translation.setRefset(refset);
    translation.setProject(refset.getProject());
    return getTerminologyId(concept);
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Translation translation) throws Exception {
    // If already assigned, reuse it
    if (translation.getTerminologyId() != null
        && !translation.getTerminologyId().isEmpty()) {
      return translation.getTerminologyId();
    }

    return UUID.randomUUID().toString();
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

  /* see superclass */
  @Override
  public String getName() {
    return "Dummy Component Identifier Service handler";
  }

}
