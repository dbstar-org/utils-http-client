package io.github.dbstarll.utils.http.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 测试HttpClientFactory
 */
public class HttpClientFactoryTest {
    @Test
    void build() throws IOException {
        final HttpClientFactory factory = new HttpClientFactory();
        final CloseableHttpClient client = factory.build();
        assertNotNull(client);
        client.close();
    }

    @Test
    void context() throws Exception {
        final KeyStore keyStore = loadKeyStore();
        final SSLContext context = SSLContextBuilder.create().loadTrustMaterial(keyStore, new TrustAllStrategy()).build();

        final HttpClientFactory factory = new HttpClientFactory();
        factory.setSslContext(context);

        try (CloseableHttpClient client = factory.build()) {
            final HttpUriRequest request = RequestBuilder.get("https://static.y1cloud.com/ping.html").build();

            try (CloseableHttpResponse response = client.execute(request)) {
                final String res = EntityUtils.toString(response.getEntity());
                assertEquals("ok\n", res);
            }
        }
    }

    @Test
    void proxy() throws Exception {
        final KeyStore keyStore = loadKeyStore();
        final SSLContext context = SSLContextBuilder.create().loadTrustMaterial(keyStore, new TrustAllStrategy()).build();

        final HttpClientFactory factory = new HttpClientFactory();
        factory.setSslContext(context);
        factory.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("y1cloud.com", 1080)));
        factory.setResolveFromProxy(true);

        try (CloseableHttpClient client = factory.build()) {
            final HttpUriRequest request = RequestBuilder.get("https://static.y1cloud.com/ping.html").build();

            try (CloseableHttpResponse response = client.execute(request)) {
                final String res = EntityUtils.toString(response.getEntity());
                assertEquals("ok\n", res);
            }
        }
    }

    private KeyStore loadKeyStore()
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream in = getClass().getResourceAsStream("/cacerts")) {
            keyStore.load(in, "changeit".toCharArray());
        }
        return keyStore;
    }
}