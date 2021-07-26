package [=package];
<#if async == true>
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
<#else>
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
</#if>
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import java.util.*;
import reactor.core.publisher.*;

import [=entityPath]

@Repository
<#if async == true>
public interface [=className] extends ReactiveSortingRepository<[=entityType], [=idType]>, [=className]Custom {

    [=methods]

}
<#else>
public interface [=className] extends ElasticsearchRepository<[=entityType], [=idType]>, [=className]Custom {

    [=methods]

}

</#if>