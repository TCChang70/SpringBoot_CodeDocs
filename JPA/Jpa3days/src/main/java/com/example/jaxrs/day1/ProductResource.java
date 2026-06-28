package com.example.jaxrs.day1;

import com.example.day1.dao.ProductDao;
import com.example.day1.entity.Product;
import com.example.jaxrs.config.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/day1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private ProductDao productDao = new ProductDao();

    @GET
    @JsonView(Views.List.class)
    public List<Product> getAll() {
        return productDao.findAll();
    }

    @GET
    @Path("/id/{id}")
    @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) {
        Product product = productDao.findById(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Product not found\"}").build();
        }
        return Response.ok(product).build();
    }

    @GET
    @Path("/search")
    @JsonView(Views.List.class)
    public List<Product> search(@QueryParam("name") String name) {
        return productDao.findByNameContaining(name);
    }

    @POST
    @JsonView(Views.Detail.class)
    public Response create(Product product) {
        productDao.create(product);
        return Response.status(Response.Status.CREATED).entity(product).build();
    }

    @PUT
    @JsonView(Views.Detail.class)
    public Response update(Product product) {
        productDao.update(product);
        return Response.ok(product).build();
    }

    @DELETE
    @Path("/id/{id}")
    public Response delete(@PathParam("id") Long id) {
        productDao.delete(id);
        return Response.ok("{\"message\":\"Product deleted\"}").build();
    }
}
