package de.schulung;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

// TODO: Lombok
public class Kunde {

  private UUID id;
  @NotNull
  @NotBlank
  @Size(max = 100)
  private String name;
  private LocalDate birthdate;
  private String status;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDate getBirthdate() {
    return birthdate;
  }

  public void setBirthdate(LocalDate birthdate) {
    this.birthdate = birthdate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
