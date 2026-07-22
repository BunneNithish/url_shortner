package com.neueda.interview.urlshortener.repository;

import com.neueda.interview.urlshortener.model.VisitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<VisitEntity, Long> {
    List<VisitEntity> findByUrlId(Long urlId);
    long countByUrlId(Long urlId);
}
