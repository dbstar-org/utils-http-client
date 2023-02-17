package io.github.dbstarll.utils.http.client.request;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbsoluteUriResolverTest {
    @Test
    void resolve() {
        final String uri = "https://www.baidu.com/";
        final UriResolver resolver = new AbsoluteUriResolver();
        assertEquals(URI.create(uri), resolver.resolve(uri));
    }
}