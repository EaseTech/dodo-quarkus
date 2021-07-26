package [=package];

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

<#if async == true>import reactor.core.publisher.*;</#if>


public class BaseConverter<FROM, TO> {
    private static final Logger log = LoggerFactory.getLogger(BaseConverter.class);
    public Class<TO> convertTo() {
        return (Class<TO>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[1];
    }
    public TO convert(FROM from)  {
        Class<TO> toClass = convertTo();
        TO to;
        try{
            to = toClass.getDeclaredConstructor().newInstance();
            log.info("Converting from {} to {}", from.getClass().getName(), to.getClass().getName());
            Class<? extends Object> fromClass = from.getClass();
            Field[] toFields = toClass.getDeclaredFields();
            for(Field field : toFields) {
                field.setAccessible(true);
                Class toFieldType = field.getType();
                Field fromField = fromClass.getDeclaredField(field.getName());
                fromField.setAccessible(true);
                Class fromFieldType = fromField.getType();
                if(toFieldType.isAssignableFrom(fromFieldType)) {
                    field.set(to, fromField.get(from));
                }
            }
        }catch (Exception e) {
            log.error("Cannot convert from {} to {}", from.getClass().getName(), toClass.getName());
            throw new RuntimeException("Cannot convert from: ".concat(from.getClass().getName()).concat(" to: ").concat(toClass.getName()));
        }
        return to;
    }
    public List<TO> convert(List<FROM> fromCollection) {
        return fromCollection.stream().map(this::convert).collect(Collectors.toList());
    }
    public Set<TO> convert(Set<FROM> fromCollection) {
        return fromCollection.stream().map(this::convert).collect(Collectors.toSet());
    }
    public Page<TO> convert(Page<FROM> fromCollection) {
        return new PageImpl<>(fromCollection.stream().map(this::convert).collect(Collectors.toList()));
    }
<#if async == true>
    public Flux<TO> convert(Flux<FROM> fromCollection){
        //List<TO> toCollection = new ArrayList<>();
        //fromCollection.toStream().forEach(from -> {
          //  toCollection.add(convert(from));
        //});
        return Flux.fromIterable(fromCollection.stream().map(this::convert).collect(Collectors.toList()));
    }

    public Mono<TO> convert(Mono<FROM> from){
        return Mono.just(convert(from.block()));
    }
</#if>

}
