package com.armaninvestment.parsparandreporter.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDropDownDto implements Serializable {
    private Long id;
    private String desc;
}
