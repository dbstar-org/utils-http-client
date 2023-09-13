package io.github.dbstarll.utils.http.client.response;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.support.ClassicResponseBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ByteArrayResponseHandlerTest {
    @Test
    void handleResponse() throws IOException {
        final ClassicHttpResponse response = ClassicResponseBuilder.create(200).setEntity("abc").build();
        assertArrayEquals("abc".getBytes(), new ByteArrayResponseHandler(false).handleResponse(response));
    }
}