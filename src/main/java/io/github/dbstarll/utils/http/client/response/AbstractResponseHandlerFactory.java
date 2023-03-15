package io.github.dbstarll.utils.http.client.response;

import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notNull;

public abstract class AbstractResponseHandlerFactory implements ResponseHandlerFactory {
    private final Map<Class<?>, HttpClientResponseHandler<?>> handlers;

    protected AbstractResponseHandlerFactory() {
        this.handlers = new HashMap<>();
    }

    protected final <T> void addResponseHandler(final Class<T> responseClass,
                                                final HttpClientResponseHandler<? extends T> responseHandler) {
        notNull(responseClass, "responseClass is null");
        notNull(responseHandler, "responseHandler is null");
        handlers.put(responseClass, responseHandler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> HttpClientResponseHandler<T> getResponseHandler(final Class<T> responseClass) {
        return (HttpClientResponseHandler<T>) handlers.get(notNull(responseClass, "responseClass is null"));
    }

    @Override
    public final Iterator<Class<?>> iterator() {
        return handlers.keySet().iterator();
    }
}
