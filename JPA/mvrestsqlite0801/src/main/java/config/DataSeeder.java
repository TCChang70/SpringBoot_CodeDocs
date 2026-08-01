package config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import model.Book;

import java.time.LocalDate;
import java.util.List;

@WebListener
public class DataSeeder implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		EntityManager em = JpaUtil.createEntityManager();
		try {
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			for (Book seed : seeds()) {
				boolean exists = em.createQuery("SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn", Long.class)
						           .setParameter("isbn", seed.getIsbn())
						           .getSingleResult() > 0;
				if (!exists) {
					em.persist(seed);
				}
			}
			tx.commit();
		} finally {
			em.close();
		}
	}

	private List<Book> seeds() {
		return List.of(
			book("Java 程式設計", "張三", "978-957-1111-01-0", 580.0, LocalDate.of(2024, 1, 15), "程式設計", 50),
			book("深入淺出 Spring", "李四", "978-957-1111-02-7", 720.0, LocalDate.of(2023, 5, 20), "程式設計", 30),
			book("SQLite 實戰", "王五", "978-957-1111-03-4", 450.0, LocalDate.of(2022, 9, 1), "資料庫", 80),
			book("RESTful API 設計", "趙六", "978-957-1111-04-1", 660.0, LocalDate.of(2024, 3, 10), "網頁開發", 25),
			book("前端三劍客", "陳七", "978-957-1111-05-8", 520.0, LocalDate.of(2023, 12, 5), "前端", 40)
		);
	}

	private Book book(String title, String author, String isbn, double price,
			LocalDate publishDate, String category, int stock) {
		Book b = new Book();
		b.setTitle(title);
		b.setAuthor(author);
		b.setIsbn(isbn);
		b.setPrice(price);
		b.setPublishDate(publishDate);
		b.setCategory(category);
		b.setStock(stock);
		return b;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}
