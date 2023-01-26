package org.filespace.repositories;

import org.filespace.model.entities.User;
import org.filespace.model.intermediate.FilespaceRole;
import org.filespace.model.compoundrelations.UserFilespaceKey;
import org.filespace.model.compoundrelations.UserFilespaceRelation;
import org.filespace.model.entities.Filespace;
import org.filespace.model.intermediate.FilespaceUserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserFilespaceRelationRepository extends JpaRepository<UserFilespaceRelation, UserFilespaceKey> {
    public UserFilespaceRelation getByKey(UserFilespaceKey key);

    public List<UserFilespaceRelation> getByUser(User user);

    @Query(value = "SELECT id, title, role FROM users_filespaces " +
            "JOIN filespaces ON filespace_id = id WHERE user_id = ?1",
            nativeQuery = true)
    public List<FilespaceRole> findFilespacesAndRolesByUserId(Long userId);

    public void deleteByFilespace(Filespace filespace);

    public void deleteByUser(User user);

    public void deleteByUserAndFilespace(User user, Filespace filespace);

    @Query(value = "SELECT id, username, role FROM users_filespaces " +
            "JOIN users ON user_id = id WHERE filespace_id = ?1",
            nativeQuery = true)
    public List<FilespaceUserInfo> getFilespaceUsersById(Long id);

    public Long countAllByFilespace(Filespace filespace);
}
