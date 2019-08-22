/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.rf2.Component;

public abstract class ImportExportAbstract {
  
  /**
   * Sets the common fields.
   *
   * @param component the component
   * @param translation the translation
   */
  @SuppressWarnings("static-method")
  protected void setCommonFields(Component component, Translation translation) {
    component.setActive(true);
    component.setEffectiveTime(null);
    component.setId(null);
    component.setPublishable(true);
    component.setPublished(false);
    component.setModuleId(translation.getModuleId());
  }
  
  /**
   * Sets the common fields.
   *
   * @param component the component
   * @param refset the refset
   */
  @SuppressWarnings("static-method")
  protected void setCommonFields(Component component, Refset refset) {
    component.setActive(true);
    component.setEffectiveTime(null);
    component.setId(null);
    component.setPublishable(true);
    component.setPublished(false);
    component.setModuleId(refset.getModuleId());
  }  

}
