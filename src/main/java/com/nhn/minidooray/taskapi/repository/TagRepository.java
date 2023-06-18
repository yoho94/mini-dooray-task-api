package com.nhn.minidooray.taskapi.repository;

import com.nhn.minidooray.taskapi.entity.ProjectEntity;
import com.nhn.minidooray.taskapi.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Long>, TagCustomRepository {
    Optional<TagEntity> findByIdAndProjectEntity(Long id, ProjectEntity projectEntity);

    Optional<TagEntity> findByProjectEntity_IdAndName(Long id, String name);
}
