package com.example.jaxrs.day2;

import com.example.day2.dao.CategoryDao;
import com.example.day2.entity.Category;
import com.example.jaxrs.config.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/day2/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private CategoryDao categoryDao = new CategoryDao();

    @GET
    @JsonView(Views.List.class)
    public List<Category> getAll() {
        return categoryDao.findAll();
    }

    @GET
    @Path("/{id}")
    @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) {
        Category category = categoryDao.findById(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Category not found\"}").build();
        }
        return Response.ok(category).build();
    }

    @POST
    @JsonView(Views.Detail.class)
    public Response create(Category category) {
        categoryDao.create(category);
        return Response.status(Response.Status.CREATED).entity(category).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        categoryDao.delete(id);
        return Response.ok("{\"message\":\"Category deleted\"}").build();
    }
}
