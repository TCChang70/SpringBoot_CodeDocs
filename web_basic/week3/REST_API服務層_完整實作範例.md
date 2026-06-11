# REST API 服務層 - 完整實作範例

## 🌐 PublisherService.java - 出版商 REST API（完整版）

```java
package service;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import model.*;
import java.math.BigDecimal;

/**
 * 出版商 REST API 服務
 * 提供完整的 CRUD 操作和進階查詢功能
 */
@Path("/publishers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublisherService {
    
    private PublisherDAO publisherDAO = new PublisherDAO();
    
    /**
     * 取得所有出版商（基本資訊）
     * GET /publishers
     */
    @GET
    public Response getAllPublishers(@QueryParam("country") String country,
                                   @QueryParam("name") String name,
                                   @QueryParam("page") @DefaultValue("0") int page,
                                   @QueryParam("size") @DefaultValue("10") int size) {
        try {
            List<Publisher> publishers;
            
            // 根據查詢參數選擇適當的查詢方法
            if (country != null && !country.trim().isEmpty()) {
                publishers = publisherDAO.findByCountry(country);
            } else if (name != null && !name.trim().isEmpty()) {
                publishers = publisherDAO.findByName(name);
            } else if (page >= 0 && size > 0) {
                publishers = publisherDAO.getPublishersWithPagination(page, size);
            } else {
                publishers = publisherDAO.getAll();
            }
            
            if (publishers.isEmpty()) {
                return Response
                    .status(Response.Status.NO_CONTENT)
                    .entity(createMessage("查無出版商資料"))
                    .build();
            }
            
            // 添加分頁資訊到回應標頭
            if (page >= 0 && size > 0) {
                long totalCount = publisherDAO.getTotalPublisherCount();
                return Response
                    .status(Response.Status.OK)
                    .entity(publishers)
                    .header("X-Total-Count", totalCount)
                    .header("X-Page-Number", page)
                    .header("X-Page-Size", size)
                    .build();
            }
            
            return Response
                .status(Response.Status.OK)
                .entity(publishers)
                .build();
                
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("查詢出版商時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 取得所有出版商（包含書籍資訊）
     * GET /publishers/with-books
     */
    @GET
    @Path("/with-books")
    public Response getAllPublishersWithBooks() {
        try {
            List<Publisher> publishers = publisherDAO.getAllWithBooks();
            
            if (publishers.isEmpty()) {
                return Response
                    .status(Response.Status.NO_CONTENT)
                    .entity(createMessage("查無出版商資料"))
                    .build();
            }
            
            return Response
                .status(Response.Status.OK)
                .entity(publishers)
                .build();
                
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("查詢出版商和書籍時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 根據ID取得特定出版商
     * GET /publishers/{id}
     */
    @GET
    @Path("/{id}")
    public Response getPublisherById(@PathParam("id") String publisherId,
                                   @QueryParam("include-books") @DefaultValue("false") boolean includeBooks) {
        try {
            if (publisherId == null || publisherId.trim().isEmpty()) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createMessage("出版商ID不可為空"))
                    .build();
            }
            
            Publisher publisher;
            if (includeBooks) {
                publisher = publisherDAO.findByIdWithBooks(publisherId);
            } else {
                publisher = publisherDAO.findById(publisherId);
            }
            
            if (publisher == null) {
                return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(createMessage("找不到出版商: " + publisherId))
                    .build();
            }
            
            return Response
                .status(Response.Status.OK)
                .entity(publisher)
                .build();
                
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("查詢出版商時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 新增出版商
     * POST /publishers
     */
    @POST
    public Response addPublisher(Publisher publisher) {
        try {
            // 輸入驗證
            String validationError = validatePublisher(publisher);
            if (validationError != null) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createMessage(validationError))
                    .build();
            }
            
            // 檢查出版商ID是否已存在
            Publisher existing = publisherDAO.findById(publisher.getPublisherId());
            if (existing != null) {
                return Response
                    .status(Response.Status.CONFLICT)
                    .entity(createMessage("出版商ID已存在: " + publisher.getPublisherId()))
                    .build();
            }
            
            boolean success = publisherDAO.addPublisher(publisher);
            if (success) {
                return Response
                    .status(Response.Status.CREATED)
                    .entity(publisher)
                    .location(UriBuilder.fromPath("/publishers/{id}")
                             .build(publisher.getPublisherId()))
                    .build();
            } else {
                return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createMessage("新增出版商失敗"))
                    .build();
            }
            
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("新增出版商時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 同時新增出版商和書籍
     * POST /publishers/with-books
     */
    @POST
    @Path("/with-books")
    public Response addPublisherWithBooks(Publisher publisher) {
        try {
            // 輸入驗證
            String validationError = validatePublisher(publisher);
            if (validationError != null) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createMessage(validationError))
                    .build();
            }
            
            // 驗證書籍資料
            List<Book> books = publisher.getBooks();
            if (books != null) {
                for (Book book : books) {
                    String bookValidationError = validateBook(book);
                    if (bookValidationError != null) {
                        return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(createMessage("書籍資料錯誤: " + bookValidationError))
                            .build();
                    }
                }
            }
            
            // 檢查出版商ID是否已存在
            Publisher existing = publisherDAO.findById(publisher.getPublisherId());
            if (existing != null) {
                return Response
                    .status(Response.Status.CONFLICT)
                    .entity(createMessage("出版商ID已存在: " + publisher.getPublisherId()))
                    .build();
            }
            
            // 清除可能的循環參照
            if (books != null) {
                books.forEach(book -> book.setPublisher(null));
            }
            
            boolean success = publisherDAO.addPublisherWithBooks(publisher);
            if (success) {
                // 重新查詢以取得完整資料（包含自動生成的書籍ID）
                Publisher created = publisherDAO.findByIdWithBooks(publisher.getPublisherId());
                
                return Response
                    .status(Response.Status.CREATED)
                    .entity(created)
                    .location(UriBuilder.fromPath("/publishers/{id}")
                             .build(publisher.getPublisherId()))
                    .build();
            } else {
                return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createMessage("新增出版商和書籍失敗"))
                    .build();
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("新增出版商和書籍時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 更新出版商資訊
     * PUT /publishers/{id}
     */
    @PUT
    @Path("/{id}")
    public Response updatePublisher(@PathParam("id") String publisherId, Publisher publisher) {
        try {
            if (publisherId == null || publisherId.trim().isEmpty()) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createMessage("出版商ID不可為空"))
                    .build();
            }
            
            // 確保路徑參數與實體ID一致
            publisher.setPublisherId(publisherId);
            
            // 輸入驗證
            String validationError = validatePublisher(publisher);
            if (validationError != null) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createMessage(validationError))
                    .build();
            }
            
            // 檢查出版商是否存在
            Publisher existing = publisherDAO.findById(publisherId);
            if (existing == null) {
                return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(createMessage("找不到出版商: " + publisherId))
                    .build();
            }
            
            boolean success = publisherDAO.updatePublisher(publisher);
            if (success) {
                return Response
                    .status(Response.Status.OK)
                    .entity(publisher)
                    .build();
            } else {
                return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createMessage("更新出版商失敗"))
                    .build();
            }
            
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("更新出版商時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 刪除出版商
     * DELETE /publishers/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deletePublisher(@PathParam("id") String publisherId,
                                  @QueryParam("force") @DefaultValue("false") boolean force) {
        try {
            if (publisherId == null || publisherId.trim().isEmpty()) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createMessage("出版商ID不可為空"))
                    .build();
            }
            
            // 檢查出版商是否存在
            Publisher existing = publisherDAO.findByIdWithBooks(publisherId);
            if (existing == null) {
                return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(createMessage("找不到出版商: " + publisherId))
                    .build();
            }
            
            // 檢查是否有相關書籍
            if (!force && existing.getBooks() != null && !existing.getBooks().isEmpty()) {
                return Response
                    .status(Response.Status.CONFLICT)
                    .entity(createMessage("出版商還有 " + existing.getBooks().size() + 
                                        " 本書籍，請先處理書籍或使用 force=true 強制刪除"))
                    .build();
            }
            
            boolean success = publisherDAO.deletePublisher(publisherId);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "成功刪除出版商: " + existing.getPublisherName());
                response.put("deletedPublisher", existing);
                
                return Response
                    .status(Response.Status.OK)
                    .entity(response)
                    .build();
            } else {
                return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createMessage("刪除出版商失敗"))
                    .build();
            }
            
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("刪除出版商時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 取得特定出版商的所有書籍
     * GET /publishers/{id}/books
     */
    @GET
    @Path("/{id}/books")
    public Response getBooksByPublisher(@PathParam("id") String publisherId,
                                      @QueryParam("category") String category,
                                      @QueryParam("minPrice") BigDecimal minPrice,
                                      @QueryParam("maxPrice") BigDecimal maxPrice) {
        try {
            if (publisherId == null || publisherId.trim().isEmpty()) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createMessage("出版商ID不可為空"))
                    .build();
            }
            
            // 檢查出版商是否存在
            Publisher publisher = publisherDAO.findById(publisherId);
            if (publisher == null) {
                return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(createMessage("找不到出版商: " + publisherId))
                    .build();
            }
            
            List<Book> books = publisherDAO.getBooksByPublisher(publisherId);
            
            // 根據查詢參數過濾書籍
            if (category != null && !category.trim().isEmpty()) {
                books = books.stream()
                    .filter(book -> category.equals(book.getCategory()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (minPrice != null || maxPrice != null) {
                books = books.stream()
                    .filter(book -> {
                        BigDecimal price = book.getPrice();
                        if (price == null) return false;
                        if (minPrice != null && price.compareTo(minPrice) < 0) return false;
                        if (maxPrice != null && price.compareTo(maxPrice) > 0) return false;
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (books.isEmpty()) {
                return Response
                    .status(Response.Status.NO_CONTENT)
                    .entity(createMessage("該出版商暫無書籍"))
                    .build();
            }
            
            return Response
                .status(Response.Status.OK)
                .entity(books)
                .header("X-Publisher-Name", publisher.getPublisherName())
                .header("X-Book-Count", books.size())
                .build();
                
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("查詢出版商書籍時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 取得出版商統計資訊
     * GET /publishers/statistics
     */
    @GET
    @Path("/statistics")
    public Response getPublisherStatistics() {
        try {
            List<Object[]> statistics = publisherDAO.getPublisherStatistics();
            
            if (statistics.isEmpty()) {
                return Response
                    .status(Response.Status.NO_CONTENT)
                    .entity(createMessage("查無統計資料"))
                    .build();
            }
            
            // 轉換統計資料為更友善的格式
            List<Map<String, Object>> formattedStats = new ArrayList<>();
            for (Object[] stat : statistics) {
                Map<String, Object> statMap = new HashMap<>();
                statMap.put("publisherName", stat[0]);
                statMap.put("bookCount", stat[1]);
                statMap.put("totalValue", stat[2]);
                formattedStats.add(statMap);
            }
            
            return Response
                .status(Response.Status.OK)
                .entity(formattedStats)
                .build();
                
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("查詢統計資料時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    // === 輔助方法 ===
    
    /**
     * 驗證出版商資料
     */
    private String validatePublisher(Publisher publisher) {
        if (publisher == null) {
            return "出版商資料不可為空";
        }
        
        if (publisher.getPublisherId() == null || publisher.getPublisherId().trim().isEmpty()) {
            return "出版商ID不可為空";
        }
        
        if (publisher.getPublisherId().length() > 10) {
            return "出版商ID長度不可超過10個字元";
        }
        
        if (publisher.getPublisherName() == null || publisher.getPublisherName().trim().isEmpty()) {
            return "出版商名稱不可為空";
        }
        
        if (publisher.getPublisherName().length() > 100) {
            return "出版商名稱長度不可超過100個字元";
        }
        
        if (publisher.getEmail() != null && !isValidEmail(publisher.getEmail())) {
            return "電子郵件格式不正確";
        }
        
        return null; // 驗證通過
    }
    
    /**
     * 驗證書籍資料
     */
    private String validateBook(Book book) {
        if (book == null) {
            return "書籍資料不可為空";
        }
        
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            return "書名不可為空";
        }
        
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            return "作者不可為空";
        }
        
        if (book.getPrice() != null && book.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return "書籍價格不可為負數";
        }
        
        return null; // 驗證通過
    }
    
    /**
     * 驗證電子郵件格式
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        return email.matches(emailRegex);
    }
    
    /**
     * 建立回應訊息
     */
    private Map<String, String> createMessage(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }
}
```

