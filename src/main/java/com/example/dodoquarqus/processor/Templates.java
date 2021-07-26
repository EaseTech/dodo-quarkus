package com.example.dodoquarqus.processor;

import lombok.Getter;

@Getter
public enum Templates {

    get("get.ftl"),
    put("put.ftl"),
    post("post.ftl"),
    delete("delete.ftl");

    private String template;

    Templates(String template) {
        this.template = template;
    }
}
