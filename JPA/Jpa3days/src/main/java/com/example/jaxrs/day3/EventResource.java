package com.example.jaxrs.day3;

import com.example.day3.dao.EventDao;
import com.example.day3.entity.Event;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/day3/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {

    private EventDao eventDao = new EventDao();

    @POST
    public Response create(Event event) {
        eventDao.create(event);
        return Response.status(Response.Status.CREATED).entity(event).build();
    }

    @GET
    public List<Event> getAll() {
        return eventDao.findAll();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Event event = eventDao.findById(id);
        if (event == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Event not found\"}").build();
        }
        return Response.ok(event).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        eventDao.delete(id);
        return Response.noContent().build();
    }
}
