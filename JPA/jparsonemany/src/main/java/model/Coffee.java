package model;

import java.io.Serializable;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;


@Entity
@Table(name="coffees")
@NamedQuery(name="Coffee.findAll", query="SELECT c FROM Coffee c")
public class Coffee implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="COF_NAME")
	private String cofName;

	private BigDecimal price;

	private int sales;

	@ManyToOne
	@JoinColumn(name="SUP_ID")
	@JsonIgnoreProperties("coffees")
	private Supplier supplier;

	private int total;

	public Coffee() {
	}

	public String getCofName() {
		return this.cofName;
	}

	public void setCofName(String cofName) {
		this.cofName = cofName;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getSales() {
		return this.sales;
	}

	public void setSales(int sales) {
		this.sales = sales;
	}

	public Supplier getSupplier() {
		return this.supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public int getTotal() {
		return this.total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

}