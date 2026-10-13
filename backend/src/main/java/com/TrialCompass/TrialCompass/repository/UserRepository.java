package com.TrialCompass.TrialCompass.repository;

import com.TrialCompass.TrialCompass.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //extending JpaRespository gives a save, findById, findAll, deleteById methods

    //additional methods
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
