/**
 * Copyright 2015 West Coast Informatics, LLC 
 */
package org.ihtsdo.otf.refset;

import java.util.List;
import java.util.Map;

import org.ihtsdo.otf.refset.helpers.Searchable;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Generically represents a set of translated concepts based on a corresponding
 * {@link Refset}. NOTE: the "terminology" and "version" indicate the details of
 * the "edition" this translation is computed or maintained against. The
 * "effective time" indicates its release version. The "editionUrl" provides an
 * alternative referencing mechanism.
 */
public interface Translation extends Component, Searchable {

  /**
   * Enum of staging types.
   */
  public enum StagingType {
    /** The import. */
    IMPORT,

    /** The migration. */
    MIGRATION,

    /** The preview. */
    PREVIEW;
  }

  /**
   * Returns the description.
   * 
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * 
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Checks if the project is viewable by public roles.
   *
   * @return true, if is public
   */
  public boolean isPublic();

  /**
   * Sets whether the project is viewable by public roles.
   *
   * @param isPublic the new public
   */
  public void setPublic(boolean isPublic);

  /**
   * Indicates whether or not staged is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isStaged();

  /**
   * Sets the staged flag;.
   *
   * @param staged the staged
   */
  public void setStaged(boolean staged);

  /**
   * Returns the staging type.
   *
   * @return the staging type
   */
  public StagingType getStagingType();

  /**
   * Sets the staging type.
   *
   * @param type the staging type
   */
  public void setStagingType(StagingType type);

  /**
   * Returns the language.
   *
   * @return the language
   */
  public String getLanguage();

  /**
   * Sets the language.
   *
   * @param language the language
   */
  public void setLanguage(String language);

  /**
   * Returns the workflow status.
   *
   * @return the workflow status
   */
  public WorkflowStatus getWorkflowStatus();

  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the workflow status
   */
  public void setWorkflowStatus(WorkflowStatus workflowStatus);

  /**
   * Returns the workflow path.
   *
   * @return the workflow path
   */
  public String getWorkflowPath();

  /**
   * Sets the workflow path.
   *
   * @param workflowPath the workflow path
   */
  public void setWorkflowPath(String workflowPath);

  /**
   * Returns the refset.
   *
   * @return the refset
   */
  public Refset getRefset();

  /**
   * Sets the refset.
   *
   * @param refset the refset
   */
  public void setRefset(Refset refset);

  /**
   * Returns the project.
   *
   * @return the project
   */
  public Project getProject();

  /**
   * Sets the project.
   *
   * @param project the project
   */
  public void setProject(Project project);

  /**
   * Returns the description types.
   *
   * @return the description types
   */
  public List<DescriptionType> getDescriptionTypes();

  /**
   * Sets the description types.
   *
   * @param types the description types
   */
  public void setDescriptionTypes(List<DescriptionType> types);

  /**
   * Returns the case sensitive types.
   *
   * @return the case sensitive types
   */
  public Map<String, String> getCaseSensitiveTypes();

  /**
   * Returns the case sensitive types.
   *
   * @param types the types
   */
  public void setCaseSensitiveTypes(Map<String, String> types);

  /**
   * Returns the concepts.
   *
   * @return the concepts
   */
  public List<Concept> getConcepts();

  /**
   * Sets the concepts.
   *
   * @param concepts the concepts
   */
  public void setConcepts(List<Concept> concepts);

  /**
   * Returns the spelling dictionary.
   *
   * @return the spelling dictionary
   */
  public SpellingDictionary getSpellingDictionary();

  /**
   * Sets the spelling dictionary.
   *
   * @param dictionary the spelling dictionary
   */
  public void setSpellingDictionary(SpellingDictionary dictionary);

  /**
   * Returns the spelling dictionary entry count.
   *
   * @return the spelling dictionary entry count
   */
  public int getSpellingDictionarySize();

  /**
   * Returns the phrase memory entry count.
   *
   * @return the phrase memory entry count
   */
  public int getPhraseMemorySize();

  /**
   * Returns the phrase memory.
   *
   * @return the phrase memory
   */
  public PhraseMemory getPhraseMemory();

  /**
   * Sets the phrase memory.
   *
   * @param phraseMemory the phrase memory
   */
  public void setPhraseMemory(PhraseMemory phraseMemory);

  /**
   * Indicates whether or not provisional is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isProvisional();

  /**
   * Sets the provisional.
   *
   * @param provisional the provisional
   */
  public void setProvisional(boolean provisional);

  /**
   * Returns the notes.
   *
   * @return the notes
   */
  public List<Note> getNotes();

  /**
   * Sets the notes.
   *
   * @param notes the notes
   */
  public void setNotes(List<Note> notes);

  /**
   * Indicates whether or not in publication process is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isInPublicationProcess();

  /**
   * Sets the in publication process.
   *
   * @param inPublicationProcess the in publication process
   */
  public void setInPublicationProcess(boolean inPublicationProcess);

  /**
   * Updates whether a lookup of member concepts names and statuses is in
   * progress.
   *
   * @param lookupInProgress the lookup in progress
   */
  public void setLookupInProgress(boolean lookupInProgress);

  /**
   * Indicates whether or not lookup of member concepts names and statuses
   * process is the case.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isLookupInProgress();

}
