package com.bonnysimon.starter.features.mail;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class EmailPayload {
    private List<String> to;
    private String subject;
    private String template;
    private Map<String, Object> context;
}
