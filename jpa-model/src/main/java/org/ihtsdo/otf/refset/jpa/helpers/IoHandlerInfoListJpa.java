/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.jpa.IoHandlerInfoJpa;

/**
 * JAXB enabled implementation of {@link IoHandlerInfoList}.
 */
@XmlRootElement(name = "handlerList")
public class IoHandlerInfoListJpa extends AbstractResultList<IoHandlerInfo>
    implements IoHandlerInfoList {

  /* see superclass */
  @Override
  @XmlElement(type = IoHandlerInfoJpa.class, name = "handlers")
  public List<IoHandlerInfo> getObjects() {
    return super.getObjectsTransient();
  }

}
