package com.commerce.product.inbound;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Checks the headers and the query of {@link ProductController} before binding ({@link ProductRequestCheck}). The base
 * path is the controller's own type-level mapping: a path prefix is keyed by its text, so two contexts under one prefix
 * would replace each other's.
 */
@Configuration(proxyBeanMethods = false)
class ProductWebConfiguration implements WebMvcConfigurer {

    private final ProductRequestCheck requestCheck;

    ProductWebConfiguration(ProductRequestCheck requestCheck) {
        this.requestCheck = requestCheck;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestCheck)
                .addPathPatterns(ProductController.BASE_PATH + "/products", ProductController.BASE_PATH + "/products/**");
    }
}
