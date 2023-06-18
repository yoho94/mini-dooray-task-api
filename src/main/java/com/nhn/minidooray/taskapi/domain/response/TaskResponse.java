package com.nhn.minidooray.taskapi.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhn.minidooray.taskapi.entity.ProjectEntity;
import com.nhn.minidooray.taskapi.entity.TaskTagEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public interface TaskResponse {
    Long getId();

    @JsonIgnore
    ProjectEntity getProjectEntity();

    default Long getProjectId() {
        return getProjectEntity().getId();
    }

    @JsonProperty("mileStone")
    MileStoneByGetTaskResponse getMilestoneEntity();

    String getWriterId();

    String getTitle();

    String getContent();

    LocalDateTime getCreateAt();

    @JsonIgnore
    List<TaskTagEntity> getTaskTagEntities();

    default List<TagByGetTaskResponse> getTaskTagList() {
        return getTaskTagEntities().stream()
                .map(taskTagEntity -> new TagByGetTaskResponse() {
                    @Override
                    public Long getTagEntityId() {
                        return taskTagEntity.getPk().getTagId();
                    }

                    @Override
                    public String getTagEntityName() {
                        return taskTagEntity.getTagEntity().getName();
                    }
                })
                .collect(Collectors.toList());
    }
}
