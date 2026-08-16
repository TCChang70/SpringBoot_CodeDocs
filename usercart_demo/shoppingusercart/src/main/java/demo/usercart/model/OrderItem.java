package demo.usercart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "orderitems")
@Data
@ToString(exclude = {"order"})
public class OrderItem {
    @Id   
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int pid;
    private String productTitle;
    private int productPrice;
    private int quantity;
    
    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName="id")
    @JsonIgnoreProperties("items")
    private Order order;

	
    // getters, setters
    
}