## 📚 BookService.java - 書籍 REST API

```java
package service;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import model.*;
import java.math.BigDecimal;

/**
 * 書籍 REST API 服務
 */
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookService {
    
    private BookDAO bookDAO = new BookDAO();
    private PublisherDAO publisherDAO = new PublisherDAO();
    
    /**
     * 取得所有書籍
     * GET /books
     */
    @GET
    public Response getAllBooks(@QueryParam("category") String category,
                               @QueryParam("author") String author,
                               @QueryParam("minPrice") BigDecimal minPrice,
                               @QueryParam("maxPrice") BigDecimal maxPrice) {
        try {
            List<Book> books;
            
            if (category != null && !category.trim().isEmpty()) {
                books = bookDAO.findByCategory(category);
            } else if (minPrice != null || maxPrice != null) {
                BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
                BigDecimal max = maxPrice != null ? maxPrice : new BigDecimal("999999");
                books = bookDAO.findByPriceRange(min, max);
            } else {
                books = bookDAO.getAll();
            }
            
            // 進一步依作者過濾
            if (author != null && !author.trim().isEmpty()) {
                books = books.stream()
                    .filter(book -> book.getAuthor().toLowerCase()
                                       .contains(author.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (books.isEmpty()) {
                return Response
                    .status(Response.Status.NO_CONTENT)
                    .entity(createMessage("查無書籍資料"))
                    .build();
            }
            
            return Response
                .status(Response.Status.OK)
                .entity(books)
                .build();
                
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("查詢書籍時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 根據ID取得特定書籍
     * GET /books/{id}
     */
    @GET
    @Path("/{id}")
    public Response getBookById(@PathParam("id") int bookId) {
        try {
            Book book = bookDAO.findById(bookId);
            
            if (book == null) {
                return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(createMessage("找不到書籍: " + bookId))
                    .build();
            }
            
            return Response
                .status(Response.Status.OK)
                .entity(book)
                .build();
                
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("查詢書籍時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 根據ISBN查詢書籍
     * GET /books/isbn/{isbn}
     */
    @GET
    @Path("/isbn/{isbn}")
    public Response getBookByISBN(@PathParam("isbn") String isbn) {
        try {
            Book book = bookDAO.findByISBN(isbn);
            
            if (book == null) {
                return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(createMessage("找不到ISBN為 " + isbn + " 的書籍"))
                    .build();
            }
            
            return Response
                .status(Response.Status.OK)
                .entity(book)
                .build();
                
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("查詢書籍時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    /**
     * 新增書籍
     * POST /books
     */
    @POST
    public Response addBook(Book book, @QueryParam("publisherId") String publisherId) {
        try {
            // 輸入驗證
            String validationError = validateBook(book);
            if (validationError != null) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createMessage(validationError))
                    .build();
            }
            
            // 檢查ISBN是否已存在
            if (book.getIsbn() != null) {
                Book existing = bookDAO.findByISBN(book.getIsbn());
                if (existing != null) {
                    return Response
                        .status(Response.Status.CONFLICT)
                        .entity(createMessage("ISBN已存在: " + book.getIsbn()))
                        .build();
                }
            }
            
            // 設定出版商
            if (publisherId != null && !publisherId.trim().isEmpty()) {
                Publisher publisher = publisherDAO.findById(publisherId);
                if (publisher == null) {
                    return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(createMessage("找不到出版商: " + publisherId))
                        .build();
                }
                book.setPublisher(publisher);
            } else if (book.getPublisher() == null) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createMessage("必須指定出版商"))
                    .build();
            }
            
            boolean success = bookDAO.addBook(book);
            if (success) {
                return Response
                    .status(Response.Status.CREATED)
                    .entity(book)
                    .build();
            } else {
                return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createMessage("新增書籍失敗"))
                    .build();
            }
            
        } catch (Exception ex) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createMessage("新增書籍時發生錯誤: " + ex.getMessage()))
                .build();
        }
    }
    
    // === 輔助方法 ===
    
    private String validateBook(Book book) {
        if (book == null) {
            return "書籍資料不可為空";
        }
        
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            return "書名不可為空";
        }
        
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            return "作者不可為空";
        }
        
        if (book.getPrice() != null && book.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return "書籍價格不可為負數";
        }
        
        return null;
    }
    
    private Map<String, String> createMessage(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }
}
```

