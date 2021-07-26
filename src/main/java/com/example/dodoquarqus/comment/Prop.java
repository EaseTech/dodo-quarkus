package com.example.dodoquarqus.comment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Prop {

    private String type;

    private Items items;

    private String name;

    private boolean required;

    private Set<Prop> properties;

    @Override
    public String toString() {
        return "Prop{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
