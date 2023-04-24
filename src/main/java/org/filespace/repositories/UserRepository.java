package org.filespace.repositories;

import org.filespace.model.entities.User;
import org.filespace.model.intermediate.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public User findUserByUsername(String username);

    public boolean existsByUsernameOrEmail(String username, String email);

    public boolean existsByUsername(String username);

    public boolean existsByEmail(String email);

    public User findUserByEmail(String email);

    public Optional<User> findByUsername(String username);

    @Query(value =
            "SELECT id, username " +
            "FROM users " +
            "WHERE LOWER(username) LIKE LOWER(CONCAT(?1,'%')) " +
            "ORDER BY username " +
            "LIMIT ?2", nativeQuery = true)
    public List<UserInfo> findUsersByUsernameWithLimit(String username, Integer limit);
}
