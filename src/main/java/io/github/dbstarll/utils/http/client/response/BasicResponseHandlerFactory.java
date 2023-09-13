package io.github.dbstarll.utils.http.client.response;

public class BasicResponseHandlerFactory extends AbstractResponseHandlerFactory {
    /**
     * 构造BasicResponseHandlerFactory.
     *
     * @param alwaysProcessEntity 在返回错误的状态码时，是否还要继续解析entity
     */
    public BasicResponseHandlerFactory(final boolean alwaysProcessEntity) {
        addResponseHandler(String.class, new StringResponseHandler(alwaysProcessEntity));
        addResponseHandler(byte[].class, new ByteArrayResponseHandler(alwaysProcessEntity));
    }
}
