package io.github.dbstarll.utils.http.client.response;

import org.apache.http.impl.client.BasicResponseHandler;

public class BasicResponseHandlerFactory extends AbstractResponseHandlerFactory {
    /**
     * 构造BasicResponseHandlerFactory.
     */
    public BasicResponseHandlerFactory() {
        super();
        addResponseHandler(String.class, new BasicResponseHandler());
    }
}
