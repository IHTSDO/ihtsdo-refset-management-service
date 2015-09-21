/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefSetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefSetMember;

/**
 * Generically represents a listener for workflow actions.
 */
public interface WorkflowListener extends Configurable {

  /**
   * Represents change actions on components.
   */
  public enum Action {

    /** The add. */
    ADD,
    /** The remove. */
    REMOVE,
    /** The update. */
    UPDATE
  }

  /**
   * Notification of transaction starting.
   *
   * @throws Exception the exception
   */
  public void beginTransaction() throws Exception;

  /**
   * Notification pre-commit.
   *
   * @throws Exception the exception
   */
  public void preCommit() throws Exception;

  /**
   * Notification post-commit.
   *
   * @throws Exception the exception
   */
  public void postCommit() throws Exception;

  /**
   * Classification started.
   *
   * @throws Exception the exception
   */
  public void classificationStarted() throws Exception;

  /**
   * Classification finished.
   *
   * @throws Exception the exception
   */
  public void classificationFinished() throws Exception;

  /**
   * Pre classification started.
   *
   * @throws Exception the exception
   */
  public void preClassificationStarted() throws Exception;

  /**
   * Pre classification finished.
   *
   * @throws Exception the exception
   */
  public void preClassificationFinished() throws Exception;

  // TODO: need "content changed" events for refset/translation

  /**
   * Metadata changed.
   */
  public void metadataChanged();

  /**
   * Notification of a cancelled operation.
   */
  public void cancel();

  /**
   * Refset changed.
   *
   * @param refset the refset
   * @param action the action
   * @throws Exception the exception
   */
  public void refsetChanged(Refset refset, Action action) throws Exception;

  /**
   * Translation changed.
   *
   * @param translation the translation
   * @param action the action
   * @throws Exception the exception
   */
  public void translationChanged(Translation translation, Action action)
    throws Exception;

  /**
   * Refset descriptor ref set member changed.
   *
   * @param member the member
   * @param action the action
   * @throws Exception the exception
   */
  public void refsetDescriptorRefSetMemberChanged(
    RefsetDescriptorRefSetMember member, Action action) throws Exception;

  /**
   * Description type ref set member changed.
   *
   * @param member the member
   * @param action the action
   * @throws Exception the exception
   */
  public void descriptionTypeRefSetMemberChanged(
    DescriptionTypeRefSetMember member, Action action) throws Exception;
}
