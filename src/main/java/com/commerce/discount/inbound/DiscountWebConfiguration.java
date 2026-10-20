package com.commerce.discount.inbound;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Checks the headers of {@link DiscountController} before binding ({@link DiscountRequestCheck}). The base path is the
 * controller's own type-level mapping: a path prefix is keyed by its text, so two contexts under one prefix would
 * replace each other's.
 */
@Configuration(proxyBeanMethods = false)
class DiscountWebConfiguration implements WebMvcConfigurer {

    private final DiscountRequestCheck requestCheck;

    DiscountWebConfiguration(DiscountRequestCheck requestCheck) {
        this.requestCheck = requestCheck;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestCheck).addPathPatterns(
                DiscountController.BASE_PATH + "/discounts", DiscountController.BASE_PATH + "/discounts/**");
    }
}
