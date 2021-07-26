package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.Component;
import com.example.dodoquarqus.repository.DodoRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class EntityClassGenerator {

    private QuarkusESEntityGenerator quarkusESEntityGenerator;

    private SpringESEntityGenerator springESEntityGenerator;

    public EntityClassGenerator(@Autowired QuarkusESEntityGenerator quarkusESEntityGenerator,
                                @Autowired SpringESEntityGenerator springESEntityGenerator) {
        this.quarkusESEntityGenerator = quarkusESEntityGenerator;
        this.springESEntityGenerator = springESEntityGenerator;
    }


    public void generateEntities(Component component) {

        final var type = component.getBackend().getType();
        if(type.equalsIgnoreCase("es") || type.equalsIgnoreCase("reactive_es") || type.equalsIgnoreCase("reactive-es")) {
            if(component.getType().equalsIgnoreCase("quarkus")) {
                quarkusESEntityGenerator.generateQuarkusEntity(component);
            } else if(component.getType().equalsIgnoreCase("spring")){
                springESEntityGenerator.generateEntity(component);
            }
        }
    }
}
