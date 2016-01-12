/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.util.List;
import java.util.Map;

import org.ihtsdo.otf.refset.Terminology;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;

/**
 * Generically represents a handler for accessing terminology objects.
 */
public interface TerminologyHandler extends Configurable {

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
   * Returns the terminology editions.
   *
   * @return the terminology editions
   * @throws Exception the exception
   */
  public List<String> getTerminologyEditions() throws Exception;

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
   * Assign names.
   *
   * @return true, if successful
   */
  public boolean assignNames();

  /**
   * Returns the standard description types.
   *
   * @param terminology the terminology
   * @return the standard description types
   * @throws Exception the exception
   */
  public List<DescriptionType> getStandardDescriptionTypes(String terminology)
    throws Exception;

  /**
   * Returns the standard language description types.
   *
   * @param terminology the terminology
   * @return the standard language description types
   * @throws Exception the exception
   */
  public List<LanguageDescriptionType> getStandardLanguageDescriptionTypes(
    String terminology) throws Exception;

  /**
   * Returns the standard case sensitivity types.
   *
   * @param terminology the terminology
   * @return the standard case sensitivity types
   * @throws Exception the exception
   */
  public Map<String, String> getStandardCaseSensitivityTypes(String terminology)
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
  public ConceptList getConceptParents(String terminologyId,
    String terminology, String version) throws Exception;

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
   * <pre>
   * Refset methods
   *  - add/update/remove/get/find refset
   *  - add/remove/get/find refset member
   *  - get/find refset members
   * Translation (some of these likely already exist)
   *  - add/update/remove concept (get/find already defined)
   *  - add/update/remove description (get alread implemented)
   *  - add/update/remove language refset member
   * </pre>
   * 
   * .
   *
   * @return true, if successful
   */

}
