package com.commerce.pricelist.inbound;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Checks the headers of {@link PriceListController} before binding ({@link PriceListRequestCheck}). The base path is
 * the controller's own type-level mapping: a path prefix is keyed by its text, so two contexts under one prefix would
 * replace each other's.
 */
@Configuration(proxyBeanMethods = false)
class PriceListWebConfiguration implements WebMvcConfigurer {

    private final PriceListRequestCheck requestCheck;

    PriceListWebConfiguration(PriceListRequestCheck requestCheck) {
        this.requestCheck = requestCheck;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestCheck).addPathPatterns(
                PriceListController.BASE_PATH + "/price-lists", PriceListController.BASE_PATH + "/price-lists/**");
    }
}
