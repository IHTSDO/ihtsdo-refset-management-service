/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Properties;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.SimpleRefSetMemberList;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;

/**
 * Default implementation of {@link TerminologyHandler}. Leverages the IHTSDO
 * terminology server to the extent possible for interacting with terminology
 * components. Uses local storage where not possible.
 */
public class DefaultTerminologyHandler extends RootServiceJpa implements
    TerminologyHandler {

  /**
   * Instantiates an empty {@link DefaultTerminologyHandler}.
   *
   * @throws Exception the exception
   */
  public DefaultTerminologyHandler() throws Exception {
    super();
  }

  /** The url. */
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

  /* see superclass */
  @Override
  public String getName() {
    return "Default terminology handler";
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception {
    // TODO
    return null;
  }

  /* see superclass */
  @Override
  public SimpleRefSetMemberList findMembersForRefset(Long refsetId,
    String query, PfsParameter pfs) throws Exception {
    // TODO
    return null;
  }

  /* see superclass */
  @Override
  public Concept getConcept(Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public Description addDescription(Description description) throws Exception {
    // TODO: Implement as call to terminology server
    return null;
  }

  /* see superclass */
  @Override
  public Description getDescription(Long id) throws Exception {
    // TODO: Implement as call to terminology server
    return null;
  }

  /* see superclass */
  @Override
  public Description getDescription(String terminologyId, String terminology,
    String version) throws Exception {
    // TODO: Implement as call to terminology server
    return null;
  }

  /* see superclass */
  @Override
  public void updateDescription(Description description) throws Exception {
    // TODO: Implement as call to terminology server
  }

  /* see superclass */
  @Override
  public void removeDescription(Long id) throws Exception {
    // TODO: Implement as call to terminology server
  }

  /* see superclass */
  @Override
  public LanguageRefSetMember addLanguageRefSetMember(
    LanguageRefSetMember languageRefSetMember) throws Exception {
    // TODO: Implement as call to terminology server
    return null;
  }

  /* see superclass */
  @Override
  public LanguageRefSetMember getLanguageRefSetMember(Long id) throws Exception {
    // TODO: Implement as call to terminology server
    return null;
  }

  /* see superclass */
  @Override
  public LanguageRefSetMember getLanguageRefSetMember(String terminologyId,
    String terminology, String version) throws Exception {
    // TODO: Implement as call to terminology server
    return null;
  }

  /* see superclass */
  @Override
  public void updateLanguageRefSetMember(
    LanguageRefSetMember languageRefSetMember) throws Exception {
    // TODO: Implement as call to terminology server

  }

  /* see superclass */
  @Override
  public void removeLanguageRefSetMember(Long id) throws Exception {
    // TODO: Implement as call to terminology server
  }

  /* see superclass */
  @Override
  public SimpleRefSetMember addSimpleRefSetMember(
    SimpleRefSetMember simpleRefSetMember) throws Exception {
    // TODO: Implement as call to terminology server
    return null;
  }

  /* see superclass */
  @Override
  public SimpleRefSetMember getSimpleRefSetMember(Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public SimpleRefSetMember getSimpleRefSetMember(String terminologyId,
    String terminology, String version) throws Exception {
    return null;
    // TODO: Implement as call to terminology server
  }

  /* see superclass */
  @Override
  public void updateSimpleRefSetMember(SimpleRefSetMember simpleRefSetMember)
    throws Exception {
    // TODO: Implement as call to terminology server
  }

  /* see superclass */
  @Override
  public void removeSimpleRefSetMember(Long id) throws Exception {
    // TODO: Implement as call to terminology server
  }

  /* see superclass */
  @Override
  public SimpleRefSetMemberList findMembersForHistoricalRefset(Refset refset,
    String query, PfsParameter pfs) throws Exception {
    // TODO: implement as a local call
    return null;
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsForHistoricalTranslation(
    Translation translation, String query, PfsParameter pfs) throws Exception {
    // TODO: implement as a local call
    return null;
  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    // n/a
  }
}
