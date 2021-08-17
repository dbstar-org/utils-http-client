package io.github.dbstarll.utils.http.client.response;

import static org.apache.commons.lang3.Validate.noNullElements;

public class MultiResponseHandlerFactory extends AbstractResponseHandlerFactory {
    /**
     * 构造MultiResponseHandlerFactory.
     *
     * @param factories ResponseHandlerFactory数组
     */
    public MultiResponseHandlerFactory(final ResponseHandlerFactory... factories) {
        addResponseHandlerFactory(factories);
    }

    protected final void addResponseHandlerFactory(final ResponseHandlerFactory... factories) {
        for (ResponseHandlerFactory factory : noNullElements(factories)) {
            for (Class<?> responseClass : factory) {
                addResponseHandler(responseClass, factory);
            }
        }
    }

    private <T> void addResponseHandler(final Class<T> responseClass, final ResponseHandlerFactory factory) {
        addResponseHandler(responseClass, factory.getResponseHandler(responseClass));
    }
}
