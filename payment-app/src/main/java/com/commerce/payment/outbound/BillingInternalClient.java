package com.commerce.payment.outbound;

import com.commerce.payment.outbound.internal.api.CustomerStatusApi;
import com.commerce.payment.outbound.internal.api.InvoicePaymentsApi;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * The client of {@code commerce-app}, generated from `docs/contracts/billing-internal.openapi.json` 1.0.0, at
 * {@code commerce.billing-internal.base-url}. Unreachable, or slower than 2 seconds, is a failure (BR-5, BR-27, BR-32;
 * architect, TL, 2026-10-20): it reaches the use case as an exception.
 */
@Configuration(proxyBeanMethods = false)
class BillingInternalClient {

    static final Duration LIMIT = Duration.ofSeconds(2);

    @Bean
    HttpServiceProxyFactory billingInternal(@Value("${commerce.billing-internal.base-url}") String baseUrl) {
        JdkClientHttpRequestFactory requests =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(LIMIT).build());
        requests.setReadTimeout(LIMIT);
        RestClient client = RestClient.builder().baseUrl(baseUrl).requestFactory(requests).build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build();
    }

    @Bean
    InvoicePaymentsApi invoicePaymentsApi(HttpServiceProxyFactory billingInternal) {
        return billingInternal.createClient(InvoicePaymentsApi.class);
    }

    @Bean
    CustomerStatusApi customerStatusApi(HttpServiceProxyFactory billingInternal) {
        return billingInternal.createClient(CustomerStatusApi.class);
    }
}