## 🔧 進階功能實作範例

### 1. 異常處理類別

```java
package exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    
    @Override
    public Response toResponse(Exception exception) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
        
        // 根據異常類型返回適當的狀態碼
        if (exception instanceof IllegalArgumentException) {
            errorResponse.put("status", 400);
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(errorResponse)
                          .build();
        }
        
        errorResponse.put("status", 500);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                      .entity(errorResponse)
                      .build();
    }
}
```

### 2. CORS 支援

```java
package filter;

import javax.ws.rs.container.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CORSFilter implements ContainerResponseFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) throws IOException {
        
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "origin, content-type, accept, authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
```

### 3. 資料傳輸物件 (DTO)

```java
package dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 出版商統計 DTO
 */
public class PublisherStatisticsDTO {
    private String publisherId;
    private String publisherName;
    private long bookCount;
    private BigDecimal totalValue;
    private LocalDate lastPublishDate;
    
    // 建構子、Getter 和 Setter
    public PublisherStatisticsDTO() {}
    
    public PublisherStatisticsDTO(String publisherId, String publisherName, 
                                 long bookCount, BigDecimal totalValue) {
        this.publisherId = publisherId;
        this.publisherName = publisherName;
        this.bookCount = bookCount;
        this.totalValue = totalValue;
    }
    
    // Getter 和 Setter 方法...
}

/**
 * 書籍摘要 DTO
 */
public class BookSummaryDTO {
    private int bookId;
    private String title;
    private String author;
    private BigDecimal price;
    private String publisherName;
    private boolean inStock;
    
    // 建構子、Getter 和 Setter
    public BookSummaryDTO() {}
    
    public BookSummaryDTO(int bookId, String title, String author, 
                         BigDecimal price, String publisherName, boolean inStock) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.publisherName = publisherName;
        this.inStock = inStock;
    }
    
    // Getter 和 Setter 方法...
}
```

