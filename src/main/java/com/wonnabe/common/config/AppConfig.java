// src/main/java/com/wonnabe/common/config/AppConfig.java
package com.wonnabe.common.config;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    @Bean(destroyMethod = "close")
    public PoolingHttpClientConnectionManager httpClientCM() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(20);
        cm.setValidateAfterInactivity(1_000);
        return cm;
    }

    private ConnectionKeepAliveStrategy keepAlive() {
        return (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                if ("timeout".equalsIgnoreCase(he.getName()) && he.getValue() != null) {
                    try { return Long.parseLong(he.getValue()) * 1000L; } catch (NumberFormatException ignore) {}
                }
            }
            return 60_000L; // default 60s
        };
    }

    private HttpRequestRetryHandler retryNoHttpResponseOnly() {
        return (ex, count, ctx) -> (count <= 2 && ex instanceof NoHttpResponseException);
    }

    private CloseableHttpClient buildClient(PoolingHttpClientConnectionManager cm, int connectMs, int readMs) {
        RequestConfig rc = RequestConfig.custom()
                .setConnectTimeout(connectMs)
                .setSocketTimeout(readMs)
                .setConnectionRequestTimeout(2_000)
                .setExpectContinueEnabled(false)
                .build();

        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(rc)
                .setKeepAliveStrategy(keepAlive())
                .setRetryHandler(retryNoHttpResponseOnly())
                .evictIdleConnections(30, TimeUnit.SECONDS)
                .evictExpiredConnections()
                .build();
    }

    @Bean
    @Primary
    @Qualifier("restTemplateFast")
    public RestTemplate restTemplateFast(PoolingHttpClientConnectionManager cm) {
        var http = buildClient(cm, 5_000, 30_000);
        var f = new HttpComponentsClientHttpRequestFactory(http);
        f.setConnectTimeout(5_000);
        f.setReadTimeout(30_000);
        f.setConnectionRequestTimeout(2_000);
        return new RestTemplate(f);
    }

    @Bean
    @Qualifier("restTemplateSlow")
    public RestTemplate restTemplateSlow(PoolingHttpClientConnectionManager cm) {
        var http = buildClient(cm, 5_000, 90_000);
        var f = new HttpComponentsClientHttpRequestFactory(http);
        f.setConnectTimeout(5_000);
        f.setReadTimeout(90_000);
        f.setConnectionRequestTimeout(2_000);
        return new RestTemplate(f);
    }
}
