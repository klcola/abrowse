package com.colorseq.abrowse.dao;

import com.colorseq.abrowse.entity.AbrowseJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbrowseJobDao extends JpaRepository<AbrowseJob, Integer> {
    AbrowseJob findAbrowseJobByIdEquals(String id);

    List<AbrowseJob> findAllBySessionIdEqualsOrderByCreateTimeDesc(String sessionId);
}
