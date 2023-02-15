package com.elseff.project.security;

import com.elseff.project.persistense.Role;
import com.elseff.project.persistense.dao.RoleRepository;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SecurityUtils {

    RoleRepository roleRepository;

    public boolean userIsAdmin(@NonNull UserDetails user) {
        Role roleAdmin = roleRepository.getByName("ROLE_ADMIN");
        return user.getAuthorities().contains(roleAdmin);
    }
}
