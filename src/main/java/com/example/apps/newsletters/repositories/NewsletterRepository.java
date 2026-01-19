package com.example.apps.newsletters.repositories;

import com.example.apps.newsletters.entities.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long>, JpaSpecificationExecutor<Newsletter> {
}
