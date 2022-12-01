package org.filespace.repositories;

import org.filespace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public User findUserByUsername(String username);

    public boolean existsByUsernameOrEmail(String username, String email);

    public User findUserByEmail(String email);
}
