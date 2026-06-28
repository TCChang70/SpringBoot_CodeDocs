package com.example.jaxrs.day1;

import com.example.day1.dao.OrderDao;
import com.example.day1.entity.Order;
import com.example.jaxrs.config.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/day1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private OrderDao orderDao = new OrderDao();

    @GET
    @JsonView(Views.List.class)
    public List<Order> getAll() {
        return orderDao.findAll();
    }

    @GET
    @Path("/id/{id}")
    @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) {
        Order order = orderDao.findById(id);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Order not found\"}").build();
        }
        return Response.ok(order).build();
    }

    @GET
    @Path("/customer/{customerId}")
    @JsonView(Views.List.class)
    public List<Order> getByCustomer(@PathParam("customerId") Long customerId) {
        return orderDao.findByCustomerId(customerId);
    }

    @POST
    @JsonView(Views.Detail.class)
    public Response create(Order order) {
        orderDao.create(order);
        return Response.status(Response.Status.CREATED).entity(order).build();
    }

    @PUT
    @JsonView(Views.Detail.class)
    public Response update(Order order) {
        orderDao.update(order);
        return Response.ok(order).build();
    }

    @DELETE
    @Path("/id/{id}")
    public Response delete(@PathParam("id") Long id) {
        orderDao.delete(id);
        return Response.ok("{\"message\":\"Order deleted\"}").build();
    }
}
