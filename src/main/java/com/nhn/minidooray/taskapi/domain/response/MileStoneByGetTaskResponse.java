package com.nhn.minidooray.taskapi.domain.response;

import java.time.LocalDate;

public interface MileStoneByGetTaskResponse {
    Long getId();

    String getName();

    LocalDate getStartDate();

    LocalDate getEndDate();
}
