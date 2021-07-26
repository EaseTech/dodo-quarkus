package com.example.dodoquarqus.generator;


import com.example.dodoquarqus.comment.Component;
import com.example.dodoquarqus.repository.DodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpringESEntityGenerator extends EntityGenerator{

    public SpringESEntityGenerator(@Autowired DodoRepository dodoRepository) {
        super("/template/spring/", dodoRepository);
    }

    public void generate(Component component) {

        generateEntity(component);
    }
}
