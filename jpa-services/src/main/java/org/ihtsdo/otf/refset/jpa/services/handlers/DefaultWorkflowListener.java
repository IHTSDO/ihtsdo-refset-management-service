/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Properties;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

/**
 * A sample validation check for a new concept meeting the minimum qualifying
 * criteria.
 */
public class DefaultWorkflowListener implements WorkflowListener {

  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  @Override
  public void beginTransaction() throws Exception {
    // n/a
  }

  @Override
  public void preCommit() throws Exception {
    // n/a

  }

  @Override
  public void postCommit() throws Exception {
    // n/a

  }

  @Override
  public void cancel() {
    // n/a

  }

  @Override
  public void refsetChanged(Refset refset, Action action) throws Exception {
    // n/a
  }

  @Override
  public void translationChanged(Translation translation, Action action)
    throws Exception {
    // n/a
  }

  @Override
  public void refsetDescriptorRefsetMemberChanged(
    RefsetDescriptorRefsetMember member, Action action) throws Exception {
    // n/a
  }

  @Override
  public void descriptionTypeRefsetMemberChanged(
    DescriptionType member, Action action) throws Exception {
    // n/a
  }

  @Override
  public void conceptChanged(Concept concept, Action action) throws Exception {
    // n/a
  }

  @Override
  public void memberChanged(ConceptRefsetMember member, Action action)
    throws Exception {
    // n/a
  }

  @Override
  public String getName() {
    return "Default workflow listener";
  }
}
