package org.filespace.repositories;


import org.filespace.model.entities.compoundrelations.CompoundKey;
import org.filespace.model.entities.simplerelations.User;
import org.filespace.model.intermediate.FilespacePermissions;
import org.filespace.model.entities.compoundrelations.UserFilespaceRelation;
import org.filespace.model.entities.simplerelations.Filespace;
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
                    "join_date_time AS joinDateTime " +
                    "FROM users_filespaces " +
                    "JOIN filespaces ON filespace_id = id " +
                    "WHERE user_id = ?1 AND LOWER(title) LIKE LOWER(CONCAT(?2,'%'))" +
                    "ORDER BY joinDateTime DESC",
            nativeQuery = true)
    public List<FilespacePermissions> findFilespacesAndPermissionsByUserIdAndTitle(Integer userId, String title);

    @Query(value =
            "SELECT id, title, allow_download AS allowDownload, allow_upload AS allowUpload, allow_deletion AS allowDeletion, " +
            "allow_user_management AS allowUserManagement, allow_filespace_management AS allowFilespaceManagement, " +
            "join_date_time AS joinDateTime " +
            "FROM users_filespaces " +
            "JOIN filespaces ON filespace_id = id " +
            "WHERE user_id = ?1 AND filespace_id = ?2 " +
            "ORDER BY joinDateTime DESC",
            nativeQuery = true)
    public Optional<FilespacePermissions> findFilespaceAndPermissionsByUserIdAndFilespaceId(Integer userId, Integer filespaceId);

    public void deleteByFilespace(Filespace filespace);

    @Query(value =
            "SELECT id, username, allow_download AS allowDownload, allow_upload AS allowUpload, allow_deletion AS allowDeletion, " +
            "allow_user_management AS allowUserManagement, allow_filespace_management AS allowFilespaceManagement, " +
            "join_date_time AS joinDateTime " +
            "FROM users_filespaces " +
            "JOIN users ON user_id = id " +
            "WHERE filespace_id = ?1 AND LOWER(username) LIKE LOWER(CONCAT(?2,'%'))" +
            "ORDER BY joinDateTime DESC",
            nativeQuery = true)
    public List<FilespaceUserInfo> getFilespaceUsersByIdAndUsername(Integer id, String username);

    public Integer countAllByFilespace(Filespace filespace);
}
