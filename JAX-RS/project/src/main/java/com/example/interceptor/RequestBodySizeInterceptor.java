package com.example.interceptor;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class RequestBodySizeInterceptor implements ReaderInterceptor {

    private static final Logger LOG = Logger.getLogger(RequestBodySizeInterceptor.class.getName());

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext ctx)
            throws IOException, WebApplicationException {

        LOG.info("[BODY IN] Content-Type: " + ctx.getMediaType());
        Object entity = ctx.proceed();
        LOG.info("[BODY IN] Entity type: " + (entity != null ? entity.getClass().getSimpleName() : "null"));
        return entity;
    }
}
