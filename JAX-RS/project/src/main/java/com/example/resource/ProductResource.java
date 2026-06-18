package com.example.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    private static final Map<Integer, Map<String, Object>> DB = new LinkedHashMap<>();

    static {
        DB.put(1, Map.of("id", 1, "name", "Laptop", "price", 999.99, "stock", 10));
        DB.put(2, Map.of("id", 2, "name", "Mouse", "price", 25.50, "stock", 100));
        DB.put(3, Map.of("id", 3, "name", "Keyboard", "price", 89.99, "stock", 50));
        DB.put(4, Map.of("id", 4, "name", "Monitor", "price", 299.99, "stock", 20));
    }

    @GET
    public Response getAll() {
        return Response.ok(DB.values()).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Map<String, Object> product = DB.get(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(Map.of("message", "Product not found: " + id))
                           .build();
        }
        return Response.ok(product).build();
    }

    @GET
    @Path("/search")
    public Response search(
            @QueryParam("minPrice") Double minPrice,
            @QueryParam("maxPrice") Double maxPrice) {

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(Map.of("message", "minPrice must not be greater than maxPrice"))
                           .build();
        }

        var results = DB.values().stream()
            .filter(p -> minPrice == null || (double) p.get("price") >= minPrice)
            .filter(p -> maxPrice == null || (double) p.get("price") <= maxPrice)
            .collect(Collectors.toList());

        if (results.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(results).build();
    }
}
