package com.example.dodoquarqus.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FigmaComment {

    private String id;

    @JsonProperty("file_key")
    private String fileKey;

    @JsonProperty("parent_id")
    private String parentId;

    private User user;


    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("resolved_at")
    private String resolvedAt;

    private String message;


}
