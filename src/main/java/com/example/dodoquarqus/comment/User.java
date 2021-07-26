package com.example.dodoquarqus.comment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class User {

    private String handle;

    @JsonProperty("img_url")
    private String imageURL;

    private String id;

    @JsonProperty("client_meta")
    private ClientMeta clientMeta;

}
