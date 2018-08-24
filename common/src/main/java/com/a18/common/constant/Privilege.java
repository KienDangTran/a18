package com.a18.common.constant;

public interface Privilege {

  // auth
  String
      STAFF = "STAFF",
      USER = "USER",
      ROLE = "ROLE",
      AUTHORITY = "AUTHORITY";

  // account
  String
      ACCOUNT = "ACCOUNT",
      AGENT_LEVEL = "AGENT_LEVEL";

  // lottery
  String
      SCHEDULER = "SCHEDULER",
      LOTTERY_ISSUE = "LOTTERY_ISSUE",
      LOTTERY_PRIZE = "LOTTERY_PRIZE",
      LOTTERY_SCHEMA = "LOTTERY_SCHEMA",
      RULE = "RULE";

  String READ = "READ_", WRITE = "WRITE_", EXEC = "EXEC_";

  static String read(String authority) {
    return READ + authority;
  }

  static String write(String authority) {
    return WRITE + authority;
  }

  static String execute(String authority) {
    return EXEC + authority;
  }
}
