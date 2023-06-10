package com.nhn.minidooray.taskapi.service;

import com.nhn.minidooray.taskapi.domain.request.CommentCreateRequest;
import com.nhn.minidooray.taskapi.domain.request.CommentUpdateRequest;
import com.nhn.minidooray.taskapi.entity.CommentEntity;
import com.nhn.minidooray.taskapi.entity.TaskEntity;
import com.nhn.minidooray.taskapi.exception.NotFoundException;
import com.nhn.minidooray.taskapi.repository.CommentRepository;
import com.nhn.minidooray.taskapi.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    @Override
    public Long createComment(Long taskId, CommentCreateRequest commentCreateRequest) {
        CommentEntity parent = null;
        if (commentCreateRequest.getParentId() != null) {
            parent = commentRepository.findById(commentCreateRequest.getParentId())
                    .orElseThrow(() -> new NotFoundException("comment"));
        }
        TaskEntity taskEntity = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("task"));

        CommentEntity commentEntity = CommentEntity.builder()
                .content(commentCreateRequest.getContent())
                .parentComment(parent)
                .taskEntity(taskEntity)
                .writerId(commentCreateRequest.getWriterId())
                .build();

        return commentRepository.save(commentEntity).getId();
    }

    @Override
    public Long updateComment(Long commentId, CommentUpdateRequest commentUpdateRequest) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("comment"));
        commentEntity.update(commentUpdateRequest);
        return commentRepository.save(commentEntity).getId();
    }

    @Override
    public void deleteComment(Long commentId) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("comment"));
        commentRepository.delete(commentEntity);
    }
}
