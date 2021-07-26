package [=package];

import java.util.*;
import java.util.stream.Collectors;
<#if async == true>import reactor.core.publisher.*;</#if>

public abstract class BaseConverter<FROM, TO> {

    public abstract TO convert(FROM from);

    public List<TO> convert(List<FROM> fromCollection) {
        return fromCollection.stream().map(this::convert).collect(Collectors.toList());
    }

    public Set<TO> convert(Set<FROM> fromCollection) {
        return fromCollection.stream().map(this::convert).collect(Collectors.toSet());
    }

    <#if async == true>
    public Flux<TO> convert(Flux<FROM> fromCollection){
        return Flux.fromIterable(fromCollection.toStream().map(this::convert).collect(Collectors.toList()));
    }

    public Mono<TO> convert(Mono<FROM> from){
        return Mono.just(convert(from.block()));
    }
    </#if>
}
