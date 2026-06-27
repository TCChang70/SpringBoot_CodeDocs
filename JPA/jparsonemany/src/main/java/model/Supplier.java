package model;

import java.io.Serializable;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name="suppliers")
@NamedQuery(name="Supplier.findAll", query="SELECT s FROM Supplier s")
public class Supplier implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="SUP_ID")
	private int supId;

	@Column(name="SUP_NAME")
	private String supName;

	private String street;
	private String city;
	private String state;
	private String zip;

	@OneToMany(mappedBy="supplier")
	@JsonIgnore
	private List<Coffee> coffees;

	public Supplier() {
	}

	public int getSupId() {
		return this.supId;
	}

	public void setSupId(int supId) {
		this.supId = supId;
	}

	public String getSupName() {
		return this.supName;
	}

	public void setSupName(String supName) {
		this.supName = supName;
	}

	public String getStreet() {
		return this.street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return this.zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public List<Coffee> getCoffees() {
		return this.coffees;
	}

	public void setCoffees(List<Coffee> coffees) {
		this.coffees = coffees;
	}
}
