package io.github.dbstarll.utils.http.client.response;

import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicResponseHandlerFactoryTest {
    @Test
    void getResponseHandler() {
        final ResponseHandlerFactory factory = new BasicResponseHandlerFactory(true);
        final HttpClientResponseHandler<String> handler = factory.getResponseHandler(String.class);
        assertNotNull(handler);
        assertEquals(handler.getClass(), StringResponseHandler.class);
    }

    @Test
    void iterator() {
        final ResponseHandlerFactory factory = new BasicResponseHandlerFactory(false);
        final Iterator<Class<?>> ite = factory.iterator();
        assertNotNull(ite);
        assertTrue(ite.hasNext());
        final Class<?> handlerClass1 = ite.next();
        assertNotNull(handlerClass1);
        assertEquals(byte[].class, handlerClass1);
        assertTrue(ite.hasNext());
        final Class<?> handlerClass2 = ite.next();
        assertNotNull(handlerClass2);
        assertEquals(String.class, handlerClass2);
        assertFalse(ite.hasNext());
        assertThrows(NoSuchElementException.class, ite::next);
    }
}