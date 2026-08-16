package demo.usercart.model;

import java.time.LocalDateTime;
import java.util.*;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "orders")
@Data
@ToString(exclude = {"items"})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private LocalDateTime orderTime;

    private int totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, targetEntity=OrderItem.class)
    private List<OrderItem> items = new ArrayList<>();

	
    // getters, setters
    
}

