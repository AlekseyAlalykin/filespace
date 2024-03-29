package org.filespace.repositories;

import org.filespace.model.entities.simplerelations.File;
import org.filespace.model.entities.simplerelations.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {
    public boolean existsByMd5Hash(String md5Hash);

    public List<File> getByMd5Hash(String md5hash);

    public List<File> getAllBySenderOrderByPostDateTimeDesc(User sender);

    public void deleteAllBySender(User sender);

    public int countAllByMd5Hash(String md5Hash);

    public List<File> getAllBySenderAndFileNameIgnoreCaseStartingWithOrderByPostDateTimeDesc(User sender, String filename);
}
