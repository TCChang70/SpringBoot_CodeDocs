package com.example.resource;

import com.example.model.ApiResponse;
import com.example.model.Employee;
import com.example.model.EmployeeFilter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    private static final Map<Integer, Employee> DB = new LinkedHashMap<>();
    private static int nextId = 4;

    static {
        DB.put(1, new Employee(1, "Alice Chen", "Engineering", 85000));
        DB.put(2, new Employee(2, "Bob Wang", "Marketing", 72000));
        DB.put(3, new Employee(3, "Carol Liu", "Engineering", 90000));
    }

    // ── GET /api/employees ──────────────────────────────────────────
    @GET
    public Response getAll(@BeanParam EmployeeFilter filter) {
        List<Employee> list = new ArrayList<>(DB.values());

        // 部門篩選
        if (filter.getDepartment() != null && !filter.getDepartment().isBlank()) {
            list.removeIf(e -> !e.getDepartment().equalsIgnoreCase(filter.getDepartment()));
        }

        // 姓名篩選
        if (filter.getName() != null && !filter.getName().isBlank()) {
            list.removeIf(e -> !e.getName().toLowerCase().contains(filter.getName().toLowerCase()));
        }

        // 薪資範圍篩選
        if (filter.getMinSalary() != null) {
            list.removeIf(e -> e.getSalary() < filter.getMinSalary());
        }
        if (filter.getMaxSalary() != null) {
            list.removeIf(e -> e.getSalary() > filter.getMaxSalary());
        }

        int total = list.size();

        // 排序
        String sortField = (filter.getSort() != null) ? filter.getSort() : "id";
        boolean asc = filter.getOrder() == null || "asc".equalsIgnoreCase(filter.getOrder());

        Comparator<Employee> comparator = switch (sortField) {
            case "name" -> Comparator.comparing(Employee::getName);
            case "salary" -> Comparator.comparingDouble(Employee::getSalary);
            case "department" -> Comparator.comparing(Employee::getDepartment);
            default -> Comparator.comparingInt(Employee::getId);
        };
        if (!asc) {
            comparator = comparator.reversed();
        }
        list.sort(comparator);

        // 分頁
        int from = Math.min((filter.getPage() - 1) * filter.getSize(), list.size());
        int to = Math.min(from + filter.getSize(), list.size());
        List<Employee> paged = list.subList(from, to);

        return Response.ok(ApiResponse.ok(paged))
                .header("X-Total-Count", total)
                .build();
    }

    // ── GET /api/employees/{id} ─────────────────────────────────────
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Employee emp = DB.get(id);
        if (emp == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Employee not found: " + id))
                    .build();
        }
        return Response.ok(ApiResponse.ok(emp)).build();
    }

    // ── POST /api/employees ─────────────────────────────────────────
    @POST
    public Response create(Employee emp, @Context UriInfo uriInfo) {
        if (emp.getName() == null || emp.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Name is required"))
                    .build();
        }

        int id = nextId++;
        emp.setId(id);
        DB.put(id, emp);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(id))
                .build();

        return Response.created(location)
                .entity(ApiResponse.ok("Created", emp))
                .build();
    }

    // ── PUT /api/employees/{id} ─────────────────────────────────────
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Employee updated) {
        if (!DB.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Employee not found: " + id))
                    .build();
        }
        if (updated.getName() == null || updated.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Name is required"))
                    .build();
        }

        updated.setId(id);
        DB.put(id, updated);
        return Response.ok(ApiResponse.ok("Updated", updated)).build();
    }

    // ── PATCH /api/employees/{id} ───────────────────────────────────
    @PATCH
    @Path("/{id}")
    public Response partialUpdate(@PathParam("id") int id, Map<String, Object> fields) {
        Employee emp = DB.get(id);
        if (emp == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Employee not found: " + id))
                    .build();
        }

        fields.forEach((key, value) -> {
            switch (key) {
                case "name" -> emp.setName((String) value);
                case "department" -> emp.setDepartment((String) value);
                case "salary" -> emp.setSalary(((Number) value).doubleValue());
            }
        });

        return Response.ok(ApiResponse.ok("Updated", emp)).build();
    }

    // ── DELETE /api/employees/{id} ──────────────────────────────────
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        if (!DB.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Employee not found: " + id))
                    .build();
        }
        DB.remove(id);
        return Response.noContent().build();
    }
}
