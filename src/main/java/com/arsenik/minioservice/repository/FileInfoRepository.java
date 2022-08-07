package com.arsenik.minioservice.repository;

import com.arsenik.minioservice.domain.FileInfo;
import com.arsenik.minioservice.domain.UserFileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Spring Data  repository for the FileInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    Optional<FileInfo> findByUserFileInfo(UserFileInfo userFileInfo);
}
