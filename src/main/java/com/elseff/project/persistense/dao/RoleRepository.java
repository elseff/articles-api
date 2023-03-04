package com.elseff.project.persistense.dao;

import com.elseff.project.persistense.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    RoleEntity getByName(String name);
}
