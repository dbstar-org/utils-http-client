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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;

public class HttpClientFactory {
  private SSLContext sslContext = SSLContexts.createDefault();
  private Proxy proxy;
  private boolean resolveFromProxy;
  private int socketTimeout = 2000;
  private int connectTimeout = 2000;
  private boolean automaticRetries = false;
  private HttpRequestRetryHandler retryHandler;

  public void setSslContext(SSLContext sslContext) {
    this.sslContext = sslContext;
  }

  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  public void setResolveFromProxy(boolean resolveFromProxy) {
    this.resolveFromProxy = resolveFromProxy;
  }

  public void setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public void setAutomaticRetries(boolean automaticRetries) {
    this.automaticRetries = automaticRetries;
  }

  public void setRetryHandler(HttpRequestRetryHandler retryHandler) {
    this.retryHandler = retryHandler;
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
    public InetAddress[] resolve(String host) throws UnknownHostException {
      // Return some fake DNS record for every request, we won't be using it
      return new InetAddress[]{InetAddress.getByAddress(new byte[]{1, 1, 1, 1})};
    }
  }

  private static class ProxyConnectionSocketFactory extends SSLConnectionSocketFactory {
    private final Proxy proxy;
    private final boolean resolveFromProxy;

    public ProxyConnectionSocketFactory(final SSLContext sslContext, final Proxy proxy,
                                        final boolean resolveFromProxy) {
      super(sslContext);
      this.proxy = proxy;
      this.resolveFromProxy = resolveFromProxy;
    }

    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
      return new Socket(proxy);
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
                                InetSocketAddress localAddress, HttpContext context) throws IOException {
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
