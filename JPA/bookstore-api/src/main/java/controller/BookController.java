package controller;

import entity.Book;
import repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Tag(name = "書籍", description = "書籍 CRUD 操作")
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookController {

    private final BookRepository repo = new BookRepository();

    // ==================== CREATE ====================

    /** POST /api/books — 新增書籍 */
    @POST
    @Operation(
        summary = "新增書籍",
        description = "建立一本新書。id、createdAt、updatedAt 由系統自動產生，不需傳入。"
    )
    @ApiResponse(responseCode = "201", description = "新增成功，回傳書籍資料（含自動產生的 id 與時間）",
        content = @Content(schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "資料錯誤（例如缺少必填欄位），新增失敗")
    public Response create(
        @Parameter(description = "書籍資料（Book JSON 物件）", required = true, schema = @Schema(implementation = Book.class))
        Book book) {
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
    @Operation(
        summary = "查詢書籍清單",
        description = "查詢全部書籍。依參數優先順序：有 category 依分類篩選；有 minPrice/maxPrice 依價格區間篩選；"
            + "都沒有則回傳全部並分頁（page 從 1 開始）。"
    )
    @ApiResponse(responseCode = "200", description = "查詢成功，data 為書籍陣列",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Book.class))))
    public Response getAll(
        @Parameter(in = ParameterIn.QUERY, description = "分類名稱（不分大小寫）", example = "小說")
        @QueryParam("category") String category,
        @Parameter(in = ParameterIn.QUERY, description = "最低價格", example = "100")
        @QueryParam("minPrice")  Double minPrice,
        @Parameter(in = ParameterIn.QUERY, description = "最高價格", example = "300")
        @QueryParam("maxPrice")  Double maxPrice,
        @Parameter(in = ParameterIn.QUERY, description = "頁碼（從 1 開始）", example = "1")
        @DefaultValue("1")  @QueryParam("page") int page,
        @Parameter(in = ParameterIn.QUERY, description = "每頁筆數", example = "10")
        @DefaultValue("10") @QueryParam("size") int size
    ) {
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
    @Operation(
        summary = "依 id 查詢單本書籍",
        description = "以路徑中的 id 查詢書籍；找不到回傳 404。"
    )
    @ApiResponse(responseCode = "200", description = "查詢成功，data 為書籍物件",
        content = @Content(schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "404", description = "書籍不存在")
    public Response getById(
        @Parameter(in = ParameterIn.PATH, description = "書籍 id", required = true, example = "1")
        @PathParam("id") Long id) {
        return repo.findById(id)
            .map(book -> Response.ok(ok(book)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(fail("書籍不存在")).build());
    }

    // ==================== UPDATE ====================

    /** PUT /api/books/{id} — 更新書籍 */
    @PUT
    @Path("/{id}")
    @Operation(
        summary = "更新書籍",
        description = "以路徑中的 id 更新書籍（整筆覆蓋）。建議先 GET 原資料再修改後整筆送出，未提供的欄位會被設為 null。"
    )
    @ApiResponse(responseCode = "200", description = "更新成功，回傳更新後的書籍資料",
        content = @Content(schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "資料錯誤，更新失敗")
    @ApiResponse(responseCode = "404", description = "書籍不存在")
    public Response update(
        @Parameter(in = ParameterIn.PATH, description = "書籍 id", required = true, example = "1")
        @PathParam("id") Long id,
        @Parameter(description = "要更新的書籍資料（Book JSON 物件）", required = true, schema = @Schema(implementation = Book.class))
        Book book) {
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
    @Operation(
        summary = "刪除書籍",
        description = "以路徑中的 id 刪除書籍；找不到回傳 404。"
    )
    @ApiResponse(responseCode = "200", description = "刪除成功，data 為文字訊息")
    @ApiResponse(responseCode = "404", description = "書籍不存在")
    public Response delete(
        @Parameter(in = ParameterIn.PATH, description = "書籍 id", required = true, example = "1")
        @PathParam("id") Long id) {
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
