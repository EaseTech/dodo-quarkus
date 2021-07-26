package [=package];

import lombok.Data;
import java.time.Instant;
import org.springframework.data.annotation.*;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.*;


@Data
@Document(indexName = [=indexName] )
public class [=entityType] {

    private @Version Long version;

    private @CreatedDate Instant createdDate;

    private @LastModifiedDate Instant lastModifiedDate;

    [=methods]

}