package com.example.resource;

import com.example.annotation.Secured;
import com.example.annotation.Timed;
import com.example.dto.DeptSalaryStats;
import com.example.entity.Employee;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.ValidationException;
import com.example.repository.EmployeeRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    private final EmployeeRepository repo = new EmployeeRepository();

    @GET
    public Response getAll(
            @QueryParam("dept") String dept,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        List<Employee> list;
        long total;

        if (dept != null && !dept.isBlank()) {
            list = repo.findByDepartment(dept);
            total = list.size();
        } else {
            list = repo.findAllPaged(page, size);
            total = repo.count();
        }

        return Response.ok(list)
                       .header("X-Total-Count", total)
                       .build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Employee emp = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return Response.ok(emp).build();
    }

    @POST
    public Response create(Employee emp, @Context UriInfo uriInfo) {
        validateEmployee(emp);

        if (repo.existsByEmail(emp.getEmail())) {
            throw new ValidationException("email", "Email already exists: " + emp.getEmail());
        }

        try {
            Employee saved = repo.save(emp);
            URI location = uriInfo.getAbsolutePathBuilder()
                                  .path(String.valueOf(saved.getId()))
                                  .build();
            return Response.created(location).entity(saved).build();
        } catch (PersistenceException e) {
            throw new ValidationException("data", "Could not save employee: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Employee emp) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }
        validateEmployee(emp);
        emp.setId(id);
        Employee updated = repo.update(emp);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }
        repo.deleteById(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/salary")
    public Response findBySalaryRange(
            @QueryParam("min") Double min,
            @QueryParam("max") Double max,
            @QueryParam("sort") @DefaultValue("asc") String sort) {

        if (min != null && max != null && min > max) {
            throw new ValidationException("range", "min must not be greater than max");
        }
        List<Employee> list = repo.findBySalaryRange(min, max, sort);
        return Response.ok(list).build();
    }

    @GET
    @Path("/stats/department")
    public Response getDepartmentStats() {
        List<DeptSalaryStats> stats = repo.getDepartmentSalaryStats();
        return Response.ok(stats).build();
    }

    private void validateEmployee(Employee emp) {
        if (emp == null) {
            throw new ValidationException("body", "Request body must not be empty");
        }
        if (emp.getName() == null || emp.getName().isBlank()) {
            throw new ValidationException("name", "Name must not be empty");
        }
        if (emp.getEmail() == null || emp.getEmail().isBlank()) {
            throw new ValidationException("email", "Email must not be empty");
        }
        if (!emp.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException("email", "Invalid email format");
        }
    }
}
