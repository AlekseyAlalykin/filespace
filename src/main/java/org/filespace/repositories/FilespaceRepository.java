package org.filespace.repositories;

import org.filespace.model.entities.simplerelations.Filespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilespaceRepository extends JpaRepository<Filespace, Long> {

}
