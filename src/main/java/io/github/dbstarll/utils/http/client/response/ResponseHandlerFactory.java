package io.github.dbstarll.utils.http.client.response;

import org.apache.hc.core5.http.io.HttpClientResponseHandler;

public interface ResponseHandlerFactory extends Iterable<Class<?>> {
    /**
     * 获得相应response类型的ResponseHandler.
     *
     * @param responseClass response类
     * @param <T>           response类型
     * @return ResponseHandler
     */
    <T> HttpClientResponseHandler<T> getResponseHandler(Class<T> responseClass);
}
