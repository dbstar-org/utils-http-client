package io.github.dbstarll.utils.http.client.request;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

import static org.apache.commons.lang3.Validate.notBlank;

public class RelativeUriResolver implements UriResolver {
    private final URI uriBase;

    /**
     * 构造RelativeUriResolver.
     *
     * @param uriBase base uri
     */
    public RelativeUriResolver(final String uriBase) {
        this.uriBase = URI.create(normalized(notBlank(uriBase)));
    }

    private static String normalized(final String uriBase) {
        final String base = uriBase.trim();
        if (base.indexOf("://") < 1) {
            throw new IllegalArgumentException("uriBase need scheme");
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    @Override
    public URI resolve(final String uri) {
        if (StringUtils.isBlank(uri)) {
            return uriBase;
        } else {
            return uriBase.resolve(uri);
        }
    }
}
