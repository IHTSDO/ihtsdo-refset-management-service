/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;

/**
 * JAXB-enabled implementation of {@link ReleaseInfoList}.
 */
@XmlRootElement(name = "releaseInfoList")
public class ReleaseInfoListJpa extends AbstractResultList<ReleaseInfo>
    implements ReleaseInfoList {

  /* see superclass */
  @Override
  @XmlElement(type = ReleaseInfoJpa.class, name = "releaseInfos")
  public List<ReleaseInfo> getObjects() {
    return super.getObjectsTransient();
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ReleaseInfoListJpa [releaseInfos=" + getObjects() + ", getCount()="
        + getCount() + "]";
  }

}
