/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Properties;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefSetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefSetMember;
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
  public void classificationStarted() throws Exception {
    // n/a

  }

  @Override
  public void classificationFinished() throws Exception {
    // n/a

  }

  @Override
  public void preClassificationStarted() throws Exception {
    // n/a

  }

  @Override
  public void preClassificationFinished() throws Exception {
    // n/a

  }

  @Override
  public void cancel() {
    // n/a

  }

  @Override
  public void metadataChanged() {
    // n/a

  }

  @Override
  public String getName() {
    return "Default workflow listener";
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
  public void refsetDescriptorRefSetMemberChanged(
    RefsetDescriptorRefSetMember member, Action action) throws Exception {
    // n/a
  }

  @Override
  public void descriptionTypeRefSetMemberChanged(
    DescriptionTypeRefSetMember member, Action action) throws Exception {
    // n/a
  }

}
