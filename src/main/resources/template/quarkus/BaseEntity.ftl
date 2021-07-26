package [=package];

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseEntity<T> {

    public T id;
}