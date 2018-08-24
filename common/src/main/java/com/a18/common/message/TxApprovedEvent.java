package com.a18.common.message;

import com.a18.common.model.TxType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TxApprovedEvent implements Serializable {

  private String txId;

  private TxType txType;

  private Long userId;

  private BigDecimal amt;

  private BigDecimal RequiredBettingAmtChange;

  private BigDecimal turnoverChange;

  private LocalDateTime transactionTime;
}
