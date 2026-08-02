package controller;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.*;
import java.util.*;

import org.hibernate.annotations.Parameter;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookController {

    private final BookDAO repo = new BookDAO();

    // ==================== CREATE ====================

    /** POST /api/books — 新增書籍 */
    @POST
    public Response create(Book book) {
        try {
            Book saved = repo.save(book);
            return Response.status(Response.Status.CREATED)
                           .entity(ok(saved)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(fail("新增失敗：" + e.getMessage())).build();
        }
    }

    // ==================== READ ====================

    /**
     * GET /api/books — 查詢全部（支援篩選與分頁）
     *
     * 查詢參數：
     *   category   — 分類名稱
     *   minPrice   — 最低價格
     *   maxPrice   — 最高價格
     *   page       — 頁碼（預設 1）
     *   size       — 每頁筆數（預設 10）
     */
    @GET
    public Response getAll(        
        @QueryParam("category") String category,       
        @QueryParam("minPrice")  Double minPrice,      
        @QueryParam("maxPrice")  Double maxPrice,        
        @DefaultValue("1")  @QueryParam("page") int page,       
        @DefaultValue("10") @QueryParam("size") int size ) {
        Object data;
        if (category != null) {
            data = repo.findByCategory(category);
        } else if (minPrice != null || maxPrice != null) {
            double lo = (minPrice != null) ? minPrice : 0;
            double hi = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;
            data = repo.findByPriceRange(lo, hi);
        } else {
            data = repo.findAllPaged(page, size);
        }
        return Response.ok(ok(data)).build();
    }

    /** GET /api/books/{id} — 查詢單筆 */
    @GET
    @Path("/{id}")  
    public Response getById( @PathParam("id") Long id) {
        return repo.findById(id)
            .map(book -> Response.ok(ok(book)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(fail("書籍不存在")).build());
    }

    // ==================== UPDATE ====================

    /** PUT /api/books/{id} — 更新書籍 */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Book book) {
        if (!repo.existsById(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(fail("書籍不存在")).build();
        }
        book.setId(id);
        try {
            Book updated = repo.update(book);
            return Response.ok(ok(updated)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(fail("更新失敗：" + e.getMessage())).build();
        }
    }

    // ==================== DELETE ====================

    /** DELETE /api/books/{id} — 刪除書籍 */
    @DELETE
    @Path("/{id}")   
    public Response delete( @PathParam("id") Long id) {
        if (!repo.existsById(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(fail("書籍不存在")).build();
        }
        repo.deleteById(id);
        return Response.ok(ok("已刪除")).build();
    }

    // ==================== 工具方法 ====================

    private Map<String, Object> ok(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
