package config;

import model.Employee;
import repository.EmployeeRepository;

import java.net.URI;
import java.util.Map;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeController {

    private final EmployeeRepository repo = new EmployeeRepository();

    // GET  /api/employees[/?dept=...&page=1&size=10]
    @GET
    public Response getAll(@QueryParam("dept") String dept,
                           @DefaultValue("1") @QueryParam("page") int page,
                           @DefaultValue("10") @QueryParam("size") int size) {
        if (dept != null) {
            return Response.ok(apiOk(repo.findByDepartment(dept))).build();
        }
        return Response.ok(apiOk(repo.findAllPaged(page, size))).build();
    }

    // GET  /api/employees/{id}
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        return repo.findById(id)
                .map(emp -> Response.ok(apiOk(emp)).build())
                .orElse(Response.status(404).entity(apiError("Not found: " + id)).build());
    }

    // POST /api/employees
    @POST
    public Response create(Employee emp, @Context UriInfo uriInfo) {
        if (emp.getName() == null || emp.getName().isBlank()) {
            return Response.status(400).entity(apiError("Name is required")).build();
        }
        Employee created = repo.save(emp);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(created.getId())).build();
        return Response.created(location).entity(apiOk(created)).build();
    }

    // PUT  /api/employees/{id}
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Employee emp) {
        emp.setId(id);
        Employee updated = repo.update(emp);
        return Response.ok(apiOk(updated)).build();
    }

    // DELETE /api/employees/{id}
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        repo.deleteById(id);
        return Response.noContent().build();
    }

    // ── 工具方法 ──
    private Map<String, Object> apiOk(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> apiError(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
