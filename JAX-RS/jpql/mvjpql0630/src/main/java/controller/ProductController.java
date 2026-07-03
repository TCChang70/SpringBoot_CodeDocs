package controller;


import model.Product;
import repository.ProductRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductController {

    private final ProductRepository repo = new ProductRepository();

    @GET
    public Response getProducts(
            @QueryParam("keyword") String keyword,
            @QueryParam("minPrice") Double minPrice,
            @QueryParam("maxPrice") Double maxPrice) {

        List<Product> result;
        if (keyword != null && !keyword.isBlank()) {
            result = repo.searchByName(keyword);
        } else if (minPrice != null && maxPrice != null) {
            result = repo.findByPriceRange(minPrice, maxPrice);
        } else {
            result = repo.findActiveProducts();
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Product product = repo.findById(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(product).build();
    }

    @GET
    @Path("/low-stock")
    public Response getLowStock(@QueryParam("threshold") @DefaultValue("10") int threshold) {
        return Response.ok(repo.findLowStockProducts(threshold)).build();
    }
}
