package com.example.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
public class HelloResource {

    @GET
    public Response sayHello() {
        return Response.ok(Map.of("message", "Hello, JAX-RS!")).build();
    }

    @GET
    @Path("/{name}")
    public Response sayHelloTo(@PathParam("name") String name) {
        return Response.ok(Map.of("message", "Hello, " + name + "!")).build();
    }
}
