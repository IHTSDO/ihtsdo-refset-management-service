/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.rest.impl;

import javax.websocket.server.ServerEndpointConfig;

/**
 * Configurator to obtain reference to the notification websocket for use by the
 * application.
 */
public class NotificationWebsocketConfigurator extends
    ServerEndpointConfig.Configurator {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getEndpointInstance(Class<T> endpointClass)
    throws InstantiationException {
    if (endpointClass.equals(NotificationWebsocket.class)) {
      NotificationWebsocket endpoint = new NotificationWebsocket();

      // Ensure root implementation of all services has access to this
      // Not ideal because there's no API to support this, there's
      // just a magic invocation. However it does make this available
      // to all services that implement RootServiceRestImpl
      RootServiceRestImpl.setNotificationWebsocket(endpoint);
      return (T) endpoint;
    }
    throw new InstantiationException("Unexpected websocket endpoint");
  }

}