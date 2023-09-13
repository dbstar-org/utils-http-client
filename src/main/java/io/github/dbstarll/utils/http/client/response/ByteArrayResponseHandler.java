package io.github.dbstarll.utils.http.client.response;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public final class ByteArrayResponseHandler extends AbstractResponseHandler<byte[]> {
    /**
     * @param alwaysProcessEntity 在返回错误的状态码时，是否还要继续解析entity
     */
    public ByteArrayResponseHandler(final boolean alwaysProcessEntity) {
        super(alwaysProcessEntity);
    }

    @Override
    public byte[] handleEntity(final HttpEntity entity) throws IOException {
        return EntityUtils.toByteArray(entity);
    }
}
