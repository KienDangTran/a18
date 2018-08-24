package com.a18.common.model;

public enum TxType {
  DP, // Deposit
  WD, // Withdrawal
  WIN, // Win a Bet
  BET, // Bet a game
  ZWD, // Withdraw Fail
  ZBET, // Cancel a bet
  ADJ, // Adjustment
  TRF, // User transfer money to another account
  RCV, // User receive money from another account
  PROMO, // Promotion
  RET, // User get a Return
  REB, // User get a Rebate
  COMM, // User get a Commission
  CADJ, // Company Account Adjustment'
  CTRF, // Company Account Transfer
  CREC; // Company Account Receive
}

