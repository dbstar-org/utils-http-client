package io.github.dbstarll.utils.http.client.response;

import org.apache.http.client.ResponseHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notNull;

public abstract class AbstractResponseHandlerFactory implements ResponseHandlerFactory {
  private final Map<Class<?>, ResponseHandler<?>> handlers;

  protected AbstractResponseHandlerFactory() {
    this.handlers = new HashMap<Class<?>, ResponseHandler<?>>();
  }

  protected final <T> void addResponseHandler(final Class<T> responseClass,
                                              final ResponseHandler<? extends T> responseHandler) {
    notNull(responseClass, "responseClass is null");
    notNull(responseHandler, "responseHandler is null");
    handlers.put(responseClass, responseHandler);
  }

  @SuppressWarnings("unchecked")
  @Override
  public final <T> ResponseHandler<T> getResponseHandler(final Class<T> responseClass) {
    return (ResponseHandler<T>) handlers.get(notNull(responseClass, "responseClass is null"));
  }

  @Override
  public final Iterator<Class<?>> iterator() {
    return handlers.keySet().iterator();
  }
}
