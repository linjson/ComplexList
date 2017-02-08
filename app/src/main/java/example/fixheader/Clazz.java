package example.fixheader;

import org.immutables.value.Value;

import java.util.List;

/**
 * Created by ljs on 2016/11/2.
 */
@Value.Immutable
@Value.Modifiable
@Value.Style(deepImmutablesDetection = true)
public interface Clazz {
    String name();
    int index();
    List<Student> student();
    boolean hide();
}
