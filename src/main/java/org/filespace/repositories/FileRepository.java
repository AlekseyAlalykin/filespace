package org.filespace.repositories;

import org.filespace.model.entities.File;
import org.filespace.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    public boolean existsByMd5Hash(String md5Hash);

    public List<File> getAllBySender(User sender);

    public void deleteAllBySender(User sender);

    public int countAllByMd5Hash(String md5Hash);
}
