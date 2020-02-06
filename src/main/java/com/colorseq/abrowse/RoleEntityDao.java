package com.colorseq.abrowse;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleEntityDao extends JpaRepository<RoleEntity, Integer> {

    RoleEntity findByFlag(int flag);

}
