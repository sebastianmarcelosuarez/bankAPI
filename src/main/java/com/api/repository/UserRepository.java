package com.api.repository;

import com.api.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Integer> {
    User findByUserName (String username);
    List<User> findAll();
}
