package io.github.dbstarll.utils.http.client.response;

import org.apache.http.impl.client.BasicResponseHandler;

public class BasicResponseHandlerFactory extends AbstractResponseHandlerFactory {
  public BasicResponseHandlerFactory() {
    addResponseHandler(String.class, new BasicResponseHandler());
  }
}
