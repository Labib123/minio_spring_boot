package com.arsenik.minioservice.repository;

import com.arsenik.minioservice.domain.UserFileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.Optional;


/**
 * Spring Data  repository for the UserFileInfo entity.
 */
@Repository
public interface UserFileInfoRepository extends JpaRepository<UserFileInfo, Long> {
 Optional<UserFileInfo> findByName(@NotNull String name);
}
