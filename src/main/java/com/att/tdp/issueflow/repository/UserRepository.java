package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.model.Role;
import com.att.tdp.issueflow.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    List<UserEntity> findByRoleOrderByIdAsc(Role role);
}
