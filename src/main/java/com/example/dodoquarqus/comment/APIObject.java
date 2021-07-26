package com.example.dodoquarqus.comment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class APIObject {

    private String name;

    private Boolean async = true;

    private RequestObject request;

    private ResponseObject response;

    private Component component;

    private List<String> roles = new ArrayList<>();


}
