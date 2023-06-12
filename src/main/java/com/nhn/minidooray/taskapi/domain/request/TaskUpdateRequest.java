package com.nhn.minidooray.taskapi.domain.request;

import lombok.Data;

@Data
public class TaskUpdateRequest {
    private String title;
    private Long projectId;
    private String writerId;
    private Long milestoneId;
}
