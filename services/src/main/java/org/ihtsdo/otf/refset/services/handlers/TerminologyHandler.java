/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.util.List;

import org.ihtsdo.otf.refset.Terminology;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.rf2.Concept;

/**
 * Generically represents a handler for accessing terminology objects.
 */
public interface TerminologyHandler extends Configurable {

  /** The Constant UNABLE_TO_DETERMINE_NAME. */
  public final static String UNABLE_TO_DETERMINE_NAME =
      "unable to determine name";

  /** The Constant NAME_LOOKUP_IN_PROGRESS. */
  public final static String NAME_LOOKUP_IN_PROGRESS =
      "name lookup in progress";

  /**
   * Copy the handler. This is needed used because of how the terminology
   * handler is instantiated. One template object is created, and then copies of
   * it are returned for individual requests.
   *
   * @return the terminology handler
   * @throws Exception the exception
   */
  public TerminologyHandler copy() throws Exception;

  /**
   * Test.
   *
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean test() throws Exception;

  /**
   * Returns the terminology editions.
   *
   * @return the terminology editions
   * @throws Exception the exception
   */
  public List<Terminology> getTerminologyEditions() throws Exception;

  /**
   * Returns the terminology versions.
   *
   * @param edition the edition
   * @return the terminology versions
   * @throws Exception the exception
   */
  public List<Terminology> getTerminologyVersions(String edition)
    throws Exception;

  /**
   * Resolve expression.
   *
   * @param expr the expr
   * @param terminolgy the terminolgy
   * @param version the version
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList resolveExpression(String expr, String terminolgy,
    String version, PfsParameter pfs) throws Exception;

  /**
   * Returns the concept with descriptions and relationships. Inactive
   * descriptions are not included. Inactive, stated, and "Is a" relationships
   * are not included.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concept with descriptions
   * @throws Exception the exception
   */
  public Concept getFullConcept(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Returns the concept with terminologyId and name.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Returns the concepts.
   *
   * @param terminologyIds the terminology ids
   * @param terminology the terminology
   * @param version the version
   * @return the concepts
   * @throws Exception the exception
   */
  public ConceptList getConcepts(List<String> terminologyIds,
    String terminology, String version) throws Exception;

  /**
   * Find concepts for query.
   *
   * @param query the query
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findConceptsForQuery(String query, String terminology,
    String version, PfsParameter pfs) throws Exception;

  /**
   * Find refsets for query.
   *
   * @param query the query
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findRefsetsForQuery(String query, String terminology,
    String version, PfsParameter pfs) throws Exception;

  /**
   * Get modules.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the concept list
   * @throws Exception the exception
   */
  public List<Concept> getModules(String terminology, String version)
    throws Exception;

  /**
   * Returns the concept parents.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concept parents
   * @throws Exception the exception
   */
  public ConceptList getConceptParents(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Returns the concept children.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concept children
   * @throws Exception the exception
   */
  public ConceptList getConceptChildren(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Returns the potential current concepts for retired concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @return the potential current concepts for retired concept
   * @throws Exception the exception
   */
  public ConceptList getReplacementConcepts(String conceptId,
    String terminology, String version) throws Exception;

  /**
   * Count expression.
   *
   * @param expr the expr
   * @param terminology the terminology
   * @param version the version
   * @return the int
   * @throws Exception the exception
   */
  public int countExpression(String expr, String terminology, String version)
    throws Exception;

  /**
   * Indicates whether or not the value is a concept id.
   *
   * @param value the value
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  public boolean isConceptId(String value) throws Exception;

  /**
   * Sets the url.
   *
   * @param url the url
   * @throws Exception the exception
   */
  public void setUrl(String url) throws Exception;

  /**
   * Returns the default url.
   *
   * @return the default url
   * @throws Exception the exception
   */
  public String getDefaultUrl() throws Exception;
}
