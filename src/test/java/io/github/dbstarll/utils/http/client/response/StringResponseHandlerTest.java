package io.github.dbstarll.utils.http.client.response;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.support.ClassicResponseBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class StringResponseHandlerTest {
    @Test
    void handleResponse() throws IOException {
        final ClassicHttpResponse response = ClassicResponseBuilder.create(200).setEntity("abc").build();
        assertEquals("abc", new StringResponseHandler(false).handleResponse(response));
    }

    @Test
    void handleNull() throws IOException {
        final ClassicHttpResponse response = ClassicResponseBuilder.create(200).build();
        assertNull(new StringResponseHandler(false).handleResponse(response));
    }

    @Test
    void handle404() {
        final ClassicHttpResponse response = ClassicResponseBuilder.create(404).setEntity("abc").build();
        final HttpResponseException e = assertThrowsExactly(HttpResponseException.class,
                () -> new StringResponseHandler(false).handleResponse(response));
        assertEquals(404, e.getStatusCode());
        assertEquals("Not Found", e.getReasonPhrase());
    }

    @Test
    void alwaysProcessEntity() throws IOException {
        final ClassicHttpResponse response = ClassicResponseBuilder.create(404).setEntity("abc").build();
        assertEquals("abc", new StringResponseHandler(true).handleResponse(response));
    }

    @Test
    void alwaysProcessEntityNull() {
        final ClassicHttpResponse response = ClassicResponseBuilder.create(404).build();
        final HttpResponseException e = assertThrowsExactly(HttpResponseException.class,
                () -> new StringResponseHandler(true).handleResponse(response));
        assertEquals(404, e.getStatusCode());
        assertEquals("Not Found", e.getReasonPhrase());
    }
}