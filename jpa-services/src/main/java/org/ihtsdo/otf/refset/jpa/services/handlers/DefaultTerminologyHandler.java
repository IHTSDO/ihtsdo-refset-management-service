/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.DescriptionList;
import org.ihtsdo.otf.refset.helpers.HasLastModified;
import org.ihtsdo.otf.refset.helpers.LanguageRefSetMemberList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.SearchResult;
import org.ihtsdo.otf.refset.helpers.SimpleRefSetMemberList;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

/**
 * Default implementation of {@link TerminologyHandler}. Leverages the IHTSDO
 * terminology server to the extent possible for interacting with terminology
 * components. Uses local storage where not possible.
 */
public class DefaultTerminologyHandler extends RootServiceJpa implements TerminologyHandler {



  /** The listeners enabled. */
  private boolean listenersEnabled = true;

  /** The config properties. */
  private static Properties config = null;

  /** The assign identifiers flag. */
  protected boolean assignIdentifiersFlag = false;

  /** The last modified flag. */
  protected boolean lastModifiedFlag = false;

  /** The listener. */
  private static List<WorkflowListener> listeners = null;
  static {
    listeners = new ArrayList<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "workflow.listener.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        WorkflowListener handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, WorkflowListener.class);
        listeners.add(handlerService);
      }

    } catch (Exception e) {
      e.printStackTrace();
      listeners = null;
    }
  }

  /** The id assignment handler . */
  public static Map<String, IdentifierAssignmentHandler> idHandlerMap =
      new HashMap<>();
  static {

    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "identifier.assignment.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        IdentifierAssignmentHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, IdentifierAssignmentHandler.class);
        idHandlerMap.put(handlerName, handlerService);
      }
    } catch (Exception e) {
      e.printStackTrace();
      idHandlerMap = null;
    }
  }

  public DefaultTerminologyHandler() throws Exception {
    super();
    // TODO Auto-generated constructor stub
  } 
  // TODO: get rid of branch and plural gets
  
  /**  The url. */
  @SuppressWarnings("unused")
  private String url;

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p.containsKey("url")) {
      this.url = p.getProperty("url");
    } else {
      throw new Exception("Required property url not specified.");
    }

  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return "Default terminology handler";
  }

  
  /* see superclass */
  @Override
  public List<SearchResult> findConceptsForTranslation(String translationId,
    String terminology, String version, String query, PfsParameter pfs)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public List<SearchResult> findMembersForRefset(String refsetId,
    String terminology, String version, String query, PfsParameter pfs)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Concept addConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add concept " + concept.getTerminologyId());
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = idHandlerMap.get(concept.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + concept.getTerminology());
      }
      String id = idHandler.getTerminologyId(concept);
      concept.setTerminologyId(id);
    }
    if (assignIdentifiersFlag && idHandler == null) {
      throw new Exception("Unable to find id handler for "
          + concept.getTerminology());
    }
    // Process Cascade.ALL data structures
    Date date = new Date();
    for (Description description : concept.getDescriptions()) {
      if (assignIdentifiersFlag) {
        String id = idHandler.getTerminologyId(description);
        description.setTerminologyId(id);
      }
      if (lastModifiedFlag) {
        description.setLastModified(date);
        description.setLastModifiedBy(concept.getLastModifiedBy());
      }
      for (LanguageRefSetMember member : description.getLanguageRefSetMembers()) {
        if (assignIdentifiersFlag) {
          String id = idHandler.getTerminologyId(member);
          member.setTerminologyId(id);
        }
        if (lastModifiedFlag) {
          member.setLastModifiedBy(concept.getLastModifiedBy());
          member.setLastModified(date);
        }
      }
    }
    for (Relationship relationship : concept.getRelationships()) {
      if (assignIdentifiersFlag) {
        String id = idHandler.getTerminologyId(relationship);
        relationship.setTerminologyId(id);
      }
      if (lastModifiedFlag) {
        relationship.setLastModifiedBy(concept.getLastModifiedBy());
        relationship.setLastModified(date);
      }

    }

    // Add component
    Concept newConcept = addComponent(concept);

    // Inform listeners
    // TODO:
    /*if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.conceptAdded(newConcept);
      }
    }*/
    return newConcept;
  }

  @Override
  public Concept getConcept(Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList getConcepts(String terminologyId, String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateConcept(Concept concept) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeConcept(Long id) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Description addDescription(Description description) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Description getDescription(Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Description getDescription(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DescriptionList getDescriptions(String terminologyId,
    String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateDescription(Description description) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeDescription(Long id) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public LanguageRefSetMember addLanguageRefSetMember(
    LanguageRefSetMember languageRefSetMember) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LanguageRefSetMember getLanguageRefSetMember(Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LanguageRefSetMember getLanguageRefSetMember(String terminologyId,
    String terminology, String version, String branch) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LanguageRefSetMemberList getLanguageRefSetMembers(
    String terminologyId, String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateLanguageRefSetMember(
    LanguageRefSetMember languageRefSetMember) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeLanguageRefSetMember(Long id) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public SimpleRefSetMember addSimpleRefSetMember(
    SimpleRefSetMember simpleRefSetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add simple map refset member "
            + simpleRefSetMember.getTerminologyId());
    // Assign id
    if (assignIdentifiersFlag) {
      simpleRefSetMember.setTerminologyId(idHandlerMap.get(simpleRefSetMember.getTerminology())
          .getTerminologyId(simpleRefSetMember));
    }
    // Add component
    SimpleRefSetMember newMember = addComponent(simpleRefSetMember);

    // Inform listeners
    // TODO: is this needed?
    /*if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.refSetMemberAdded(newMember);
      }
    }*/
    return newMember;
  }

  @Override
  public SimpleRefSetMember getSimpleRefSetMember(Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SimpleRefSetMember getSimpleRefSetMember(String terminologyId,
    String terminology, String version, String branch) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SimpleRefSetMemberList getSimpleRefSetMembers(String terminologyId,
    String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateSimpleRefSetMember(SimpleRefSetMember simpleRefSetMember)
    throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeSimpleRefSetMember(Long id) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<SearchResult> findMembersForHistoricalRefset(String refsetId,
    String terminology, String version, String query, PfsParameter pfs)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<SearchResult> findConceptsForHistoricalTranslation(
    String translationId, String terminology, String version, String query,
    PfsParameter pfs) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * Adds the component.
   *
   * @param <T> the
   * @param component the component
   * @return the component
   * @throws Exception the exception
   */
  private <T extends HasLastModified> T addComponent(T component) throws Exception {
    try {
      // Set last modified date
      if (lastModifiedFlag) {
        component.setLastModified(new Date());
      }

      // add
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(component);
        tx.commit();
      } else {
        manager.persist(component);
      }
      return component;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  @Override
  public void refreshCaches() throws Exception {
    // TODO Auto-generated method stub
    
  }  
}
