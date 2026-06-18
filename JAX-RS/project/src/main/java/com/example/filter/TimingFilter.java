package com.example.filter;

import com.example.annotation.Timed;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
@Timed
public class TimingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(TimingFilter.class.getName());
    private static final String START_TIME = "startTime";

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        ctx.setProperty(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext req,
                       ContainerResponseContext res) throws IOException {
        Long start = (Long) req.getProperty(START_TIME);
        if (start != null) {
            long elapsed = System.currentTimeMillis() - start;
            LOG.info(String.format("[TIMING] %s %s → %d ms",
                req.getMethod(),
                req.getUriInfo().getPath(),
                elapsed));
            res.getHeaders().add("X-Response-Time", elapsed + "ms");
        }
    }
}
