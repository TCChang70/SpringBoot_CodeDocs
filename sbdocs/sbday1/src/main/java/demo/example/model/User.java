package demo.example.model;

import java.util.UUID;

public class User {
    private String id;
    private String name;
    private String email;
    private int age;
    
    public User() {
        this.id = UUID.randomUUID().toString();
    }
    
    public User(String name, String email, int age) {
        this();
        this.name = name;
        this.email = email;
        this.age = age;
    }
    
    // Getter 和 Setter 方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "', email='" + email + "', age=" + age + "}";
    }
}
