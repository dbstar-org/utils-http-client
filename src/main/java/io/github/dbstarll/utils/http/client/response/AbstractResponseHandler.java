package io.github.dbstarll.utils.http.client.response;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public abstract class AbstractResponseHandler<T> extends AbstractHttpClientResponseHandler<T> {
    private final boolean alwaysProcessEntity;

    protected AbstractResponseHandler(final boolean alwaysProcessEntity) {
        this.alwaysProcessEntity = alwaysProcessEntity;
    }

    @Override
    public final T handleResponse(final ClassicHttpResponse response) throws IOException {
        final HttpEntity entity = response.getEntity();
        if (response.getCode() >= HttpStatus.SC_REDIRECTION && (entity == null || !alwaysProcessEntity)) {
            EntityUtils.consume(entity);
            throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
        }
        return entity == null ? null : handleEntity(entity);
    }
}
