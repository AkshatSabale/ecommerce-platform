package com.ecommerce.backend.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
public class RevenueResponse {
  private double amount;
  private LocalDate fromDate;
  private LocalDate toDate;
}