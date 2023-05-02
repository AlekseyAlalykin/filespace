package org.filespace.repositories;


import org.filespace.model.entities.compoundrelations.CompoundKey;
import org.filespace.model.intermediate.FilespaceFileInfo;
import org.filespace.model.entities.compoundrelations.FileFilespaceRelation;
import org.filespace.model.entities.simplerelations.File;
import org.filespace.model.entities.simplerelations.Filespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileFilespaceRelationRepository extends JpaRepository<FileFilespaceRelation, CompoundKey> {
    public void deleteByFile(File file);

    public void deleteByFilespace(Filespace filespace);

    public boolean existsByFileAndFilespace(File file, Filespace filespace);

    @Query(value =
            "SELECT file_id AS fileId, file_name AS fileName, size, sender_id AS senderId, description, " +
            "number_of_downloads AS numberOfDownloads, attach_time AS attachTime, attach_date AS attachDate, username " +
            "FROM files_filespaces " +
            "JOIN files ON file_id = files.id " +
            "JOIN users ON sender_id = users.id " +
            "WHERE filespace_id = ?1 AND LOWER(file_name) LIKE LOWER(CONCAT(?2,'%')) " +
            "ORDER BY attach_date DESC, attach_time DESC", nativeQuery = true)
    public List<FilespaceFileInfo> getFilesFromFilespace(Long id, String filename);

    @Modifying
    @Query(value = "DELETE FROM files_filespaces WHERE file_id IN " +
            "(SELECT id AS file_id FROM files WHERE sender_id = ?1)", nativeQuery = true)
    public void deleteFilesFromFilespaceByUserId(Long id);

    public void deleteAllByFilespace(Filespace filespace);

    public void deleteAllByFile(File file);
}
