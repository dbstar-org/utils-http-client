package io.github.dbstarll.utils.http.client.response;

import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;

public class BasicResponseHandlerFactory extends AbstractResponseHandlerFactory {
    /**
     * 构造BasicResponseHandlerFactory.
     */
    public BasicResponseHandlerFactory() {
        addResponseHandler(String.class, new BasicHttpClientResponseHandler());
    }
}
