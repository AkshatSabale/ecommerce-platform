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
public class DailyRevenueDTO {
  private LocalDate date;
  private double revenue;
}