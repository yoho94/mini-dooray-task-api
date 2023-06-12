package com.nhn.minidooray.taskapi.controller;


import com.nhn.minidooray.taskapi.domain.request.TaskCreateRequest;
import com.nhn.minidooray.taskapi.domain.request.TaskUpdateRequest;
import com.nhn.minidooray.taskapi.domain.response.CommonResponse;
import com.nhn.minidooray.taskapi.domain.response.ResultResponse;
import com.nhn.minidooray.taskapi.domain.response.TaskResponse;
import com.nhn.minidooray.taskapi.domain.response.TasksResponse;
import com.nhn.minidooray.taskapi.exception.ValidationFailedException;
import com.nhn.minidooray.taskapi.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${com.nhn.minidooray.taskapi.requestmapping.prefix}")
public class TaskController {
    private final TaskService taskService;

    @GetMapping("${com.nhn.minidooray.taskapi.requestmapping.get-tasks}")
    public ResultResponse<Page<TasksResponse>> getTasks(@PathVariable("projectId") Long projectId, Pageable pageable) {
        Page<TasksResponse> tasks = taskService.getTasks(projectId, pageable);

        return ResultResponse.fetched(List.of(tasks));
    }

    @GetMapping("${com.nhn.minidooray.taskapi.requestmapping.get-task}")
    public ResultResponse<TaskResponse> getTask(@PathVariable("projectId") Long projectId, @PathVariable("taskId") Long taskId) {
        TaskResponse task = taskService.getTask(taskId);

        return ResultResponse.fetched(List.of(task));
    }

    @PostMapping("${com.nhn.minidooray.taskapi.requestmapping.create-task}")
    public ResultResponse<CommonResponse> createTask(
            @RequestBody @Valid TaskCreateRequest taskCreateRequest,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationFailedException(bindingResult);
        }
        Long taskId = taskService.createTask(taskCreateRequest);
        return ResultResponse.created(List.of(CommonResponse.builder().id(taskId).build()));
    }

    @PutMapping("${com.nhn.minidooray.taskapi.requestmapping.update-task}")
    public ResultResponse<CommonResponse> updateTask(
            @PathVariable("taskId") Long taskId,
            @RequestBody @Valid TaskUpdateRequest taskUpdateRequest,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationFailedException(bindingResult);
        }
        taskService.updateTask(taskId, taskUpdateRequest);
        return ResultResponse.updated(List.of(CommonResponse.builder().id(taskId).build()));
    }

    @DeleteMapping("${com.nhn.minidooray.taskapi.requestmapping.delete-task}")
    public ResultResponse<Void> deleteTask(
            @PathVariable("taskId") Long taskId) {
        taskService.deleteTask(taskId);
        return ResultResponse.deleted(null);
    }
}
