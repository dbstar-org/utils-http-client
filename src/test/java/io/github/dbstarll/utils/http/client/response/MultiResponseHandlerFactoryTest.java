package io.github.dbstarll.utils.http.client.response;

import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiResponseHandlerFactoryTest {
    @Test
    void getResponseHandler() {
        final ResponseHandlerFactory factory = new MultiResponseHandlerFactory(new BasicResponseHandlerFactory());
        final HttpClientResponseHandler<String> handler = factory.getResponseHandler(String.class);
        assertNotNull(handler);
        assertEquals(handler.getClass(), BasicHttpClientResponseHandler.class);
    }


    @Test
    void iterator() {
        final ResponseHandlerFactory factory = new MultiResponseHandlerFactory(new BasicResponseHandlerFactory());
        final Iterator<Class<?>> ite = factory.iterator();
        assertNotNull(ite);
        assertTrue(ite.hasNext());
        final Class<?> handlerClass = ite.next();
        assertNotNull(handlerClass);
        assertEquals(handlerClass, String.class);
        assertFalse(ite.hasNext());
        assertThrows(NoSuchElementException.class, ite::next);
    }
}