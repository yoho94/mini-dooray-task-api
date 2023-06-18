package com.nhn.minidooray.taskapi.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Table(name = "task_tag")
@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "pk")
public class TaskTagEntity {
    @EmbeddedId
    private Pk pk;

    @MapsId("taskId")
    @ManyToOne
    @JoinColumn(name = "task_id")
    private TaskEntity taskEntity;

    @MapsId("tagId")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "tag_id")
    private TagEntity tagEntity;

    @Column(updatable = false)
    private LocalDateTime createAt;

    @PrePersist
    public void setCreateAt() {
        this.createAt = LocalDateTime.now();
    }

    @Builder
    public TaskTagEntity(Pk pk, TaskEntity taskEntity, TagEntity tagEntity) {
        this.pk = pk;
        this.taskEntity = taskEntity;
        this.tagEntity = tagEntity;
    }

    @NoArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Embeddable
    public static class Pk implements Serializable {
        private Long taskId;
        private Long tagId;

        @Builder
        public Pk(Long taskId, Long tagId) {
            this.taskId = taskId;
            this.tagId = tagId;
        }
    }
}
