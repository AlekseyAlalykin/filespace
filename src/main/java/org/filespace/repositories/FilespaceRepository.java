package org.filespace.repositories;

import org.filespace.model.entities.Filespace;
import org.filespace.model.intermediate.FilespacePermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FilespaceRepository extends JpaRepository<Filespace, Long> {

}
