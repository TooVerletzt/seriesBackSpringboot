package com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.repository;

import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.TweetReaction;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.Tweet;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TweetReactionRepository extends JpaRepository<TweetReaction, Long> {
    Optional<TweetReaction> findByTweetAndUser(Tweet tweet, User user);
    List<TweetReaction> findByTweetId(Long tweetId);
}

