package com.colorseq.abrowse;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConfigGenomeDao extends MongoRepository<ConfigGenome, String> {

    ConfigGenome findByName(String name);

    List<ConfigGenome> findAll();
}
