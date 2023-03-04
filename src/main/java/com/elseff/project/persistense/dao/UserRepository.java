package com.elseff.project.persistense.dao;

import com.elseff.project.persistense.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity getByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);
}
