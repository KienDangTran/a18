package com.a18.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.a18.common.util.ClockProvider;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * An entity base class that is used for auditing purposes bounded context.
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

  @JsonIgnore
  @Version
  protected Long version;

  @JsonIgnore
  @CreationTimestamp
  private LocalDateTime createdOn;

  @JsonIgnore
  @UpdateTimestamp
  private LocalDateTime updatedOn;

  @JsonIgnore
  private Long createdBy;

  @JsonIgnore
  private Long updatedBy;

  @PrePersist
  protected void onCreate() {
    this.createdOn = ClockProvider.now();
    this.updatedOn = ClockProvider.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedOn = ClockProvider.now();
  }
}
