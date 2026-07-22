package com.neueda.interview.urlshortener.repository;

import com.neueda.interview.urlshortener.model.UrlEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    Optional<UrlEntity> findByShortUrlAndIsDeletedFalse(String shortUrl);

    List<UrlEntity> findByFullUrl(String fullUrl);

    @Query("SELECT u FROM url u WHERE u.isDeleted = false AND " +
           "(LOWER(u.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullUrl) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UrlEntity> searchLinks(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM url u WHERE u.isDeleted = false")
    Page<UrlEntity> findAllActive(Pageable pageable);

    long countByIsDeletedFalse();

    @Query("SELECT COUNT(u) FROM url u WHERE u.isDeleted = false AND u.isEnabled = true AND (u.expiryDate IS NULL OR u.expiryDate > CURRENT_TIMESTAMP)")
    long countActiveLinks();

    @Query("SELECT COUNT(u) FROM url u WHERE u.isDeleted = false AND u.expiryDate IS NOT NULL AND u.expiryDate <= CURRENT_TIMESTAMP")
    long countExpiredLinks();
}
