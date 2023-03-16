package io.github.dbstarll.utils.http.client;

import io.github.dbstarll.utils.lang.security.InstanceException;
import io.github.dbstarll.utils.lang.security.KeyPairGeneratorAlgorithm;
import io.github.dbstarll.utils.lang.security.KeyStoreAlgorithm;
import io.github.dbstarll.utils.lang.security.SecureRandomAlgorithm;
import io.github.dbstarll.utils.lang.security.SecurityFactory;
import io.github.dbstarll.utils.lang.security.SignatureAlgorithm;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

/**
 * 测试HttpClientFactory
 */
class HttpClientFactoryTest {
    private static final String TOKEN_PROXY_AUTH = "PROXY_AUTH";

    private static final String PROXY_HOST = "proxy.y1cloud.com";
    private static final int PROXY_PORT = 33031;

    private String proxyUsername;
    private String proxyPassword;

    @BeforeEach
    void setUp() {
        Authenticator.setDefault(getAuthenticator());
    }

    private Authenticator getAuthenticator() {
        final String proxyAuth = getProxyAuth();
        if (StringUtils.isNotBlank(proxyAuth)) {
            final int split = proxyAuth.indexOf(':');
            if (split > 0) {
                proxyUsername = proxyAuth.substring(0, split);
                proxyPassword = proxyAuth.substring(split + 1);
                return new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
                    }
                };
            }
        }
        return null;
    }

    private String getProxyAuth() {
        final String keyFromProperty = System.getProperty(TOKEN_PROXY_AUTH);
        if (StringUtils.isNotBlank(keyFromProperty)) {
            return keyFromProperty;
        }

        final String opts = System.getenv("MAVEN_OPTS");
        if (StringUtils.isNotBlank(opts)) {
            for (String opt : StringUtils.split(opts)) {
                if (opt.startsWith("-D" + TOKEN_PROXY_AUTH + "=")) {
                    return opt.substring(3 + TOKEN_PROXY_AUTH.length());
                }
            }
        }

        return null;
    }

    @AfterEach
    void tearDown() {
        Authenticator.setDefault(null);
    }

    @SafeVarargs
    private final void useServer(final ThrowingConsumer<MockWebServer> consumer,
                                 final ThrowingConsumer<MockWebServer>... customizers) throws Throwable {
        try (final MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setBody("ok"));
            for (ThrowingConsumer<MockWebServer> c : customizers) {
                c.accept(server);
            }
            server.start();
            consumer.accept(server);
        }
    }

    @Test
    void socks() {
        final Proxy proxy = HttpClientFactory.proxy(Type.SOCKS, PROXY_HOST, PROXY_PORT);
        assertEquals(Type.SOCKS, proxy.type());
        final InetSocketAddress address = assertInstanceOf(InetSocketAddress.class, proxy.address());
        assertEquals(PROXY_HOST, address.getHostName());
        assertEquals(PROXY_HOST, address.getHostString());
        assertEquals(PROXY_PORT, address.getPort());
        assertFalse(address.isUnresolved());
        assertNotNull(address.getAddress());
    }

    @Test
    void direct() {
        final Proxy proxy = HttpClientFactory.proxy(Type.DIRECT, PROXY_HOST, PROXY_PORT);
        assertSame(Proxy.NO_PROXY, proxy);
        assertEquals(Type.DIRECT, proxy.type());
        assertNull(proxy.address());
    }

    @Test
    void http() throws Throwable {
        useServer(server -> {
            try (CloseableHttpClient client = new HttpClientFactory().setAutomaticRetries(false)
                    .build(HttpClientBuilder::disableAutomaticRetries)) {
                final ClassicHttpRequest request = ClassicRequestBuilder.get(server.url("/ping.html").uri()).build();
                assertEquals("ok", client.execute(request, new BasicHttpClientResponseHandler()));
            }
        });
    }

    @Test
    void httpAsync() throws Throwable {
        useServer(server -> {
            try (CloseableHttpAsyncClient client = new HttpClientFactory().setAutomaticRetries(false)
                    .buildAsync(HttpAsyncClientBuilder::disableAutomaticRetries)) {
                client.start();
                final SimpleHttpRequest request = SimpleRequestBuilder.get(server.url("/ping.html").uri()).build();
                final Future<SimpleHttpResponse> future = client.execute(request, null);
                assertEquals("ok", future.get().getBodyText());
            }
        });
    }

    @Test
    void https() throws Throwable {
        final SecureRandom random = SecurityFactory.builder(SecureRandomAlgorithm.SHA1PRNG).build();
        final KeyPair keyPair = genKeyPair(random);
        final ContentSigner signer = signer(keyPair.getPrivate(), random);
        final char[] password = "changeit".toCharArray();

        final X500Name subject = new X500NameBuilder().addRDN(BCStyle.CN, "localhost").build();
        final PKCS10CertificationRequest csr = csr(subject, keyPair.getPublic(), signer);
        final X509Certificate crt = crt(csr, subject, signer, random);

        final KeyStore keyStore = SecurityFactory.builder(KeyStoreAlgorithm.JKS).load(null, null).build();
        keyStore.setKeyEntry("localhost", keyPair.getPrivate(), password, new X509Certificate[]{crt});

        useServer(server -> {
            final SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(keyStore, null)
                    .setSecureRandom(random).build();
            try (CloseableHttpClient client = new HttpClientFactory().setSslContext(sslContext).build()) {
                final ClassicHttpRequest request = ClassicRequestBuilder.get(server.url("/ping.html").uri()).build();
                assertEquals("ok", client.execute(request, new BasicHttpClientResponseHandler()));
            }
        }, s -> {
            final SSLContext sslContext = SSLContextBuilder.create().loadKeyMaterial(keyStore, password)
                    .setSecureRandom(random).build();
            s.useHttps(sslContext.getSocketFactory(), false);
        });
    }

    @Test
    void httpsAsync() throws Throwable {
        final SecureRandom random = SecurityFactory.builder(SecureRandomAlgorithm.SHA1PRNG).build();
        final KeyPair keyPair = genKeyPair(random);
        final ContentSigner signer = signer(keyPair.getPrivate(), random);
        final char[] password = "changeit".toCharArray();

        final X500Name subject = new X500NameBuilder().addRDN(BCStyle.CN, "localhost").build();
        final PKCS10CertificationRequest csr = csr(subject, keyPair.getPublic(), signer);
        final X509Certificate crt = crt(csr, subject, signer, random);

        final KeyStore keyStore = SecurityFactory.builder(KeyStoreAlgorithm.JKS).load(null, null).build();
        keyStore.setKeyEntry("localhost", keyPair.getPrivate(), password, new X509Certificate[]{crt});

        useServer(server -> {
            final SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(keyStore, null)
                    .setSecureRandom(random).build();
            try (CloseableHttpAsyncClient client = new HttpClientFactory().setSslContext(sslContext).buildAsync()) {
                client.start();
                final SimpleHttpRequest request = SimpleRequestBuilder.get(server.url("/ping.html").uri()).build();
                final Future<SimpleHttpResponse> future = client.execute(request, null);
                assertEquals("ok", future.get().getBodyText());
            }
        }, s -> {
            final SSLContext sslContext = SSLContextBuilder.create().loadKeyMaterial(keyStore, password)
                    .setSecureRandom(random).build();
            s.useHttps(sslContext.getSocketFactory(), false);
        });
    }

    @Test
    void proxy() throws Throwable {
        try (CloseableHttpClient client = new HttpClientFactory().setSocketTimeout(5000).setConnectTimeout(5000)
                .setProxy(HttpClientFactory.proxy(Type.SOCKS, PROXY_HOST, PROXY_PORT)).build()) {
            final ClassicHttpRequest request = ClassicRequestBuilder.get("https://static.y1cloud.com/ping.html").build();
            assertEquals("ok\n", client.execute(request, new BasicHttpClientResponseHandler()));
        }
    }

