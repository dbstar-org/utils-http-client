package io.github.dbstarll.utils.http.client.response;

import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class BasicResponseHandlerFactoryTest {
    @Test
    void getResponseHandler() {
        final ResponseHandlerFactory factory = new BasicResponseHandlerFactory();
        final ResponseHandler<String> handler = factory.getResponseHandler(String.class);
        assertNotNull(handler);
        assertEquals(handler.getClass(), BasicResponseHandler.class);
    }

    @Test
    void iterator() {
        final ResponseHandlerFactory factory = new BasicResponseHandlerFactory();
        final Iterator<Class<?>> ite = factory.iterator();
        assertNotNull(ite);
        assertTrue(ite.hasNext());
        final Class<?> handlerClass = ite.next();
        assertNotNull(handlerClass);
        assertEquals(handlerClass, String.class);
        assertFalse(ite.hasNext());
        assertThrows(NoSuchElementException.class, () -> ite.next());
    }
}