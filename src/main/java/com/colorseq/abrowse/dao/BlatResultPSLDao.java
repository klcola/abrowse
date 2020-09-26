package com.colorseq.abrowse.dao;

import com.colorseq.abrowse.entity.BlatResultPSL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlatResultPSLDao extends JpaRepository<BlatResultPSL, Integer> {

    /**
     * 根据jobid查询blat结果
     * @param id
     * @return
     */
    List<BlatResultPSL> findAllByJobidEqualsOrderByBmatchDesc(String id);

}
