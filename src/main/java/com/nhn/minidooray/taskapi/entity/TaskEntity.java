package com.nhn.minidooray.taskapi.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "task")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity projectEntity;

    private String writerId;

    @ManyToOne
    @JoinColumn(name = "mile_stone_id")
    private MilestoneEntity milestoneEntity;

    @Column(updatable = false)
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "taskEntity", cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<TaskTagEntity> taskTagEntities = new ArrayList<>();

    @PrePersist
    public void setCreateAt() {
        this.createAt = LocalDateTime.now();
    }

    @Builder
    public TaskEntity(Long id, String title, ProjectEntity projectEntity, String writerId, MilestoneEntity milestoneEntity, String content, List<TaskTagEntity> taskTagEntities) {
        this.id = id;
        this.title = title;
        this.projectEntity = projectEntity;
        this.writerId = writerId;
        this.milestoneEntity = milestoneEntity;
        this.content = content;
        this.taskTagEntities = taskTagEntities;
    }

    public void update(String title, ProjectEntity projectEntity, String writerId, MilestoneEntity milestoneEntity) {
        this.title = title == null ? this.title : title;
        this.projectEntity = projectEntity == null ? this.projectEntity : projectEntity;
        this.writerId = writerId == null ? this.writerId : writerId;
        this.milestoneEntity = milestoneEntity;
    }
}
