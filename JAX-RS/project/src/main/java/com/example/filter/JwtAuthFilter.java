package com.example.filter;

import com.example.annotation.Secured;
import com.example.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

@Provider
@Secured
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String authHeader = ctx.getHeaderString(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            abortUnauthorized(ctx, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(TOKEN_PREFIX.length()).trim();

        try {
            Claims claims = JwtUtil.parseToken(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            ctx.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return () -> username;
                }

                @Override
                public boolean isUserInRole(String r) {
                    return role != null && role.equalsIgnoreCase(r);
                }

                @Override
                public boolean isSecure() {
                    return ctx.getSecurityContext().isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return "Bearer";
                }
            });

        } catch (JwtException e) {
            abortUnauthorized(ctx, "Invalid or expired token");
        }
    }

    private void abortUnauthorized(ContainerRequestContext ctx, String message) {
        ctx.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Bearer realm=\"JAX-RS API\"")
                    .entity("{\"message\":\"" + message + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build()
        );
    }
}
