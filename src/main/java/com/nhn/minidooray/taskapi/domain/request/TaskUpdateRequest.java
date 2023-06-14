package com.nhn.minidooray.taskapi.domain.request;

import lombok.Data;

import java.util.List;

@Data
public class TaskUpdateRequest {
    private String title;
    private String content;
    private Long projectId;
    private String writerId;
    private Long milestoneId;
    private List<String> tagNameList;
}
