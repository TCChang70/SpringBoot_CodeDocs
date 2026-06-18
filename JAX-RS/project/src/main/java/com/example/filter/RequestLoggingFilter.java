package com.example.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Provider
public class RequestLoggingFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        LOG.info(String.format("[%s] %s %s | IP: %s",
            LocalDateTime.now(),
            ctx.getMethod(),
            ctx.getUriInfo().getRequestUri(),
            ctx.getHeaderString("X-Forwarded-For") != null
                ? ctx.getHeaderString("X-Forwarded-For")
                : "unknown"
        ));
    }
}
