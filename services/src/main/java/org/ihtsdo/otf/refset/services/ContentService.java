/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;


/**
 * Generically represents a service for interacting with terminology content.
 */
public interface ContentService extends RootService {

  // Uses a "content service handler" internally

  
  // get/find concept (with descriptions)
  // get/find description (with language refset entry)
  
  // get/add/remove/update/find Refset (really this is refset metadata)
  //   store "refset" objects locally
  //   link to refset descriptor refset

  // get/add/remove/update/find Translation
  //   store "translation" objects locally
  //   link to description type refset
  
  // get/add/remove/update DescriptionTypeRefset entries
  // get/add/remove/update RefsetDescriptorRefset entries

  // compute an expression, return concepts.
}