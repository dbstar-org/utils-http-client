package io.github.dbstarll.utils.http.client.request;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class RelativeUriResolverTest {
    @Test
    void resolve() {
        final UriResolver resolver = new RelativeUriResolver("https://static.y1cloud.com");
        new HashMap<String, String>() {{
            put(null, "https://static.y1cloud.com");
            put("", "https://static.y1cloud.com/");
            put("/", "https://static.y1cloud.com/");
            put("ping.html", "https://static.y1cloud.com/ping.html");
            put("/ping.html", "https://static.y1cloud.com/ping.html");
            put("https://baidu.com/", "https://baidu.com/");
        }}.forEach((k, v) -> {
            final String message = String.format("failed with [%s]", k);
            assertEquals(v, resolver.resolve(k).toString(), message);
        });
    }

    @Test
    void resolvePath() {
        final UriResolver resolver = new RelativeUriResolver("https://static.y1cloud.com/first/second");
        new HashMap<String, String>() {{
            put(null, "https://static.y1cloud.com/first/second");
            put("", "https://static.y1cloud.com/first/second/");
            put("/", "https://static.y1cloud.com/");
            put("ping.html", "https://static.y1cloud.com/first/second/ping.html");
            put("/ping.html", "https://static.y1cloud.com/ping.html");
            put("https://baidu.com/", "https://baidu.com/");
        }}.forEach((k, v) -> {
            final String message = String.format("failed with [%s]", k);
            assertEquals(v, resolver.resolve(k).toString(), message);
        });
    }

    @Test
    void resolvePathEndWithSlash() {
        final UriResolver resolver = new RelativeUriResolver("https://static.y1cloud.com/first/second/");
        new HashMap<String, String>() {{
            put(null, "https://static.y1cloud.com/first/second");
            put("", "https://static.y1cloud.com/first/second/");
            put("/", "https://static.y1cloud.com/");
            put("ping.html", "https://static.y1cloud.com/first/second/ping.html");
            put("/ping.html", "https://static.y1cloud.com/ping.html");
            put("https://baidu.com/", "https://baidu.com/");
        }}.forEach((k, v) -> {
            final String message = String.format("failed with [%s]", k);
            assertEquals(v, resolver.resolve(k).toString(), message);
        });
    }

    @Test
    void resolvePathWithContext() {
        final UriResolver resolver = new RelativeUriResolver("https://static.y1cloud.com/first/second/", "/first");
        new HashMap<String, String>() {{
            put(null, "https://static.y1cloud.com/first/second");
            put("", "https://static.y1cloud.com/first/second/");
            put("/", "https://static.y1cloud.com/first/");
            put("ping.html", "https://static.y1cloud.com/first/second/ping.html");
            put("/ping.html", "https://static.y1cloud.com/first/ping.html");
            put("https://baidu.com/", "https://baidu.com/");
        }}.forEach((k, v) -> {
            final String message = String.format("failed with [%s]", k);
            assertEquals(v, resolver.resolve(k).toString(), message);
        });
    }

    @Test
    void resolvePathWithContextSlash() {
        final UriResolver resolver = new RelativeUriResolver("https://static.y1cloud.com/first/second/", "first/");
        new HashMap<String, String>() {{
            put(null, "https://static.y1cloud.com/first/second");
            put("", "https://static.y1cloud.com/first/second/");
            put("/", "https://static.y1cloud.com/first/");
            put("ping.html", "https://static.y1cloud.com/first/second/ping.html");
            put("/ping.html", "https://static.y1cloud.com/first/ping.html");
            put("https://baidu.com/", "https://baidu.com/");
        }}.forEach((k, v) -> {
            final String message = String.format("failed with [%s]", k);
            assertEquals(v, resolver.resolve(k).toString(), message);
        });
    }

    @Test
    void resolveContextInvalid() {
        final Exception e = assertThrowsExactly(IllegalArgumentException.class, () ->
                new RelativeUriResolver("https://static.y1cloud.com/first/second/", "/firs"));
        assertEquals("path of [https://static.y1cloud.com/first/second] not start with [/firs/]", e.getMessage());

        final Exception e1 = assertThrowsExactly(IllegalArgumentException.class, () ->
                new RelativeUriResolver("https://static.y1cloud.com/first/second/", "/sec"));
        assertEquals("path of [https://static.y1cloud.com/first/second] not start with [/sec/]", e1.getMessage());
    }

    @Test
    void resolveBaseInvalid() {
        final Exception e = assertThrowsExactly(IllegalArgumentException.class, () ->
                new RelativeUriResolver("/static.y1cloud.com/first/second/"));
        assertEquals("uriBase[/static.y1cloud.com/first/second/] need scheme before [://]", e.getMessage());
    }
}