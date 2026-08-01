package controller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.*;
import java.util.*;

@Path("/books")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BookController {
	private final BookRepository repo = new BookRepository();

    @GET
    public Response getAll() {
    	    List<Book>  bks=repo.findAll();
        return Response.ok(Map.of("success", true, "data", bks)).build();
    }

    private Map<String, Object> ok(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
