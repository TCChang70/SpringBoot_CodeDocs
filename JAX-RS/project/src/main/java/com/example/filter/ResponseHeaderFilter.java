package com.example.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class ResponseHeaderFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req,
                       ContainerResponseContext res) throws IOException {
        res.getHeaders().add("X-API-Version", "1.0");
        res.getHeaders().add("X-Powered-By", "Jersey JAX-RS");
    }
}
