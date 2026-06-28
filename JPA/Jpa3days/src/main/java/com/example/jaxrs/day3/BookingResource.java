package com.example.jaxrs.day3;

import com.example.day3.dao.TicketDao;
import com.example.day3.entity.Ticket;
import com.example.jaxrs.config.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/day3/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    private TicketDao ticketDao = new TicketDao();

    @POST
    @Path("/book")
    public Response book(Map<String, Object> body) {
        if (body == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid JSON body\"}").build();
        }
        long eventId = ((Number) body.get("eventId")).longValue();
        String buyerName = (String) body.get("buyerName");
        int quantity = ((Number) body.get("quantity")).intValue();
        String result = ticketDao.bookTicket(eventId, buyerName, quantity);
        if (result.contains("\"error\"")) {
            int status = result.contains("not found") ? 404 : 409;
            return Response.status(status).entity(result).build();
        }
        return Response.ok(result).build();
    }

    @GET
    @JsonView(Views.List.class)
    public List<Ticket> getAll() {
        return ticketDao.findAll();
    }

    @GET
    @Path("/{id}")
    @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) {
        Ticket ticket = ticketDao.findById(id);
        if (ticket == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Ticket not found\"}").build();
        }
        return Response.ok(ticket).build();
    }

    @GET
    @Path("/event/{eventId}")
    @JsonView(Views.List.class)
    public List<Ticket> getByEvent(@PathParam("eventId") Long eventId) {
        return ticketDao.findByEventId(eventId);
    }

    @GET
    @Path("/concurrency-test")
    public String concurrencyTest() {
        return runConcurrencyTest();
    }

    private String runConcurrencyTest() {
        int threadCount = 5;
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failCount = new java.util.concurrent.atomic.AtomicInteger(0);
        StringBuilder details = new StringBuilder("[");

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    String buyerName = "ConcurrentUser-" + threadId;
                    String result = ticketDao.bookTicket(1L, buyerName, 1);
                    synchronized (details) {
                        if (details.length() > 1) details.append(",");
                        details.append("{\"thread\":").append(threadId)
                               .append(",\"result\":").append(result).append("}");
                    }
                    if (result.contains("\"success\":true")) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    synchronized (details) {
                        if (details.length() > 1) details.append(",");
                        details.append("{\"thread\":").append(threadId)
                               .append(",\"result\":\"error:").append(e.getMessage()).append("\"}");
                    }
                    failCount.incrementAndGet();
                }
            });
        }
        latch.countDown();
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.yield();
        }
        details.append("]");
        return "{\"totalThreads\":" + threadCount + ",\"success\":" + successCount.get()
                + ",\"failed\":" + failCount.get() + ",\"details\":" + details + "}";
    }
}
