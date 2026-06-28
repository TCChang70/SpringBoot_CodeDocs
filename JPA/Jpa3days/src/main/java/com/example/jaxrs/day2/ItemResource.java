package com.example.jaxrs.day2;

import com.example.day2.dao.ItemDao;
import com.example.day2.dto.ItemSearchCriteria;
import com.example.day2.entity.Item;
import com.example.jaxrs.config.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/day2/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {

    private ItemDao itemDao = new ItemDao();

    @GET
    @JsonView(Views.List.class)
    public List<Item> getAll() {
        return itemDao.findAll();
    }

    @GET
    @Path("/count")
    public Long count() {
        return itemDao.countItems();
    }

    @GET
    @Path("/hints")
    @JsonView(Views.List.class)
    public List<Item> getWithHints() {
        return itemDao.findWithQueryHint();
    }

    @GET
    @Path("/expensive")
    @JsonView(Views.List.class)
    public List<Item> getExpensive(@QueryParam("top") @DefaultValue("10") int n) {
        return itemDao.findTopExpensive(n);
    }

    @GET
    @Path("/category/{id}")
    @JsonView(Views.List.class)
    public List<Item> getByCategory(@PathParam("id") Long id) {
        return itemDao.findByCategoryWithJoinFetch(id);
    }

    @GET
    @Path("/search")
    @JsonView(Views.List.class)
    public List<Item> search(@QueryParam("name") String name,
                             @QueryParam("minPrice") Double minPrice,
                             @QueryParam("maxPrice") Double maxPrice,
                             @QueryParam("categoryId") Long categoryId,
                             @QueryParam("active") Boolean active) {
        ItemSearchCriteria criteria = new ItemSearchCriteria();
        criteria.setName(name);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setCategoryId(categoryId);
        criteria.setActive(active);
        return itemDao.findByCriteria(criteria);
    }

    @GET
    @Path("/{id}")
    @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) {
        Item item = itemDao.findById(id);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Item not found\"}").build();
        }
        return Response.ok(item).build();
    }

    @POST
    @JsonView(Views.Detail.class)
    public Response create(Item item) {
        itemDao.create(item);
        return Response.status(Response.Status.CREATED).entity(item).build();
    }

    @PUT
    @JsonView(Views.Detail.class)
    public Response update(Item item) {
        itemDao.update(item);
        return Response.ok(item).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        itemDao.delete(id);
        return Response.ok("{\"message\":\"Item deleted\"}").build();
    }
}
