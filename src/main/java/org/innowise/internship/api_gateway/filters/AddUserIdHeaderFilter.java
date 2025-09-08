package org.innowise.internship.api_gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

public class AddUserIdHeaderFilter extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {
    public AddUserIdHeaderFilter() {
        super(NameConfig.class);
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        return (exchange, chain) ->
                ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .map(Authentication::getPrincipal)
                        .cast(Jwt.class)
                        .flatMap(jwt -> {
                            String userId = jwt.getSubject();
                            ServerHttpRequest mutateRequest = exchange.getRequest()
                                    .mutate()
                                    .header("X-User-Id", userId)
                                    .headers(headers -> headers.remove("Authorization"))
                                    .build();

                            return chain.filter(
                                    exchange.mutate()
                                            .request(mutateRequest)
                                            .build()
                            );
                        });
    }
}
