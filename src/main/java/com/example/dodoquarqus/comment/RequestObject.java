package com.example.dodoquarqus.comment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestObject {

    private String type;

    private String path;

    private String description;

    private Set<Prop> queryParams;

    private String produces = "application/json";

    private String consumes = "*/*";

    private Body body;

    private Boolean async;

    private Boolean scrollable;

    private Boolean pageable = false;
}
