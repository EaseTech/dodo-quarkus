package com.example.dodoquarqus.repository;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassMeta {

    private String name;

    private List<String> methods;
}
