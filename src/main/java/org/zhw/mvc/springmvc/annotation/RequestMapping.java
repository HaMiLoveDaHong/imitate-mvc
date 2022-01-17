package org.zhw.mvc.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @Author zhw
 * @since 2022/1/17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
}
