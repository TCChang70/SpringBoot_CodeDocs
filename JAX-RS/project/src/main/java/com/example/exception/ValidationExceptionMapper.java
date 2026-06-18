package com.example.exception;

import com.example.model.ErrorResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper
        implements ExceptionMapper<ValidationException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ValidationException ex) {
        ErrorResponse err = new ErrorResponse(
            400, "Bad Request", ex.getMessage(),
            uriInfo.getRequestUri().getPath()
        );
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(err)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
