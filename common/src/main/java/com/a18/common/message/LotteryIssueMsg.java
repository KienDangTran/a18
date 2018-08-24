package com.a18.common.message;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LotteryIssueMsg implements Serializable {
  public Long id;

  public String code;

  public Short schedulerId;

  public LocalDateTime openingTime;

  public LocalDateTime closingTime;

  public LocalDateTime endingTime;

  public String status;

  public Set<DrawResultMsg> results;

  @Builder
  @ToString
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DrawResultMsg implements Serializable {
    public Short prizeId;

    public String winNumbers;
  }
}
