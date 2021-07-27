package io.github.dbstarll.utils.http.client.request;

import java.net.URI;

public interface UriResolver {
  /**
   * Constructs a new URI by parsing the given string and then resolving it.
   *
   * @param uri The string to be parsed into a URI
   * @return The resulting URI
   * @throws NullPointerException     If <tt>str</tt> is <tt>null</tt>
   * @throws IllegalArgumentException If the given string violates RFC&nbsp;2396
   */
  URI resolve(String uri);
}
