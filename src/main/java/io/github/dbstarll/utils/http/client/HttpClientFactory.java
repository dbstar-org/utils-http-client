package io.github.dbstarll.utils.http.client;

import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.InMemoryDnsResolver;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.function.Consumer;

public final class HttpClientFactory {
    public static final Timeout DEFAULT_TIMEOUT = Timeout.ofSeconds(2);
    private SSLContext sslContext;
    private Proxy proxy;
    private boolean resolveFromProxy;
    private Timeout socketTimeout = DEFAULT_TIMEOUT;
    private Timeout connectTimeout = DEFAULT_TIMEOUT;
    private boolean automaticRetries = true;
    private HttpRequestRetryStrategy retryStrategy;

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
        this.socketTimeout = Timeout.ofMilliseconds(newSocketTimeout);
        return this;
    }

    /**
     * 设置connect timeout(ms).
     *
     * @param newConnectTimeout connect timeout
     * @return this HttpClientFactory
     */
    public HttpClientFactory setConnectTimeout(final int newConnectTimeout) {
        this.connectTimeout = Timeout.ofMilliseconds(newConnectTimeout);
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
     * @param newRetryStrategy 重试处理器
     * @return this HttpClientFactory
     */
    public HttpClientFactory setRetryStrategy(final HttpRequestRetryStrategy newRetryStrategy) {
        this.retryStrategy = newRetryStrategy;
        return this;
    }

    /**
     * 构造CloseableHttpClient.
     *
     * @param consumers 用于对HttpClientBuilder的自定义
     * @return CloseableHttpClient
     */
    @SafeVarargs
    public final CloseableHttpClient build(final Consumer<HttpClientBuilder>... consumers) {
        final HttpClientBuilder builder = HttpClients.custom().setConnectionManager(buildConnectionManager());
        if (retryStrategy != null) {
            builder.setRetryStrategy(retryStrategy);
        } else if (!automaticRetries) {
            builder.disableAutomaticRetries();
        }
        Arrays.stream(consumers).forEach(c -> c.accept(builder));
        return builder.build();
    }

    private HttpClientConnectionManager buildConnectionManager() {
        final PoolingHttpClientConnectionManagerBuilder builder = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(buildConnectionConfig());
        if (sslContext != null) {
            builder.setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create().setSslContext(sslContext).build());
        }
        if (proxy != null && proxy.type() == Type.SOCKS) {
            builder.setSSLSocketFactory(new ProxyConnectionSocketFactory(sslContext, proxy, resolveFromProxy));
            if (resolveFromProxy) {
                builder.setDnsResolver(new FakeDnsResolver());
            }
        }
        return builder.build();
    }

    /**
     * 构造CloseableHttpAsyncClient.
     *
     * @param consumers 用于对HttpAsyncClientBuilder的自定义
     * @return CloseableHttpAsyncClient
     */
    @SafeVarargs
    public final CloseableHttpAsyncClient buildAsync(final Consumer<HttpAsyncClientBuilder>... consumers) {
        final HttpAsyncClientBuilder builder = HttpAsyncClients.custom()
                .setConnectionManager(buildConnectionManagerAsync())
                .setIOReactorConfig(buildIOReactorConfig());
        if (retryStrategy != null) {
            builder.setRetryStrategy(retryStrategy);
        } else if (!automaticRetries) {
            builder.disableAutomaticRetries();
        }
        Arrays.stream(consumers).forEach(c -> c.accept(builder));
        return builder.build();
    }

    private AsyncClientConnectionManager buildConnectionManagerAsync() {
        final PoolingAsyncClientConnectionManagerBuilder builder = PoolingAsyncClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(buildConnectionConfig());
        if (sslContext != null) {
            builder.setTlsStrategy(ClientTlsStrategyBuilder.create().setSslContext(sslContext).build());
        }
        // Async的proxy模式暂时无法实现
//        if (proxy != null && proxy.type() == Type.SOCKS && resolveFromProxy) {
//            builder.setDnsResolver(new FakeDnsResolver());
//        }
        return builder.build();
    }

    private ConnectionConfig buildConnectionConfig() {
        return ConnectionConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();
    }

    private IOReactorConfig buildIOReactorConfig() {
        final IOReactorConfig.Builder builder = IOReactorConfig.custom().setSoTimeout(socketTimeout);
        if (proxy != null && proxy.type() == Type.SOCKS) {
            builder.setSocksProxyAddress(proxy.address());
        }
        return builder.build();
    }

    private static class FakeDnsResolver extends InMemoryDnsResolver {
        @Override
        public InetAddress[] resolve(final String host) throws UnknownHostException {
            // Return some fake DNS record for every request, we won't be using it
            return new InetAddress[]{InetAddress.getByAddress(new byte[]{1, 1, 1, 1})};
        }
    }

    private static class ProxyConnectionSocketFactory extends SSLConnectionSocketFactory {
        private final Proxy proxy;
        private final boolean resolveFromProxy;

        ProxyConnectionSocketFactory(final SSLContext sslContext, final Proxy proxy, final boolean resolveFromProxy) {
            super(sslContext != null ? sslContext : SSLContexts.createDefault());
            this.proxy = proxy;
            this.resolveFromProxy = resolveFromProxy;
        }

        @Override
        public Socket createSocket(final HttpContext context) {
            return new Socket(proxy);
        }

        @Override
        public Socket connectSocket(final Socket socket, final HttpHost host, final InetSocketAddress remoteAddress,
                                    final InetSocketAddress localAddress, final Timeout connectTimeout,
                                    final Object attachment, final HttpContext ctx) throws IOException {
            if (resolveFromProxy) {
                final InetSocketAddress unresolved = InetSocketAddress.createUnresolved(host.getHostName(),
                        remoteAddress.getPort());
                return super.connectSocket(socket, host, unresolved, localAddress, connectTimeout, attachment, ctx);
            } else {
                return super.connectSocket(socket, host, remoteAddress, localAddress, connectTimeout, attachment, ctx);
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