//    @Test
//    void proxyAsync() throws Throwable {
//        try (CloseableHttpAsyncClient client = new HttpClientFactory().setSocketTimeout(5000).setConnectTimeout(5000)
//                .setProxy(HttpClientFactory.proxy(Type.SOCKS, PROXY_HOST, PROXY_PORT))
//                .buildAsync()) {
//            client.start();
//            final SimpleHttpRequest request = SimpleRequestBuilder.get("https://static.y1cloud.com/ping.html").build();
//            final Future<SimpleHttpResponse> future = client.execute(request, null);
//            assertEquals("ok\n", future.get().getBodyText());
//        }
//    }

    @Test
    void proxyWithContext() throws Throwable {
        try (CloseableHttpClient client = new HttpClientFactory().setSocketTimeout(5000).setConnectTimeout(5000)
                .setProxy(HttpClientFactory.proxy(Type.SOCKS, PROXY_HOST, PROXY_PORT))
                .setSslContext(SSLContexts.createDefault()).build()) {
            final ClassicHttpRequest request = ClassicRequestBuilder.get("https://static.y1cloud.com/ping.html").build();
            assertEquals("ok\n", client.execute(request, new BasicHttpClientResponseHandler()));
        }
    }

    @Test
    void proxyDirect() throws Throwable {
        try (CloseableHttpClient client = new HttpClientFactory()
                .setProxy(HttpClientFactory.proxy(Type.DIRECT, PROXY_HOST, PROXY_PORT)).build()) {
            final ClassicHttpRequest request = ClassicRequestBuilder.get("https://static.y1cloud.com/ping.html").build();
            assertEquals("ok\n", client.execute(request, new BasicHttpClientResponseHandler()));
        }
    }

    @Test
    void resolveFromProxy() throws Throwable {
        try (CloseableHttpClient client = new HttpClientFactory().setSocketTimeout(5000).setConnectTimeout(5000)
                .setProxy(HttpClientFactory.proxy(Type.SOCKS, PROXY_HOST, PROXY_PORT))
                .setResolveFromProxy(true).build()) {
            final ClassicHttpRequest request = ClassicRequestBuilder.get("https://static.y1cloud.com/ping.html").build();
            assertEquals("ok\n", client.execute(request, new BasicHttpClientResponseHandler()));
        }
    }

    @Test
    void retry() throws Throwable {
        final AtomicInteger retry = new AtomicInteger();
        final HttpRequestRetryStrategy retryHandler = new DefaultHttpRequestRetryStrategy(3, TimeValue.ofSeconds(1L)) {
            @Override
            public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
                retry.incrementAndGet();
                return super.retryRequest(request, exception, execCount, context);
            }
        };
        try (CloseableHttpClient client = new HttpClientFactory()
                .setRetryStrategy(retryHandler)
                .setProxy(HttpClientFactory.proxy(Type.SOCKS, PROXY_HOST, 1080))
                .build()) {
            final ClassicHttpRequest request = ClassicRequestBuilder.get("https://static.y1cloud.com/ping.html").build();
            final Exception e = assertThrowsExactly(SocketException.class, () -> client.execute(request, new BasicHttpClientResponseHandler()));
            assertEquals("connect timed out", e.getMessage());
            assertEquals(4, retry.get());
        }
    }

    private static KeyPair genKeyPair(final SecureRandom random) throws InstanceException, NoSuchAlgorithmException {
        return SecurityFactory.builder(KeyPairGeneratorAlgorithm.RSA).keySize(2048, random).build().genKeyPair();
    }

    private static ContentSigner signer(final PrivateKey privateKey, final SecureRandom random) throws OperatorCreationException {
        return new JcaContentSignerBuilder(SignatureAlgorithm.SHA256withRSA.name()).setSecureRandom(random).build(privateKey);
    }

    private static PKCS10CertificationRequest csr(final X500Name subject, final PublicKey publicKey,
                                                  final ContentSigner signer) {
        return new JcaPKCS10CertificationRequestBuilder(subject, publicKey).build(signer);
    }

    private static X509Certificate crt(final PKCS10CertificationRequest csr, final X500Name issuer,
                                       final ContentSigner signer, final SecureRandom random) throws CertificateException {
        final BigInteger serial = BigInteger.probablePrime(128, random);
        final Date notBefore = new Date();
        final Date notAfter = DateUtils.addYears(notBefore, 1);
        final X509CertificateHolder holder = new X509v3CertificateBuilder(issuer, serial, notBefore, notAfter,
                csr.getSubject(), csr.getSubjectPublicKeyInfo()).build(signer);
        return new JcaX509CertificateConverter().getCertificate(holder);
    }
}