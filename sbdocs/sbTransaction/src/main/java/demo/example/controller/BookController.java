package demo.example.controller;

import demo.example.model.BookCreateRequest;
import demo.example.model.BookResponse;
import demo.example.model.BookUpdateRequest;
import demo.example.model.Book;
import demo.example.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/books  或  GET /api/books?category=Programming
    @GetMapping
    public List<BookResponse> getAll(@RequestParam(required = false) String category) {
        if (category != null) {
            return bookService.findByCategory(category)
                    .stream().map(BookResponse::from).toList();
        }
        return bookService.findAll()
                .stream().map(BookResponse::from).toList();
    }

    // GET /api/books/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(BookResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/books
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookCreateRequest req) {
        Book book = new Book(
                req.getTitle(), req.getAuthor(), req.getIsbn(),
                req.getPrice(), req.getStock(), req.getCategory());
        Book saved = bookService.create(book);
        URI location = URI.create("/api/books/" + saved.getId());
        return ResponseEntity.created(location).body(BookResponse.from(saved));
    }

    // PUT /api/books/{id}
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest req) {
        Book updatedData = new Book(
                req.getTitle(), req.getAuthor(), req.getIsbn(),
                req.getPrice(), req.getStock(), req.getCategory());
        return bookService.update(id, updatedData)
                .map(BookResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/books/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (bookService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}