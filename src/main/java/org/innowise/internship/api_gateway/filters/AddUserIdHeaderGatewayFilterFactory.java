package org.innowise.internship.api_gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AddUserIdHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final Logger log = LoggerFactory.getLogger(AddUserIdHeaderGatewayFilterFactory.class);

    public AddUserIdHeaderGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            log.info("AddUserIdHeaderGatewayFilter called for path: {}", exchange.getRequest().getPath());

            return ReactiveSecurityContextHolder.getContext()
                    .flatMap(ctx -> {
                        Authentication auth = ctx.getAuthentication();
                        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
                            log.warn("No JWT authentication found, passing request as is");
                            return chain.filter(exchange);
                        }

                        String userId = jwt.getSubject();
                        log.info("JWT subject (userId): {}", userId);

                        ServerHttpRequest mutated = exchange.getRequest()
                                .mutate()
                                .header("X-User-Id", userId)
                                .headers(headers -> headers.remove("Authorization"))
                                .build();

                        return chain.filter(exchange.mutate().request(mutated).build());
                    })
                    .switchIfEmpty(chain.filter(exchange)); // если SecurityContext пустой, просто пропускаем
        };
    }
}
