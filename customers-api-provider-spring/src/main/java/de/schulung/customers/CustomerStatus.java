package de.schulung.customers;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public enum CustomerStatus {

  ACTIVE("active"),
  LOCKED("locked"),
  DISABLED("disabled");

  private final String value;

  CustomerStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public static Optional<CustomerStatus> fromValue(String value) {
    return Arrays.stream(values())
      .filter(status -> status.value.equals(value))
      .findFirst();
  }

}
