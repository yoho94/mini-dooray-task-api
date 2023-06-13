package com.nhn.minidooray.taskapi.entity;

import com.nhn.minidooray.taskapi.domain.request.TagUpdateRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Table(name = "tag")
@Entity
@Getter
@NoArgsConstructor
public class TagEntity implements Updatable<TagUpdateRequest> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity projectEntity;

    private String name;

    private LocalDateTime createAt;

    @OneToMany(mappedBy = "tagEntity", cascade = CascadeType.REMOVE)
    private List<TaskTagEntity> taskTagEntities;

    @PrePersist
    public void setCreateAt() { this.createAt = LocalDateTime.now(); }

    @Builder
    public TagEntity(Long id, ProjectEntity projectEntity, String name) {
        this.id = id;
        this.projectEntity = projectEntity;
        this.name = name;
    }

    @Override
    public void update(TagUpdateRequest tagUpdateRequest) {
        this.name = tagUpdateRequest.getName();
    }
}
