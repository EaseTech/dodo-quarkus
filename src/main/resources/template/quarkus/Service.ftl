package [=package]

import lombok.Getter;
import lombok.Setter;
import java.util.*;
import org.jboss.logging.Logger;
import javax.inject.Inject;
import javax.enterprise.context.ApplicationScoped;

import [=converterPath]
import [=repositoryPath]
import [=entityPath]
<#if async == true>import reactor.core.publisher.*;</#if>

@Getter
@Setter
@ApplicationScoped
public class [=className] {

    @Inject
    Logger log;

    [=entityType]Repository repository;

    public [=className]([=entityType]Repository repository) {
        this.repository = repository;
    }

    [=methods]
}
