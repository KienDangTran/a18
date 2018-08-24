package com.a18.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.a18.common.model.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "role_authority", schema = "auth")
public class RoleAuthority extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Short id;

  @JsonIgnore
  @ManyToOne(optional = false)
  private Authority authority;

  @JsonIgnore
  @ManyToOne(optional = false)
  private Role role;

  private boolean read = true;

  private boolean write = false;

  private boolean execute = false;
}
