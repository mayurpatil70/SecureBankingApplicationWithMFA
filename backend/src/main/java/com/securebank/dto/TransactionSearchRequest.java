package com.securebank.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionSearchRequest {
    private String accountNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String type;
    private String status;
    private String description;
}