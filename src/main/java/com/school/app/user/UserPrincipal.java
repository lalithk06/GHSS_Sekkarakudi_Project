package com.school.app.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Role role;
    private final boolean active;

    public UserPrincipal(Long id, String username, String password, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    public static UserPrincipal fromUser(User user) {
        return new UserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(), user.getRole(), user.isActive());
    }

    public Long getId() { return id; }
    public Role getRole() { return role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // A class teacher may ALSO be assigned as subject teacher for other sections.
        // Every subject-teacher action is still checked against the actual assignments
        // (AssignmentService.isSubjectTeacherOf), so this alone grants no extra data access.
        if (role == Role.CLASS_TEACHER) {
            return List.of(new SimpleGrantedAuthority("ROLE_CLASS_TEACHER"),
                    new SimpleGrantedAuthority("ROLE_SUBJECT_TEACHER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return username; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return active; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return active; }
}
