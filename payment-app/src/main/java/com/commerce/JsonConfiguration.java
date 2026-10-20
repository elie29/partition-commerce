package com.commerce;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

/**
 * A value of the wrong JSON type is refused, as the OpenAPI contracts type it, never coerced: {@code 1.5} or
 * {@code "100"} is no integer, {@code 42} is no string. A copy of {@code commerce-app}'s: no shared module in this
 * version (TL, 2026-10-20).
 */
@Configuration(proxyBeanMethods = false)
class JsonConfiguration {

    @Bean
    JsonMapperBuilderCustomizer strictJsonTypes() {
        return builder -> builder
                .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .withCoercionConfig(LogicalType.Textual, config -> config
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
    }
}
