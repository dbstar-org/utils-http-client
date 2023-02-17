package io.github.dbstarll.utils.http.client.request;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

import static org.apache.commons.lang3.Validate.notBlank;

public class RelativeUriResolver implements UriResolver {
    public static final String DEFAULT_CONTEXT = "/";

    private final URI uriBase;
    private final String context;

    /**
     * 构造RelativeUriResolver.
     *
     * @param uriBase base uri
     */
    public RelativeUriResolver(final String uriBase) {
        this(uriBase, DEFAULT_CONTEXT);
    }

    /**
     * 构造RelativeUriResolver.
     *
     * @param uriBase base uri
     * @param context context
     * @throws IllegalArgumentException uriBase or context invalid
     */
    public RelativeUriResolver(final String uriBase, final String context) throws IllegalArgumentException {
        this.uriBase = URI.create(normalizedBase(notBlank(uriBase)));
        this.context = normalizedContext(notBlank(context));
        verifyContext();
    }

    private void verifyContext() throws IllegalArgumentException {
        final String path = uriBase.getRawPath();
        if (StringUtils.startsWith(path, context)) {
            if (path.length() == context.length() || path.charAt(context.length()) == '/') {
                // match and return
                return;
            }
        }
        throw new IllegalArgumentException(String.format("path of [%s] not start with [%s/]", uriBase, context));
    }

    private static String normalizedBase(final String uriBase) throws IllegalArgumentException {
        final String base = uriBase.trim();
        if (base.indexOf("://") < 1) {
            throw new IllegalArgumentException(String.format("uriBase[%s] need scheme before [://]", base));
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private static String normalizedContext(final String context) {
        String ctx = context.trim();
        if (!ctx.startsWith("/")) {
            ctx = '/' + ctx;
        }
        if (ctx.endsWith("/")) {
            ctx = ctx.substring(0, ctx.length() - 1);
        }
        return ctx;
    }

    @Override
    public URI resolve(final String str) {
        if (null == str) {
            return uriBase;
        }
        final String path = str.trim();
        final URI uri = URI.create(path);
        if (uri.isAbsolute()) {
            return uriBase.resolve(uri);
        } else if (!path.startsWith("/")) {
            return uriBase.resolve(uriBase.getRawPath() + '/' + path);
        } else {
            return uriBase.resolve(context + path);
        }
    }
}
