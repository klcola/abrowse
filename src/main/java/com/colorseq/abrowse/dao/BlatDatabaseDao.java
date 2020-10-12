package com.colorseq.abrowse.dao;

import com.colorseq.abrowse.entity.BlatDatabase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlatDatabaseDao extends JpaRepository<BlatDatabase, Integer> {
    BlatDatabase findBlatDatabaseByIdEquals(String id);
}
