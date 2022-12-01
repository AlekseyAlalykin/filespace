package org.filespace.repositories;

import org.filespace.model.File;
import org.filespace.model.Log;
import org.filespace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    public List<Log> findAllByUser(User user);

    public List<Log> findAllByFile(File file);
}
