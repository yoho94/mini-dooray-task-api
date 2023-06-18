package com.nhn.minidooray.taskapi.service;

import com.nhn.minidooray.taskapi.domain.request.TaskCreateRequest;
import com.nhn.minidooray.taskapi.domain.request.TaskUpdateRequest;
import com.nhn.minidooray.taskapi.domain.response.ProjectAccountResponse;
import com.nhn.minidooray.taskapi.domain.response.TasksResponse;
import com.nhn.minidooray.taskapi.entity.MilestoneEntity;
import com.nhn.minidooray.taskapi.entity.ProjectAccountEntity;
import com.nhn.minidooray.taskapi.entity.ProjectEntity;
import com.nhn.minidooray.taskapi.entity.TaskEntity;
import com.nhn.minidooray.taskapi.exception.NotFoundException;
import com.nhn.minidooray.taskapi.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class TaskServiceImplTest {

    @InjectMocks
    private TaskServiceImpl taskService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    TaskRepository taskRepository;
    @Mock
    ProjectAccountRepository projectAccountRepository;
    @Mock
    MilestoneRepository milestoneRepository;

    @Mock
    ProjectStateRepository projectStateRepository;

    @Test
    void createTask_o() {
        TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
        taskCreateRequest.setTitle("test task");
        taskCreateRequest.setProjectId(1L);
        taskCreateRequest.setWriterId("test writer");
        taskCreateRequest.setMilestoneId(1L);
        taskCreateRequest.setTagNameList(Collections.emptyList());

        ProjectEntity projectEntity = ProjectEntity.builder()
                .id(1L)
                .name("test project")
                .createAt(LocalDateTime.now())
                .build();

        MilestoneEntity milestoneEntity = MilestoneEntity.builder()
                .build();

        ProjectAccountEntity.Pk projectAccountPk = ProjectAccountEntity.Pk.builder()
                .projectId(taskCreateRequest.getProjectId())
                .accountId(taskCreateRequest.getWriterId()).build();

        ProjectAccountEntity projectAccountEntity = ProjectAccountEntity.builder()
                .pk(projectAccountPk)
                .build();

        TaskEntity taskEntity = TaskEntity.builder()
                .title(taskCreateRequest.getTitle())
                .writerId(taskCreateRequest.getWriterId())
                .projectEntity(projectEntity)
                .milestoneEntity(milestoneEntity)
                .build();
        taskEntity.setId(1L);

        // When
        when(projectRepository.findById(taskCreateRequest.getProjectId())).thenReturn(
                Optional.of(projectEntity));
        when(milestoneRepository.findById(taskCreateRequest.getMilestoneId())).thenReturn(
                Optional.of(milestoneEntity));
        when(projectAccountRepository.findByPk(projectAccountPk)).thenAnswer(invocation -> {
            ProjectAccountEntity.Pk pk = invocation.getArgument(0);
            if (pk.equals(projectAccountPk)) {
                return Optional.of(new ProjectAccountResponse() {
                    @Override
                    public String getAuthorityEntityCode() {
                        return "관리자";
                    }
                });
            }
            return Optional.empty();
        });
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(taskEntity);

        // Then
        Long taskId = taskService.createTask(taskCreateRequest);
        assertNotNull(taskId);
        assertEquals(1L, taskId);
    }

    @Test
    void updateTask_o() {
        Long taskId = 1L;
        AtomicInteger idGenerator = new AtomicInteger(1); // 자동 증가를 위한 id 생성기

        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
        taskUpdateRequest.setTitle("updated task");
        taskUpdateRequest.setProjectId(1L);
        taskUpdateRequest.setWriterId("test writer");
        taskUpdateRequest.setMilestoneId(1L);
        taskUpdateRequest.setTagNameList(Collections.emptyList());

        TaskEntity taskEntity = TaskEntity.builder()
                .title("old task")
                .writerId("old writer")
                .taskTagEntities(Collections.emptyList())
                .build();

        ProjectEntity projectEntity = ProjectEntity.builder()
                .id(1L)
                .name("test project")
                .createAt(LocalDateTime.now())
                .build();

        MilestoneEntity milestoneEntity = MilestoneEntity.builder()
                .id(1L)
                .build();

        ProjectAccountEntity.Pk projectAccountPk = ProjectAccountEntity.Pk.builder()
                .projectId(projectEntity.getId())
                .accountId("test writer")
                .build();

        // When
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskEntity));
        when(projectRepository.findById(taskUpdateRequest.getProjectId())).thenReturn(Optional.of(projectEntity));
        when(milestoneRepository.findById(taskUpdateRequest.getMilestoneId())).thenReturn(Optional.of(milestoneEntity));

        when(projectAccountRepository.findByPk(any(ProjectAccountEntity.Pk.class))).thenReturn(Optional.of(new ProjectAccountResponse() {
            @Override
            public String getAuthorityEntityCode() {
                return "관리자";
            }
        }));

        when(taskRepository.save(any(TaskEntity.class))).thenAnswer((invocation) -> {
            TaskEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId((long) idGenerator.getAndIncrement()); // id 설정
            }
            return entity;
        });

        // Then
        Long updatedTaskId = taskService.updateTask(taskId, taskUpdateRequest);
        assertNotNull(updatedTaskId);
        assertEquals(taskId, updatedTaskId);
        TaskEntity updatedTaskEntity = taskRepository.findById(updatedTaskId).orElseThrow();
        assertEquals(taskUpdateRequest.getTitle(), updatedTaskEntity.getTitle());
        assertEquals(taskUpdateRequest.getWriterId(), updatedTaskEntity.getWriterId());
        assertEquals(taskUpdateRequest.getProjectId(), updatedTaskEntity.getProjectEntity().getId());
        assertEquals(taskUpdateRequest.getMilestoneId(), updatedTaskEntity.getMilestoneEntity().getId());

    }

    @Test
    void deleteTaskTest_o() {
        // Given
        Long taskId = 1L;
        TaskEntity taskEntity = TaskEntity.builder().title("Test Task").build();

        // When
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskEntity));
        doNothing().when(taskRepository).delete(taskEntity);

        // Act
        taskService.deleteTask(taskId);

        // Then
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).delete(taskEntity);
    }

    @Test
    void deleteTaskTest_x() {
        // Given
        Long taskId = 1L;

        // When
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Then
        assertThrows(NotFoundException.class, () -> taskService.deleteTask(taskId));
    }

    @Test
    void getTasksTest_o() {
        // Given
        Long projectId = 1L;
        Pageable pageable = PageRequest.of(0, 5); // 1st page with 5 items
        List<TasksResponse> tasks = IntStream.range(0, 5)
                .mapToObj(i -> {
                            TasksResponse tasksResponse = new TasksResponse() {
                                @Override
                                public Long getProjectEntityId() {
                                    return (long) i;
                                }

                                @Override
                                public Long getId() {
                                    return (long) i;
                                }

                                @Override
                                public String getTitle() {
                                    return "test title :" + i;
                                }

                                @Override
                                public String getContent() {
                                    return "test content :" + i;
                                }

                                @Override
                                public String getWriterId() {
                                    return "test writer :" + i;
                                }

                                @Override
                                public LocalDateTime getCreateAt() {
                                    return LocalDateTime.now().minusDays(i);
                                }

                            };
                            return tasksResponse;
                        }
                )
                .collect(Collectors.toList());

        Page<TasksResponse> taskPage = new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findAllByProjectEntity_Id(anyLong(), any(Pageable.class))).thenReturn(taskPage);

        // When
        Page<TasksResponse> returnedPage = taskService.getTasks(projectId, pageable);

        // Then
        assertNotNull(returnedPage);
        assertEquals(5, returnedPage.getContent().size());

        IntStream.range(0, 5).forEach(i -> {
            assertEquals(tasks.get(i).getId(), returnedPage.getContent().get(i).getId());
            assertEquals(tasks.get(i).getTitle(), returnedPage.getContent().get(i).getTitle());
        });

        verify(taskRepository, times(1)).findAllByProjectEntity_Id(projectId, pageable);
    }

}
