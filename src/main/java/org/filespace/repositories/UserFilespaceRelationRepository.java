package org.filespace.repositories;

import org.filespace.model.UserFilespaceKey;
import org.filespace.model.UserFilespaceRelation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFilespaceRelationRepository extends JpaRepository<UserFilespaceRelation, UserFilespaceKey> {
    public UserFilespaceRelation getByKey(UserFilespaceKey key);
    public boolean existsByKey(UserFilespaceKey key);
}
