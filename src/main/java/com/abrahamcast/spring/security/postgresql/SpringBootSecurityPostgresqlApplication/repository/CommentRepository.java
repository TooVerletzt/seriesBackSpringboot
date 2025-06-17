package com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.repository;

import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTweetId(Long tweetId);
}
