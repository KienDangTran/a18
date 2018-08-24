package com.a18.common.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {
  public String padStart(String s, int minLength, char padChar) {
    if (isBlank(s)) return s;
    if (s.length() >= minLength) return s;
    StringBuilder sb = new StringBuilder(minLength);
    for (int i = s.length(); i < minLength; i++) {
      sb.append(padChar);
    }
    return sb.append(s).toString();
  }

  public boolean isBlank(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
