package com.a18.common.message;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceAdjustedMsg implements Serializable {

  private String txId;

  private Long userId;

  private String remark;

  private boolean success;
}
