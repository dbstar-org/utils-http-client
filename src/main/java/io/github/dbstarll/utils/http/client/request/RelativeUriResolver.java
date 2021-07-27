package io.github.dbstarll.utils.http.client.request;

import static org.apache.commons.lang3.Validate.notBlank;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

public class RelativeUriResolver implements UriResolver {
  private final URI uriBase;

  public RelativeUriResolver(String uriBase) {
    this.uriBase = URI.create(normalized(notBlank(uriBase)));
  }

  private static String normalized(String uriBase) {
    uriBase = uriBase.trim();
    if (uriBase.indexOf("://") < 1) {
      throw new IllegalArgumentException("uriBase need scheme");
    }
    return uriBase.endsWith("/") ? uriBase.substring(0, uriBase.length() - 1) : uriBase;
  }

  @Override
  public URI resolve(String uri) {
    if (StringUtils.isBlank(uri)) {
      return uriBase;
    } else {
      return uriBase.resolve(uri);
    }
  }
}
