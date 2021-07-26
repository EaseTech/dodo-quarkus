package [=package]

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.*;

import [=converterPath]
import [=repositoryPath]
import [=entityPath]

@Service
@Getter
@Setter
public class [=className] {

    private static Logger log = LoggerFactory.getLogger([=className].class);

    [=entityType]Repository repository;

    public [=className](@Autowired [=entityType]Repository repository) {
        this.repository = repository;
    }

    [=methods]
}
