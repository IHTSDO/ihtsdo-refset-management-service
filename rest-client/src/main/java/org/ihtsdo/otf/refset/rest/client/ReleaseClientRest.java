/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.io.InputStream;
import java.util.Properties;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ReleaseServiceRest;

/**
 * A client for connecting to a release REST service.
 */
public class ReleaseClientRest extends RootClientRest implements
    ReleaseServiceRest {

  /** The config. */
  @SuppressWarnings("unused")
  private Properties config = null;

  /**
   * Instantiates a {@link ReleaseClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ReleaseClientRest(Properties config) {
    this.config = config;
  }


  @Override
  public ValidationResult validateRefsetRelease(Long refsetId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult previewRefsetRelease(Long refsetId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult finishRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult performTranslationRelease(Long translationId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult previewTranslationRelease(Long translationId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult finishTranslationRelease(Long translationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult cancelRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult cancelTranslationRelease(Long translationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReleaseInfoList findRefsetReleasesForQuery(Long refsetId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReleaseInfo getCurrentRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReleaseInfoList findTranslationReleasesForQuery(Long translationId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReleaseInfo getCurrentTranslationRelease(Long translationtId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeReleaseArtifact(Long artifactId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void uploadReleaseArtifact(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long releaseInfoId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public InputStream exportReleaseArtifact(Long artifactId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public ReleaseInfo beginRefsetRelease(Long refsetId, String effectiveTime,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public ReleaseInfo beginTranslationRelease(Long translationId,
    String effectiveTime, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


}
