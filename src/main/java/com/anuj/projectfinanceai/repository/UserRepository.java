package com.anuj.projectfinanceai.repository;
import com.anuj.projectfinanceai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
/**
 * UserRepository provides data access for User entities
 *
 * Spring Data JPA automatically implements basic CRUD operations
 * We add custom query methods for business-specific needs
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address
     * Used for login and user lookup
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists
     * Used for registration validation
     */
    boolean existsByEmail(String email);

    /**
     * Find users by first and last name (case insensitive)
     * Used for user search functionality
     */
    Optional<User> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
}