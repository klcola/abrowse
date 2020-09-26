package com.colorseq.abrowse.dao;

import com.colorseq.abrowse.entity.AbrowseJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbrowseJobDao extends JpaRepository<AbrowseJob, Integer> {
    AbrowseJob findAbrowseJobByIdEquals(String id);
}
