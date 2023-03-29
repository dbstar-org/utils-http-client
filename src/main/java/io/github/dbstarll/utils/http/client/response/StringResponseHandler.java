package io.github.dbstarll.utils.http.client.response;

import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.core5.http.HttpEntity;

import java.io.IOException;

public final class StringResponseHandler extends AbstractResponseHandler<String> {
    private static final BasicHttpClientResponseHandler DEFAULT_HANDLER = new BasicHttpClientResponseHandler();

    /**
     * @param alwaysProcessEntity 在返回错误的状态码时，是否还要继续解析entity
     */
    public StringResponseHandler(final boolean alwaysProcessEntity) {
        super(alwaysProcessEntity);
    }

    @Override
    public String handleEntity(final HttpEntity entity) throws IOException {
        return DEFAULT_HANDLER.handleEntity(entity);
    }
}
