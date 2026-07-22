package com.neueda.interview.urlshortener.repository;

import com.neueda.interview.urlshortener.model.UrlEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class UrlRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UrlRepository urlRepository;

    @Test
    public void shouldInsertAndGetFullurl() {
        UrlEntity urlEntity = new UrlEntity("http://example.com");
        urlRepository.save(urlEntity);

        Assertions.assertNotNull(urlEntity.getId());

        UrlEntity urlEntityFromDb = urlRepository.findById(urlEntity.getId()).get();
        Assertions.assertEquals(urlEntity.getId(), urlEntityFromDb.getId());
    }

}