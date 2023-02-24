package org.filespace.repositories;

import org.filespace.model.entities.TokenType;
import org.filespace.model.entities.User;
import org.filespace.model.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    public VerificationToken findByToken(String token);
    public void deleteAllByUser(User user);
    public List<VerificationToken> findAllByUserAndType(User user, TokenType type);
}
