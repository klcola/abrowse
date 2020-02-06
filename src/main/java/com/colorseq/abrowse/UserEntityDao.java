package com.colorseq.abrowse;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityDao extends JpaRepository<UserEntity, Integer> {

    UserEntity findById(int id);

    UserEntity findByUsername(String username);

}
