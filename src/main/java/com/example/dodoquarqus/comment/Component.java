package com.example.dodoquarqus.comment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Component {

    private String name;

    private String type = "spring";

    private String enableJWT = "false";

    private boolean async = true;

    private String ref;

    private boolean override = false;

    private String description;

    private BackendObject backend;

    private FrontendObject frontend;
}
