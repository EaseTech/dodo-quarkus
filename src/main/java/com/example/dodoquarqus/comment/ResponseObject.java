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
public class ResponseObject {

    private String type;

    private String ref;

    private Items items;

    private Set<Prop> properties;

    private Convert convert;

}
