package test.io.github.dbstarll.utils.http.client;

import io.github.dbstarll.utils.http.client.HttpClientFactory;
import junit.framework.TestCase;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

public class TestHttpClientFactory extends TestCase {
  public void testNew() {
    new HttpClientFactory();
  }

  @Test
  public void testBuild() throws IOException {
    final HttpClientFactory factory = new HttpClientFactory();

    final CloseableHttpClient client = factory.build();
    assertNotNull(client);
    client.close();
  }

  @Test
  public void testContext() throws Exception {
    final KeyStore keyStore = loadKeyStore("/cacerts", "changeit".toCharArray());
    final SSLContext context = SSLContextBuilder.create().loadTrustMaterial(keyStore, new TrustAllStrategy()).build();

    final HttpClientFactory factory = new HttpClientFactory();
    factory.setSslContext(context);

    final CloseableHttpClient client = factory.build();
    try {
      final HttpUriRequest request = RequestBuilder.get("https://static.y1cloud.com/ping.html").build();

      final CloseableHttpResponse response = client.execute(request);
      try {
        final String res = EntityUtils.toString(response.getEntity());
        assertEquals("ok\n", res);
      } finally {
        response.close();
      }
    } finally {
      client.close();
    }
  }

  @Test
  public void testProxy() throws Exception {
    final KeyStore keyStore = loadKeyStore("/cacerts", "changeit".toCharArray());
    final SSLContext context = SSLContextBuilder.create().loadTrustMaterial(keyStore, new TrustAllStrategy()).build();

    final HttpClientFactory factory = new HttpClientFactory();
    factory.setSslContext(context);
    factory.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("y1cloud.com", 1080)));
    factory.setResolveFromProxy(true);

    final CloseableHttpClient client = factory.build();
    try {
      final HttpUriRequest request = RequestBuilder.get("https://static.y1cloud.com/ping.html").build();

      final CloseableHttpResponse response = client.execute(request);
      try {
        final String res = EntityUtils.toString(response.getEntity());
        assertEquals("ok\n", res);
      } finally {
        response.close();
      }
    } finally {
      client.close();
    }
  }

  private KeyStore loadKeyStore(String resource, char[] password)
          throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    final KeyStore keyStore = KeyStore.getInstance("JKS");
    final InputStream in = getClass().getResourceAsStream(resource);
    try {
      keyStore.load(in, password);
    } finally {
      in.close();
    }
    return keyStore;
  }
}