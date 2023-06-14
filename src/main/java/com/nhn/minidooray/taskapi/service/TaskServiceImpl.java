package com.nhn.minidooray.taskapi.service;

import com.nhn.minidooray.taskapi.domain.request.TaskCreateRequest;
import com.nhn.minidooray.taskapi.domain.request.TaskUpdateRequest;
import com.nhn.minidooray.taskapi.domain.response.TaskResponse;
import com.nhn.minidooray.taskapi.domain.response.TasksResponse;
import com.nhn.minidooray.taskapi.entity.*;
import com.nhn.minidooray.taskapi.exception.NotFoundException;
import com.nhn.minidooray.taskapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskTagRepository taskTagRepository;
    private final ProjectAccountRepository projectAccountRepository;
    private final MilestoneRepository milestoneRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public Long createTask(TaskCreateRequest taskCreateRequest) {
        MilestoneEntity milestoneEntity = null;
        ProjectEntity projectEntity = projectRepository.findById(taskCreateRequest.getProjectId())
                .orElseThrow(() -> new NotFoundException("project"));
        if (taskCreateRequest.getMilestoneId() != null) {
            milestoneEntity = milestoneRepository.findById(taskCreateRequest.getMilestoneId())
                    .orElseThrow(() -> new NotFoundException("milestone"));
        }

        //프로젝트에 속한 회원인지 검사
        projectAccountRepository.findByPk(ProjectAccountEntity.Pk.builder()
                        .projectId(taskCreateRequest.getProjectId())
                        .accountId(taskCreateRequest.getWriterId()).build())
                .orElseThrow(() -> new NotFoundException("projectAccount"));

        TaskEntity task = TaskEntity.builder().title(taskCreateRequest.getTitle())
                .writerId(taskCreateRequest.getWriterId())
                .projectEntity(projectEntity)
                .milestoneEntity(milestoneEntity)
                .content(taskCreateRequest.getContent())
                .build();

        // tagName 리스트를 저장
        List<TaskTagEntity> taskTagEntityList = new ArrayList<>();

        for (String name : taskCreateRequest.getTagNameList()) {
            TagEntity tagEntity = tagRepository.findByProjectEntity_IdAndName(taskCreateRequest.getProjectId(), name)
                    .orElseGet(() -> TagEntity.builder()
                            .name(name)
                            .projectEntity(projectEntity)
                            .build());

            // tag에 없으면 새로 만든다.
            // 있으면 있는 TAG를 추가한다.
            taskTagEntityList.add(TaskTagEntity.builder()
                    .taskEntity(task)
                    .tagEntity(tagEntity)
                    .pk(TaskTagEntity.Pk.builder()
                            .taskId(task.getId())
                            .tagId(tagEntity.getId())
                            .build())
                    .build());
        }

        task.setTaskTagEntities(taskTagEntityList);

        return taskRepository.save(task)
                .getId();
    }

    @Override
    @Transactional
    public Long updateTask(Long taskId, TaskUpdateRequest taskUpdateRequest) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("task"));
        ProjectEntity projectEntity = projectRepository.findById(taskUpdateRequest.getProjectId())
                .orElseThrow(() -> new NotFoundException("project"));
        MilestoneEntity milestoneEntity = null;

        if (taskUpdateRequest.getMilestoneId() != null) {
            milestoneEntity = milestoneRepository.findById(taskUpdateRequest.getMilestoneId())
                    .orElseThrow(() -> new NotFoundException("milestone"));
        }

        //프로젝트에 속한 회원으로의 변경인지 검사
        projectAccountRepository.findByPk(ProjectAccountEntity.Pk.builder()
                        .projectId(taskUpdateRequest.getProjectId())
                        .accountId(taskUpdateRequest.getWriterId()).build())
                .orElseThrow(() -> new NotFoundException("projectAccount"));

        // tagName 리스트를 저장
        List<TaskTagEntity> taskTagEntityList = new ArrayList<>();

        for (String name : taskUpdateRequest.getTagNameList()) {
            TagEntity tagEntity = tagRepository.findByProjectEntity_IdAndName(taskUpdateRequest.getProjectId(), name)
                    .orElseGet(() -> tagRepository.save(TagEntity.builder()
                            .name(name)
                            .projectEntity(projectEntity)
                            .build()));

            // tag에 없으면 새로 만든다.
            // 있으면 있는 TAG를 추가한다.
            taskTagEntityList.add(TaskTagEntity.builder()
                    .taskEntity(taskEntity)
                    .tagEntity(tagEntity)
                    .pk(TaskTagEntity.Pk.builder()
                            .taskId(taskEntity.getId())
                            .tagId(tagEntity.getId())
                            .build())
                    .build());
        }
        taskEntity.getTaskTagEntities().clear();
        taskEntity.getTaskTagEntities().addAll(taskTagEntityList);

        taskEntity.update(taskUpdateRequest.getTitle(), projectEntity, taskUpdateRequest.getWriterId(), milestoneEntity);
        taskEntity.setContent(taskUpdateRequest.getContent());
        return taskRepository.save(taskEntity).getId();
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("task"));
        taskRepository.delete(taskEntity);
    }

    @Override
    public Page<TasksResponse> getTasks(Long projectId, Pageable pageable) {
        return taskRepository.findAllByProjectEntity_Id(projectId, pageable);
    }

    @Override
    public TaskResponse getTask(Long taskId) {
        return taskRepository.findTaskResponseById(taskId)
                .orElseThrow(() -> new NotFoundException("task"));
    }
}
