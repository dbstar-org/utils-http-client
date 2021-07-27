package io.github.dbstarll.utils.http.client.request;

import java.net.URI;

public class AbsoluteUriResolver implements UriResolver {
  @Override
  public URI resolve(String uri) {
    return URI.create(uri);
  }
}
