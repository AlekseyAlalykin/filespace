package org.filespace.repositories;

import org.filespace.model.intermediate.FilespaceFileInfo;
import org.filespace.model.compoundrelations.FileFilespaceKey;
import org.filespace.model.compoundrelations.FileFilespaceRelation;
import org.filespace.model.entities.File;
import org.filespace.model.entities.Filespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileFilespaceRelationRepository extends JpaRepository<FileFilespaceRelation, FileFilespaceKey> {
    public void deleteByFile(File file);

    public void deleteByFilespace(Filespace filespace);

    public boolean existsByFileAndFilespace(File file, Filespace filespace);

    @Query(value = "SELECT id, file_name as fileName, size, sender_id as senderId, comment, " +
            "number_of_downloads as numberOfDownloads, attach_time as attachTime, attach_date as attachDate " +
            "FROM files_filespaces join files on file_id = id WHERE filespace_id = ?1", nativeQuery = true)
    public List<FilespaceFileInfo> getFilesFromFilespace(Long id);

    @Modifying
    @Query(value = "DELETE FROM files_filespaces WHERE file_id IN " +
            "(SELECT id AS file_id FROM files WHERE sender_id = ?1)", nativeQuery = true)
    public void deleteFilesFromFilespaceByUserId(Long id);

    public void deleteAllByFilespace(Filespace filespace);

    public void deleteAllByFile(File file);
}
