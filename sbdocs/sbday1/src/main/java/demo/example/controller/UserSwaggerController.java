package demo.example.controller;


import demo.example.model.User;
import demo.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/swaggerusers")
@Tag(name = "使用者管理", description = "使用者 CRUD 操作 API")
public class UserSwaggerController {
    
    private final UserService userService;
    
    public UserSwaggerController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping
    @Operation(summary = "建立使用者", description = "建立一個新的使用者")
    @ApiResponse(responseCode = "201", description = "使用者建立成功")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(
            user.getName(), 
            user.getEmail(), 
            user.getAge()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "取得使用者", description = "根據 ID 取得使用者資訊")
    @Parameter(name = "id", description = "使用者 ID", required = true)
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "取得所有使用者", description = "取得所有使用者的列表")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
