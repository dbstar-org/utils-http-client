package io.github.dbstarll.utils.http.client.response;

import java.util.Arrays;

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
        Arrays.stream(noNullElements(factories)).forEach(f -> f.forEach(c -> addResponseHandler(c, f)));
    }

    private <T> void addResponseHandler(final Class<T> responseClass, final ResponseHandlerFactory factory) {
        addResponseHandler(responseClass, factory.getResponseHandler(responseClass));
    }
}
