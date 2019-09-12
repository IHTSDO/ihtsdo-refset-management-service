/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.TerminologyList;
import org.ihtsdo.otf.refset.helpers.TranslationExtensionLanguage;
import org.ihtsdo.otf.refset.helpers.TranslationExtensionLanguageList;
import org.ihtsdo.otf.refset.jpa.TranslationExtensionLanguageJpa;

/**
 * JAXB enabled implementation of {@link TerminologyList}.
 */
@XmlRootElement(name = "translationExtensionLanguages")
public class TranslationExtensionLanguageListJpa extends AbstractResultList<TranslationExtensionLanguage>
    implements TranslationExtensionLanguageList {

  /* see superclass */
  @Override
  @XmlElement(type = TranslationExtensionLanguageJpa.class, name = "translationExtensionLanguages")
  public List<TranslationExtensionLanguage> getObjects() {
    return super.getObjectsTransient();
  }
}
