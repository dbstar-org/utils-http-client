package io.github.dbstarll.utils.http.client.response;

import org.apache.http.client.ResponseHandler;

public interface ResponseHandlerFactory extends Iterable<Class<?>> {
    /**
     * 获得相应response类型的ResponseHandler.
     *
     * @param responseClass response类
     * @param <T>           response类型
     * @return ResponseHandler
     */
    <T> ResponseHandler<T> getResponseHandler(Class<T> responseClass);
}
