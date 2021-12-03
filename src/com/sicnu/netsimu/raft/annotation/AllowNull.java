package com.sicnu.netsimu.raft.annotation;

import java.lang.annotation.*;

/**
 * 允许值为空
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface AllowNull {
}
