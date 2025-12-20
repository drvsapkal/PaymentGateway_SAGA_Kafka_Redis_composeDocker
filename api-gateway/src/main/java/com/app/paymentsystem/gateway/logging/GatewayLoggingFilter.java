package com.app.paymentsystem.gateway.logging;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GatewayLoggingFilter implements GlobalFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        final long start = System.currentTimeMillis();

        // ✅ Generate correlationId ONCE (effectively final)
        final String correlationId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(CORRELATION_ID_HEADER) != null
                        ? exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER)
                        : UUID.randomUUID().toString();

        // ✅ Put into MDC (for gateway logs)
        MDC.put(CORRELATION_ID_HEADER, correlationId);

        // ✅ Mutate request to propagate correlationId downstream
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.header(CORRELATION_ID_HEADER, correlationId))
                .build();

        return chain.filter(mutatedExchange)
                .doFinally(signal -> {
                    long duration = System.currentTimeMillis() - start;

                    String method = exchange.getRequest().getMethod() != null
                            ? exchange.getRequest().getMethod().name()
                            : "UNKNOWN";

                    String path = exchange.getRequest().getPath().value();

                    HttpStatusCode status = exchange.getResponse().getStatusCode();
                    Integer statusCode = status != null ? status.value() : null;

                    // ✅ Add correlationId to response
                    exchange.getResponse()
                            .getHeaders()
                            .add(CORRELATION_ID_HEADER, correlationId);

                    log.info(
                            "HTTP_REQUEST method={} path={} status={} duration_ms={} correlationId={}",
                            method,
                            path,
                            statusCode,
                            duration,
                            correlationId
                    );

                    // ✅ VERY IMPORTANT: clean MDC
                    MDC.remove(CORRELATION_ID_HEADER);
                });
    }
}
