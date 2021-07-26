package com.example.dodoquarqus.comment;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Entity {

    private String type;

    private Set<Prop> properties;
}
