package com.sicnu.netsimu.core.statis;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnergyCost {
    float value();

    float beta() default 0.1f;
}
