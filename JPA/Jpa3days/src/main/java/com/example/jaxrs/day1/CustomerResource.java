package com.example.jaxrs.day1;

import com.example.day1.dao.CustomerDao;
import com.example.day1.entity.Customer;
import com.example.jaxrs.config.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/day1/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private CustomerDao customerDao = new CustomerDao();

    @GET
    @JsonView(Views.List.class)
    public List<Customer> getAll() {
        return customerDao.findAll();
    }

    @GET
    @Path("/id/{id}")
    @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) {
        Customer customer = customerDao.findById(id);
        if (customer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Customer not found\"}").build();
        }
        return Response.ok(customer).build();
    }

    @GET
    @Path("/email")
    @JsonView(Views.Detail.class)
    public Response getByEmail(@QueryParam("email") String email) {
        Customer customer = customerDao.findByEmail(email);
        if (customer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Customer not found\"}").build();
        }
        return Response.ok(customer).build();
    }

    @POST
    @JsonView(Views.Detail.class)
    public Response create(Customer customer) {
        customerDao.create(customer);
        return Response.status(Response.Status.CREATED).entity(customer).build();
    }

    @PUT
    @JsonView(Views.Detail.class)
    public Response update(Customer customer) {
        customerDao.update(customer);
        return Response.ok(customer).build();
    }

    @DELETE
    @Path("/id/{id}")
    public Response delete(@PathParam("id") Long id) {
        customerDao.delete(id);
        return Response.ok("{\"message\":\"Customer deleted\"}").build();
    }
}
