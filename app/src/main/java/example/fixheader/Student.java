package example.fixheader;

import org.immutables.value.Value;

/**
 * Created by ljs on 2016/11/2.
 */
@Value.Immutable
@Value.Modifiable
@Value.Style(deepImmutablesDetection = true)
public interface Student {
    String name();

    int age();

    int clazz();

    boolean hide();
}