package com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.repository;

import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.EReaction;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.Reaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    
    // Buscar reacción por su nombre enum
    Optional<Reaction> findByName(EReaction name);

    // Verificar si ya existe una reacción por su nombre enum
    boolean existsByName(EReaction name);
}
