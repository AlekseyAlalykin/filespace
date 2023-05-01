package org.filespace.repositories;


import org.filespace.model.compoundrelations.CompoundKey;
import org.filespace.model.entities.User;
import org.filespace.model.intermediate.FilespacePermissions;
import org.filespace.model.compoundrelations.UserFilespaceRelation;
import org.filespace.model.entities.Filespace;
import org.filespace.model.intermediate.FilespaceUserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserFilespaceRelationRepository extends JpaRepository<UserFilespaceRelation, CompoundKey> {
    public List<UserFilespaceRelation> getByUser(User user);

    @Query(value =
            "SELECT id, title, allow_download AS allowDownload, allow_upload AS allowUpload, allow_deletion AS allowDeletion, " +
                    "allow_user_management AS allowUserManagement, allow_filespace_management AS allowFilespaceManagement, " +
                    "join_date AS joinDate, join_time AS joinTime " +
                    "FROM users_filespaces " +
                    "JOIN filespaces ON filespace_id = id " +
                    "WHERE user_id = ?1 AND LOWER(title) LIKE LOWER(CONCAT(?2,'%'))" +
                    "ORDER BY joinDate DESC, joinTime DESC",
            nativeQuery = true)
    public List<FilespacePermissions> findFilespacesAndPermissionsByUserIdAndTitle(Long userId, String title);

    @Query(value =
            "SELECT id, title, allow_download AS allowDownload, allow_upload AS allowUpload, allow_deletion AS allowDeletion, " +
            "allow_user_management AS allowUserManagement, allow_filespace_management AS allowFilespaceManagement, " +
            "join_date AS joinDate, join_time AS joinTime " +
            "FROM users_filespaces " +
            "JOIN filespaces ON filespace_id = id " +
            "WHERE user_id = ?1 AND filespace_id = ?2 " +
            "ORDER BY joinDate DESC, joinTime DESC",
            nativeQuery = true)
    public Optional<FilespacePermissions> findFilespaceAndPermissionsByUserIdAndFilespaceId(Long userId, Long filespaceId);

    public void deleteByFilespace(Filespace filespace);

    @Query(value =
            "SELECT id, username, allow_download AS allowDownload, allow_upload AS allowUpload, allow_deletion AS allowDeletion, " +
            "allow_user_management AS allowUserManagement, allow_filespace_management AS allowFilespaceManagement, " +
            "join_date AS joinDate, join_time AS joinTime " +
            "FROM users_filespaces " +
            "JOIN users ON user_id = id " +
            "WHERE filespace_id = ?1 AND LOWER(username) LIKE LOWER(CONCAT(?2,'%'))" +
            "ORDER BY joinDate DESC, joinTime DESC",
            nativeQuery = true)
    public List<FilespaceUserInfo> getFilespaceUsersByIdAndUsername(Long id, String username);

    public Long countAllByFilespace(Filespace filespace);
}