## 📝 測試用的 JSON 範例

### 新增出版商範例：
```json
{
    "publisherId": "PUB003",
    "publisherName": "碁峰資訊",
    "address": "台北市中正區博愛路76號6樓",
    "city": "台北",
    "country": "台灣",
    "phone": "02-2311-2666",
    "email": "service@gotop.com.tw",
    "website": "https://www.gotop.com.tw"
}
```

### 同時新增出版商和書籍範例：
```json
{
    "publisherId": "PUB004",
    "publisherName": "松崗出版",
    "address": "台北市中正區重慶南路一段100號",
    "city": "台北",
    "country": "台灣",
    "phone": "02-2371-1111",
    "email": "info@unalis.com.tw",
    "website": "https://www.unalis.com.tw",
    "books": [
        {
            "title": "深入淺出 Java 設計模式",
            "author": "陳會安",
            "isbn": "978-957-22-1234-5",
            "price": 580.00,
            "publishDate": "2024-02-15",
            "category": "程式設計",
            "pageCount": 450,
            "description": "完整介紹 Java 設計模式的實務應用",
            "stockQuantity": 100
        },
        {
            "title": "Spring Boot 微服務實戰",
            "author": "王小明",
            "isbn": "978-957-22-5678-9",
            "price": 620.00,
            "publishDate": "2024-04-20",
            "category": "微服務",
            "pageCount": 520,
            "description": "從零開始學習 Spring Boot 微服務架構",
            "stockQuantity": 80
        }
    ]
}
```

這些完整的實作範例展示了：

1. **完整的 REST API 設計** - 包含所有 HTTP 方法和狀態碼處理
2. **詳細的輸入驗證** - 確保資料完整性和正確性
3. **錯誤處理機制** - 適當的異常處理和錯誤回應
4. **進階查詢功能** - 支援分頁、過濾、排序等功能
5. **業務邏輯處理** - 複雜的關聯操作和統計功能
6. **CORS 和安全性** - 跨域請求支援和基本安全措施
7. **DTO 模式** - 分離資料傳輸和業務邏輯

這套完整的實作可以作為學習 JPA 一對多關聯和 REST API 開發的優秀範例！