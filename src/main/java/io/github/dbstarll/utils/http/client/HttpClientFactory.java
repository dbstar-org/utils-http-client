package io.github.dbstarll.utils.http.client;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.UnknownHostException;

public final class HttpClientFactory {
    public static final int DEFAULT_TIMEOUT = 2000;
    private SSLContext sslContext = SSLContexts.createDefault();
    private Proxy proxy;
    private boolean resolveFromProxy;
    private int socketTimeout = DEFAULT_TIMEOUT;
    private int connectTimeout = DEFAULT_TIMEOUT;
    private boolean automaticRetries = true;
    private HttpRequestRetryHandler retryHandler;

    /**
     * Assigns {@link SSLContext} instance.
     *
     * @param newSslContext SSLContext instance
     * @return this HttpClientFactory
     */
    public HttpClientFactory setSslContext(final SSLContext newSslContext) {
        this.sslContext = newSslContext;
        return this;
    }

    /**
     * 设置proxy.
     *
     * @param newProxy proxy实例
     * @return this HttpClientFactory
     */
    public HttpClientFactory setProxy(final Proxy newProxy) {
        this.proxy = newProxy;
        return this;
    }

    /**
     * 设置是否通过proxy来解析域名.
     *
     * @param newResolveFromProxy 是否通过proxy来解析域名
     * @return this HttpClientFactory
     */
    public HttpClientFactory setResolveFromProxy(final boolean newResolveFromProxy) {
        this.resolveFromProxy = newResolveFromProxy;
        return this;
    }

    /**
     * 设置socket timeout(ms).
     *
     * @param newSocketTimeout socket timeout
     * @return this HttpClientFactory
     */
    public HttpClientFactory setSocketTimeout(final int newSocketTimeout) {
        this.socketTimeout = newSocketTimeout;
        return this;
    }

    /**
     * 设置connect timeout(ms).
     *
     * @param newConnectTimeout connect timeout
     * @return this HttpClientFactory
     */
    public HttpClientFactory setConnectTimeout(final int newConnectTimeout) {
        this.connectTimeout = newConnectTimeout;
        return this;
    }

    /**
     * 设置是否再请求失败时重试，默认为不重试.
     *
     * @param newAutomaticRetries 是否再请求失败时重试
     * @return this HttpClientFactory
     */
    public HttpClientFactory setAutomaticRetries(final boolean newAutomaticRetries) {
        this.automaticRetries = newAutomaticRetries;
        return this;
    }

    /**
     * 设置重试处理器，若不为null，则忽略automaticRetries配置.
     *
     * @param newRetryHandler 重试处理器
     * @return this HttpClientFactory
     */
    public HttpClientFactory setRetryHandler(final HttpRequestRetryHandler newRetryHandler) {
        this.retryHandler = newRetryHandler;
        return this;
    }

    /**
     * 构造CloseableHttpClient.
     *
     * @return CloseableHttpClient
     */
    public CloseableHttpClient build() {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        if (retryHandler != null) {
            builder.setRetryHandler(retryHandler);
        } else if (!automaticRetries) {
            builder.disableAutomaticRetries();
        }
        builder.setDefaultRequestConfig(
                RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build());
        if (proxy == null || proxy.type() == Proxy.Type.DIRECT) {
            builder.setSSLContext(sslContext);
        } else {
            builder.setSSLSocketFactory(new ProxyConnectionSocketFactory(sslContext, proxy, resolveFromProxy));
            if (resolveFromProxy) {
                builder.setDnsResolver(new FakeDnsResolver());
            }
        }
        return builder.build();
    }

    private static class FakeDnsResolver implements DnsResolver {
        @Override
        public InetAddress[] resolve(final String host) throws UnknownHostException {
            // Return some fake DNS record for every request, we won't be using it
            return new InetAddress[]{InetAddress.getByAddress(new byte[]{1, 1, 1, 1})};
        }
    }

    private static class ProxyConnectionSocketFactory extends SSLConnectionSocketFactory {
        private final Proxy proxy;
        private final boolean resolveFromProxy;

        ProxyConnectionSocketFactory(final SSLContext sslContext, final Proxy proxy,
                                     final boolean resolveFromProxy) {
            super(sslContext);
            this.proxy = proxy;
            this.resolveFromProxy = resolveFromProxy;
        }

        @Override
        public Socket createSocket(final HttpContext context) {
            return new Socket(proxy);
        }

        @Override
        public Socket connectSocket(final int connectTimeout,
                                    final Socket socket,
                                    final HttpHost host,
                                    final InetSocketAddress remoteAddress,
                                    final InetSocketAddress localAddress,
                                    final HttpContext context) throws IOException {
            if (resolveFromProxy) {
                final InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(),
                        remoteAddress.getPort());
                return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
            } else {
                return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
            }
        }
    }

    /**
     * 构造一个代理实例.
     *
     * @param type     代理类型
     * @param hostname 代理主机名
     * @param port     代理端口
     * @return 代理实例
     */
    public static Proxy proxy(final Proxy.Type type, final String hostname, final int port) {
        if (type == Type.DIRECT) {
            return Proxy.NO_PROXY;
        } else {
            return new Proxy(type, new InetSocketAddress(hostname, port));
        }
    }
}
