package com.example.interceptor;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class ResponseLoggingInterceptor implements WriterInterceptor {

    private static final Logger LOG = Logger.getLogger(ResponseLoggingInterceptor.class.getName());

    @Override
    public void aroundWriteTo(WriterInterceptorContext ctx)
            throws IOException, WebApplicationException {

        LOG.info("[BODY OUT] Writing entity: " + ctx.getEntity());
        ctx.proceed();
    }
}
