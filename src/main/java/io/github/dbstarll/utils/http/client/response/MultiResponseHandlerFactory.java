package io.github.dbstarll.utils.http.client.response;

import static org.apache.commons.lang3.Validate.noNullElements;

public class MultiResponseHandlerFactory extends AbstractResponseHandlerFactory {
  public MultiResponseHandlerFactory(ResponseHandlerFactory... factories) {
    addResponseHandlerFactory(factories);
  }

  protected final void addResponseHandlerFactory(ResponseHandlerFactory... factories) {
    for (ResponseHandlerFactory factory : noNullElements(factories)) {
      for (Class<?> responseClass : factory) {
        addResponseHandler(responseClass, factory);
      }
    }
  }

  private <T> void addResponseHandler(Class<T> responseClass, ResponseHandlerFactory factory) {
    addResponseHandler(responseClass, factory.getResponseHandler(responseClass));
  }
}
