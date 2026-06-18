package com.example.exception;

import com.example.model.ErrorResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResourceNotFoundExceptionMapper
        implements ExceptionMapper<ResourceNotFoundException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ResourceNotFoundException ex) {
        ErrorResponse err = new ErrorResponse(
            404, "Not Found", ex.getMessage(),
            uriInfo.getRequestUri().getPath()
        );
        return Response.status(Response.Status.NOT_FOUND)
                       .entity(err)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
