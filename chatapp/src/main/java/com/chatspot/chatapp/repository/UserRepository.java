package com.chatspot.chatapp.repository;

import com.chatspot.chatapp.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u WHERE lower(concat(u.firstName,u.lastName)) like %:name%")
    List<User> findByName(String name);
    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    List<User> findAllByIds(@Param("userIds") List<String> userIds);
}