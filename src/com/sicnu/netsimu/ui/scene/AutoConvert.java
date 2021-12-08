package com.sicnu.netsimu.ui.scene;

import java.lang.annotation.*;

/**
 * 自动转换注解，使用该注解后，会按照对应方式进行转化
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoConvert {
}
