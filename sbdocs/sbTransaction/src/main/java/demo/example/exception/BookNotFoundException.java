package demo.example.exception;


//繼承 RuntimeException：不需要在方法簽名宣告 throws，程式碼更簡潔
public class BookNotFoundException extends RuntimeException {

 public BookNotFoundException(Long id) {
     super("書籍不存在，id: " + id);
 }

 public BookNotFoundException(String message) {
     super(message);
 }
}
