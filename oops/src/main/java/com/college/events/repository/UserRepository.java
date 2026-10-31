package com.college.events.repository;

import com.college.events.model.Role;
import com.college.events.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Storage contract for accounts.
 *
 * <p>Swap {@code InMemoryUserRepository} for a Spring Data JPA interface later -
 * the service layer only depends on this interface.</p>
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByRole(Role role);

    boolean existsByEmail(String email);

    void deleteById(Long id);

    long count();
}
