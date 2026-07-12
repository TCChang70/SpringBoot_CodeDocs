package demo.example.model;


import demo.example.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class UserRepository {
    
    private final List<User> users = new CopyOnWriteArrayList<>();
    
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(java.util.UUID.randomUUID().toString());
        }
        // 更新或新增
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
        return user;
    }
    
    public Optional<User> findById(String id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }
    
    public List<User> findAll() {
        return new ArrayList<>(users);
    }
    
    public boolean deleteById(String id) {
        return users.removeIf(user -> user.getId().equals(id));
    }
    
    public long count() {
        return users.size();
    }
}
