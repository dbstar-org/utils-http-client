package io.github.dbstarll.utils.http.client.response;

import org.apache.http.client.ResponseHandler;

public interface ResponseHandlerFactory extends Iterable<Class<?>> {
  <T> ResponseHandler<T> getResponseHandler(Class<T> responseClass);
}
