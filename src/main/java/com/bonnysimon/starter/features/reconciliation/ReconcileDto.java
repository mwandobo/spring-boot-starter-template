package com.bonnysimon.starter.features.reconciliation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReconcileDto {
    String firstExcel;
    String secondExcel;
}
