package com.example.resource;

import com.example.entity.User;
import com.example.model.LoginRequest;
import com.example.model.LoginResponse;
import com.example.repository.UserRepository;
import com.example.security.JwtUtil;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final UserRepository userRepo = new UserRepository();

    @POST
    @Path("/login")
    public Response login(LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"message\":\"Username and password are required\"}")
                           .build();
        }

        Optional<User> userOpt = userRepo.findByUsername(req.getUsername());
        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("{\"message\":\"Invalid credentials\"}")
                           .build();
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("{\"message\":\"Invalid credentials\"}")
                           .build();
        }

        String token = JwtUtil.generateToken(user.getUsername(), user.getRole());
        LoginResponse resp = new LoginResponse(token, user.getUsername(), user.getRole(), 7200);
        return Response.ok(resp).build();
    }

    @POST
    @Path("/register")
    public Response register(LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"message\":\"Username and password are required\"}")
                           .build();
        }

        if (userRepo.existsByUsername(req.getUsername())) {
            return Response.status(Response.Status.CONFLICT)
                           .entity("{\"message\":\"Username already exists\"}")
                           .build();
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt(12)));
        user.setRole("EMPLOYEE");

        userRepo.save(user);
        return Response.status(Response.Status.CREATED)
                       .entity("{\"message\":\"User registered successfully\"}")
                       .build();
    }
}
