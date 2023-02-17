package io.github.dbstarll.utils.http.client;

import io.github.dbstarll.utils.lang.security.InstanceException;
import io.github.dbstarll.utils.lang.security.KeyPairGeneratorAlgorithm;
import io.github.dbstarll.utils.lang.security.KeyStoreAlgorithm;
import io.github.dbstarll.utils.lang.security.SecureRandomAlgorithm;
import io.github.dbstarll.utils.lang.security.SecurityFactory;
import io.github.dbstarll.utils.lang.security.SignatureAlgorithm;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

import javax.net.ssl.SSLContext;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 测试HttpClientFactory
 */
class HttpClientFactoryTest {
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
        final Proxy proxy = HttpClientFactory.proxy(Type.SOCKS, "y1cloud.com", 1080);
        assertEquals(Type.SOCKS, proxy.type());
        final InetSocketAddress address = assertInstanceOf(InetSocketAddress.class, proxy.address());
        assertEquals("y1cloud.com", address.getHostName());
        assertEquals("y1cloud.com", address.getHostString());
        assertEquals(1080, address.getPort());
        assertFalse(address.isUnresolved());
        assertNotNull(address.getAddress());
    }

    @Test
    void direct() {
        final Proxy proxy = HttpClientFactory.proxy(Type.DIRECT, "y1cloud.com", 1080);
        assertSame(Proxy.NO_PROXY, proxy);
        assertEquals(Type.DIRECT, proxy.type());
        assertNull(proxy.address());
    }

    @Test
    void http() throws Throwable {
        useServer(server -> {
            try (CloseableHttpClient client = new HttpClientFactory().build()) {
                final HttpUriRequest request = RequestBuilder.get(server.url("/ping.html").uri()).build();
                try (CloseableHttpResponse response = client.execute(request)) {
                    assertEquals("ok", EntityUtils.toString(response.getEntity()));
                }
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
            final HttpClientFactory factory = new HttpClientFactory();
            final SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(keyStore, null)
                    .setSecureRandom(random).build();
            factory.setSslContext(sslContext);

            try (CloseableHttpClient client = factory.build()) {
                final HttpUriRequest request = RequestBuilder.get(server.url("/ping.html").uri()).build();
                try (CloseableHttpResponse response = client.execute(request)) {
                    assertEquals("ok", EntityUtils.toString(response.getEntity()));
                }
            }
        }, s -> {
            final SSLContext sslContext = SSLContextBuilder.create().loadKeyMaterial(keyStore, password)
                    .setSecureRandom(random).build();
            s.useHttps(sslContext.getSocketFactory(), false);
        });
    }

    @Test
    void proxy() throws Throwable {
        useServer(server -> {
            final HttpClientFactory factory = new HttpClientFactory();
            factory.setProxy(HttpClientFactory.proxy(Type.SOCKS, "y1cloud.com", 1080));

            try (CloseableHttpClient client = factory.build()) {
                final HttpUriRequest request = RequestBuilder.get(server.url("/ping.html").uri()).build();
                try (CloseableHttpResponse response = client.execute(request)) {
                    assertEquals("ok", EntityUtils.toString(response.getEntity()));
                }
            }
        });
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