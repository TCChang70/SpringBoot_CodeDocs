package com.example.exception;

import com.example.model.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable ex) {
        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }

        LOG.log(Level.SEVERE, "Unhandled exception", ex);

        ErrorResponse err = new ErrorResponse(
            500, "Internal Server Error",
            "An unexpected error occurred",
            uriInfo.getRequestUri().getPath()
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(err)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
