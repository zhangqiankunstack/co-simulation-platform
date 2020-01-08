package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Files;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<Files, String> {
    //根据MD5值判断文件是否存在
    boolean existsByMD5(String md5);

    //根据MD5值查询文件
    Optional<Files> findByMD5(String md5);
}
