package com.nhn.minidooray.taskapi.domain.request;

import lombok.Data;

@Data
public class TaskUpdateRequest {
    private String title;
    private String content;
    private Long projectId;
    private String writerId;
    private Long milestoneId;
}
