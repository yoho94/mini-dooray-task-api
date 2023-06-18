package com.nhn.minidooray.taskapi.repository;

import com.nhn.minidooray.taskapi.domain.response.TaskResponse;
import com.nhn.minidooray.taskapi.domain.response.TasksResponse;
import com.nhn.minidooray.taskapi.entity.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    Page<TasksResponse> findAllByProjectEntity_Id(Long projectId, Pageable pageable);

    Optional<TaskResponse> findTaskResponseById(Long taskId);
}
