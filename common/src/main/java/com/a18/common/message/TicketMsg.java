package com.a18.common.message;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketMsg implements Serializable {
  public Long id;

  public Short lotterySchemaId;

  public Long lotteryIssueId;

  public Long userId;

  public BigDecimal totalWinAmt = BigDecimal.ZERO;

  public Short totalWinCount;

  public String status;

  public Set<BetItemMsg> betItem;

  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class BetItemMsg implements Serializable {
    public Long id;

    public String betContent;

    public BigDecimal betAmt = BigDecimal.ZERO;

    public Short betCount = 1;

    public BigDecimal winAmt = BigDecimal.ZERO;

    public Short winCount = 0;

    public String status;
  }
}
