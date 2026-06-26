package controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import model.*;
import model.Order.OrderItem;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderController {

    private static final Map<Integer, Order> DB = new ConcurrentHashMap<>();
    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    static {
        List<OrderItem> items = List.of(
            new OrderItem("iPhone 15", 2, 34900.0),
            new OrderItem("AirPods Pro", 1, 7990.0)
        );
        DB.put(1001, new Order(1001, items, LocalDateTime.now()));
    }

    @GET    
    public Response getAll() {
        List<Order> orders = new ArrayList<Order>(DB.values());
        if (orders == null || orders.size()==0) {
            return Response.noContent().build();
        }
        return Response.ok(orders).build();
       
    }
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Order order = DB.get(id);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(order).build();
        // 巢狀結構 Order → List<OrderItem>
        // Jackson 自動遞迴序列化所有層級
    }

    @POST
    public Response create(Order order, @Context UriInfo uriInfo) {
        // Jackson 自動從 JSON 反序列化巢狀結構
        // order.items 自動完成
        int id = 1000 + ID_GEN.incrementAndGet();
        Order created = new Order(id, order.getItems(), LocalDateTime.now());
        System.out.println("add order:"+created);
        DB.put(id, created);
        return Response.created(
            uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build()
        ).entity(created).build();
    }
}
