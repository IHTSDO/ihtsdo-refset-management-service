/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;

import com.ibm.icu.util.Calendar;
import com.wordnik.swagger.jaxrs.config.BeanConfig;

/**
 * The application (for jersey). Also serves the role of the initialization
 * listener.
 */
@ApplicationPath("/")
public class RefsetServerApplication extends Application {

  /** The API_VERSION - also used in "swagger.htmL" */
  public final static String API_VERSION = "1.0.0";

  /** The timer. */
  Timer timer;

  /**
   * Instantiates an empty {@link RefsetServerApplication}.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  public RefsetServerApplication() throws Exception {
    Logger.getLogger(getClass()).info(
        "IHTSDO refset management service APPLICATION START");
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setTitle("IHTSDO refset management service API");
    beanConfig
        .setDescription("RESTful calls for IHTSDO refset management service");
    beanConfig.setVersion(API_VERSION);
    beanConfig.setBasePath(ConfigUtility.getConfigProperties().getProperty(
        "base.url"));
    beanConfig.setResourcePackage("org.ihtsdo.otf.refset.rest.impl");
    beanConfig.setScan(true);

    // Set up a timer task to run at 2AM every day
    TimerTask task = new InitializationTask();
    timer = new Timer();
    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 2);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    //timer.scheduleAtFixedRate(task, today.getTime(), 6 * 60 * 60 * 1000);

  }

  /**
   * Initialization task.
   */
  class InitializationTask extends TimerTask {

    /* see superclass */
    @Override
    public void run() {
      // TODO: ??
    }
  }

  /* see superclass */
  @Override
  public Set<Class<?>> getClasses() {
    final Set<Class<?>> classes = new HashSet<Class<?>>();
    // TODO: need to list services here
    classes.add(SecurityServiceRestImpl.class);
    classes.add(ProjectServiceRestImpl.class);
    classes.add(RefsetServiceRestImpl.class);
    classes.add(TranslationServiceRestImpl.class);
    classes.add(ReleaseServiceRestImpl.class);
    classes.add(ValidationServiceRestImpl.class);
    classes.add(WorkflowServiceRestImpl.class);
    classes
        .add(com.wordnik.swagger.jersey.listing.ApiListingResourceJSON.class);
    classes
        .add(com.wordnik.swagger.jersey.listing.JerseyApiDeclarationProvider.class);
    classes
        .add(com.wordnik.swagger.jersey.listing.JerseyResourceListingProvider.class);
    return classes;
  }

  /* see superclass */
  @Override
  public Set<Object> getSingletons() {
    final Set<Object> instances = new HashSet<Object>();
    instances.add(new JacksonFeature());
    instances.add(new JsonProcessingFeature());
    instances.add(new MultiPartFeature());
    // Enable for LOTS of logging of HTTP requests
    // instances.add(new LoggingFilter());
    return instances;
  }

}
