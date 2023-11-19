package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerSelectDto implements Serializable {
    private Long id;
    private String name;
    private String phone;
    private String customerCode;
    private String economicCode;
    private String nationalCode;
}