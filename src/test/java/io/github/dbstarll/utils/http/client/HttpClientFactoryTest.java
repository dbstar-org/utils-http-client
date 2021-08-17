package io.github.dbstarll.utils.http.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 测试HttpClientFactory
 */
public class HttpClientFactoryTest {
    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.enqueue(new MockResponse().setBody("ok"));
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
        server = null;
    }

    @Test
    void http() throws Exception {
        try (CloseableHttpClient client = new HttpClientFactory().build()) {
            final HttpUriRequest request = RequestBuilder.get(server.url("/ping.html").uri()).build();
            try (CloseableHttpResponse response = client.execute(request)) {
                assertEquals("ok", EntityUtils.toString(response.getEntity()));
            }
        }
    }

    @Test
    void context() throws Exception {
        final KeyStore keyStore = loadKeyStore();
        final SSLContext context = SSLContextBuilder.create().loadTrustMaterial(keyStore, new TrustAllStrategy()).build();
        final HttpClientFactory factory = new HttpClientFactory();
        factory.setSslContext(context);

        try (CloseableHttpClient client = factory.build()) {
            final HttpUriRequest request = RequestBuilder.get(server.url("/ping.html").uri()).build();
            try (CloseableHttpResponse response = client.execute(request)) {
                assertEquals("ok", EntityUtils.toString(response.getEntity()));
            }
        }
    }

    @Test
    void proxy() throws Exception {
        final HttpClientFactory factory = new HttpClientFactory();
        factory.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("y1cloud.com", 1080)));

        try (CloseableHttpClient client = factory.build()) {
            final HttpUriRequest request = RequestBuilder.get(server.url("/ping.html").uri()).build();
            try (CloseableHttpResponse response = client.execute(request)) {
                assertEquals("ok", EntityUtils.toString(response.getEntity()));
            }
        }
    }

    private KeyStore loadKeyStore() throws GeneralSecurityException, IOException {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream in = getClass().getResourceAsStream("/cacerts")) {
            keyStore.load(in, "changeit".toCharArray());
        }
        return keyStore;
    }
}