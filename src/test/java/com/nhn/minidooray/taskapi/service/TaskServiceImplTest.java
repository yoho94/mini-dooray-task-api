package com.nhn.minidooray.taskapi.service;

import com.nhn.minidooray.taskapi.domain.request.TaskCreateRequest;
import com.nhn.minidooray.taskapi.domain.request.TaskUpdateRequest;
import com.nhn.minidooray.taskapi.domain.response.ProjectAccountResponse;
import com.nhn.minidooray.taskapi.entity.MilestoneEntity;
import com.nhn.minidooray.taskapi.entity.TaskEntity;
import com.nhn.minidooray.taskapi.repository.AuthorityRepository;
import com.nhn.minidooray.taskapi.repository.MilestoneRepository;
import com.nhn.minidooray.taskapi.repository.ProjectAccountRepository;
import com.nhn.minidooray.taskapi.repository.ProjectRepository;
import com.nhn.minidooray.taskapi.repository.ProjectStateRepository;
import com.nhn.minidooray.taskapi.repository.TaskRepository;
import net.bytebuddy.asm.Advice;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import com.nhn.minidooray.taskapi.domain.request.ProjectCreateRequest;
import com.nhn.minidooray.taskapi.domain.request.ProjectUpdateRequest;
import com.nhn.minidooray.taskapi.domain.response.ProjectByAccountResponse;
import com.nhn.minidooray.taskapi.entity.AuthorityEntity;
import com.nhn.minidooray.taskapi.entity.ProjectAccountEntity;
import com.nhn.minidooray.taskapi.entity.ProjectEntity;
import com.nhn.minidooray.taskapi.entity.ProjectStateEntity;
import com.nhn.minidooray.taskapi.enumerate.AuthorityType;
import com.nhn.minidooray.taskapi.enumerate.ProjectStateType;
import com.nhn.minidooray.taskapi.exception.NotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
        taskCreateRequest.setName( "test task" );
        taskCreateRequest.setProjectId( 1L );
        taskCreateRequest.setWriterId( "test writer" );
        taskCreateRequest.setMilestoneId( 1L );

        ProjectEntity projectEntity = ProjectEntity.builder()
            .id( 1L )
            .name( "test project" )
            .createAt( LocalDateTime.now() )
            .build();

        MilestoneEntity milestoneEntity = MilestoneEntity.builder()
            .build();

        ProjectAccountEntity.Pk projectAccountPk = ProjectAccountEntity.Pk.builder()
            .projectId( taskCreateRequest.getProjectId() )
            .accountId( taskCreateRequest.getWriterId() ).build();

        ProjectAccountEntity projectAccountEntity = ProjectAccountEntity.builder()
            .pk( projectAccountPk )
            .build();

        TaskEntity taskEntity = TaskEntity.builder()
            .title( taskCreateRequest.getName() )
            .writerId( taskCreateRequest.getWriterId() )
            .projectEntity( projectEntity )
            .milestoneEntity( milestoneEntity )
            .build();
        taskEntity.setId( 1L );

        // When
        when( projectRepository.findById( taskCreateRequest.getProjectId() ) ).thenReturn(
            Optional.of( projectEntity ) );
        when( milestoneRepository.findById( taskCreateRequest.getMilestoneId() ) ).thenReturn(
            Optional.of( milestoneEntity ) );
        when( projectAccountRepository.findByPk( projectAccountPk ) ).thenAnswer( invocation -> {
            ProjectAccountEntity.Pk pk = invocation.getArgument( 0 );
            if (pk.equals( projectAccountPk )) {
                return Optional.of( new ProjectAccountResponse() {
                    @Override
                    public String getAuthorityEntityCode() {
                        return "관리자";
                    }
                } );
            }
            return Optional.empty();
        } );
        when( taskRepository.save( any( TaskEntity.class ) ) ).thenReturn( taskEntity );

        // Then
        Long taskId = taskService.createTask( taskCreateRequest );
        assertNotNull( taskId );
        assertEquals( 1L, taskId );
    }

    @Test
    void updateTask_o() {
        // Given
        Long taskId = 1L;
        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
        taskUpdateRequest.setName("updated task");
        taskUpdateRequest.setProjectId(1L);
        taskUpdateRequest.setWriterId("test writer");
        taskUpdateRequest.setMilestoneId(1L);

        TaskEntity taskEntity = TaskEntity.builder()
            .title("old task")
            .writerId("old writer")
            .build();

        ProjectEntity projectEntity = ProjectEntity.builder()
            .name("test project")
            .createAt(LocalDateTime.now())
            .build();

        MilestoneEntity milestoneEntity = MilestoneEntity.builder()
            .build();

        ProjectAccountEntity.Pk projectAccountPk = ProjectAccountEntity.Pk.builder()
            .projectId(projectEntity.getId())
            .accountId("test writer")
            .build();

        ProjectAccountEntity projectAccountEntity = ProjectAccountEntity.builder()
            .pk(projectAccountPk)
            .build();

        // When
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskEntity));
        when(projectRepository.findById(taskUpdateRequest.getProjectId())).thenReturn(Optional.of(projectEntity));
        when(milestoneRepository.findById(taskUpdateRequest.getMilestoneId())).thenReturn(Optional.of(milestoneEntity));
        // Given
        when(projectAccountRepository.findByPk(any(ProjectAccountEntity.Pk.class))).thenAnswer((invocation) -> {
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
        Long updatedTaskId = taskService.updateTask(taskId, taskUpdateRequest);
        assertNotNull(updatedTaskId);
        assertEquals(taskId, updatedTaskId);
        assertEquals(taskUpdateRequest.getName(), taskEntity.getTitle());
        assertEquals(taskUpdateRequest.getProjectId(), taskEntity.getProjectEntity().getId());
        assertEquals(taskUpdateRequest.getWriterId(), taskEntity.getWriterId());
        assertEquals(taskUpdateRequest.getMilestoneId(), taskEntity.getMilestoneEntity().getId());
    }

}
