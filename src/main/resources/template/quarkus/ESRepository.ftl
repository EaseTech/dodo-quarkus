package [=package];

import java.util.*;
import javax.enterprise.context.ApplicationScoped;

import [=entityPath]
<#if async == true>import reactor.core.publisher.*;</#if>

@ApplicationScoped
public class [=className] extends BaseRepository<[=entityType]>{

    [=methods]

    @Override
    protected String getIndex() {
        return [=indexName];
    }

    @Override
    protected Class<[=entityType]> getEntityClass() {
        return [=entityType].class;
    }

}