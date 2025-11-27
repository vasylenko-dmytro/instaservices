package com.vasylenko.edu.ig.to.kafka.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IGPost {
    private String id;
    private String permalink;
    private String caption;
    private String timestamp;
}
