package com.colorseq.abrowse;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserConfigGenomeDao extends MongoRepository<UserConfigGenome, String> {

    List<UserConfigGenome> findAllByUserId(int userId);

    UserConfigGenome findByUserIdAndName(int userId, String name);
}
