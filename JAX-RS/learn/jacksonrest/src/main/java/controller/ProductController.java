package controller;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import model.Product;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductController {

    private static final Map<Integer, Product> DB = new ConcurrentHashMap<>();
    private static final AtomicInteger ID_GEN = new AtomicInteger(3);

    static {
        DB.put(1, new Product(1, "MacBook Pro", 59900.0));
        DB.put(2, new Product(2, "iPhone 15", 34900.0));
        DB.put(3, new Product(3, "AirPods Pro", 7990.0));
    }

    // ── 自動序列化範例 ─────────────────────────────────
    // @GET + @Produces(JSON)
    // Java List<Product> → MessageBodyWriter → JSON String
    //───────────────────────────────────────────────────
    @GET
    public Response getAll() {
        List<Product> list = new ArrayList<>(DB.values());
        list.get(0).setRemark("remark test");
        // 不需要手動 mapper.writeValueAsString()
        // Jersey 自動呼叫 JacksonJsonWriter.writeTo()
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Product p = DB.get(id);
        if (p == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Collections.singletonMap("error", "Product not found"))
                    .build();
        }
        return Response.ok(p).build();
    }

    // ── 自動反序列化範例 ───────────────────────────────
    // @POST + @Consumes(JSON)
    // JSON String → MessageBodyReader → Java Product
    //───────────────────────────────────────────────────
    @POST
    public Response create(Product product, @Context UriInfo uriInfo) {
        // product 已由 Jackson 自動從 JSON Body 反序列化
        int id = ID_GEN.incrementAndGet();
        product.setId(id);
        DB.put(id, product);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(id)).build();
        return Response.created(location).entity(product).build();
    }

    // ── 巢狀物件序列化 ─────────────────────────────────
    // Map 結構同樣自動序列化
    //───────────────────────────────────────────────────
    @GET
    @Path("/stats")
    public Response getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", DB.size());
        stats.put("maxPrice", DB.values().stream()
                .mapToDouble(Product::getPrice).max().orElse(0));
        stats.put("avgPrice", DB.values().stream()
                .mapToDouble(Product::getPrice).average().orElse(0));
        stats.put("categories", List.of("Electronics", "Accessories"));
        // Map + List 混合結構自動序列化
        return Response.ok(stats).build();
    }

    // ── 自訂 Response 狀態碼 ──────────────────────────
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        if (DB.remove(id) == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Collections.singletonMap("error", "Product not found"))
                    .build();
        }
        return Response.noContent().build();
    }
}
