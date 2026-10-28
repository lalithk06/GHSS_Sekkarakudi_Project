package com.school.app.user;

import com.school.app.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserDto>> getAll() {
        List<UserDto> dtos = userService.getAllUsers().stream().map(UserDto::fromEntity).toList();
        return ApiResponse.ok(dtos);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(UserDto.fromEntity(userService.getById(id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDto> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok("User created", UserDto.fromEntity(userService.createUser(request)));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserDto> setActive(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.ok(UserDto.fromEntity(userService.setActive(id, active)));
    }

    @PutMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return ApiResponse.ok("Password reset", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.ok("User deleted", null);
    }
}
