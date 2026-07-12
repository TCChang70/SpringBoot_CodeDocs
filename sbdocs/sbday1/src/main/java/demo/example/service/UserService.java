package demo.example.service;

import demo.example.model.User;
import demo.example.model.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User createUser(String name, String email, int age) {
        User user = new User(name, email, age);
        return userRepository.save(user);
    }
    
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User updateUser(String id, String name, String email, int age) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setName(name);
            user.setEmail(email);
            user.setAge(age);
            return userRepository.save(user);
        }
        throw new RuntimeException("使用者不存在: " + id);
    }
    
    public boolean deleteUser(String id) {
        return userRepository.deleteById(id);
    }
    
    public long getUserCount() {
        return userRepository.count();
    }
}
