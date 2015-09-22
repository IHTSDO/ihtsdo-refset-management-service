/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.worfklow;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;

/**
 * JAXB enabled implementation of {@link TrackingRecordList}.
 */
@XmlRootElement(name = "projectList")
public class TrackingRecordListJpa extends AbstractResultList<TrackingRecord>
    implements TrackingRecordList {

  /* see superclass */
  @Override
  @XmlElement(type = TrackingRecordJpa.class, name = "record")
  public List<TrackingRecord> getObjects() {
    return super.getObjectsTransient();
  }

}
