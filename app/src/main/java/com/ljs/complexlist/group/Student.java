package com.ljs.complexlist.group;

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
}