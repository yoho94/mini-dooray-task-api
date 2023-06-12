package com.nhn.minidooray.taskapi.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface TagByGetTaskResponse {

    @JsonProperty("tagId")
    Long getTagEntityId();

    @JsonProperty("tagName")
    String getTagEntityName();
}
