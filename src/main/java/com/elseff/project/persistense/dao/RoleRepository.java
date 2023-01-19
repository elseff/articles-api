package com.elseff.project.persistense.dao;

import com.elseff.project.persistense.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role getByName(String name);
}
