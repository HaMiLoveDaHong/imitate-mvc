package org.zhw.mvc.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @Author zhw
 * @since 2022/1/17
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {

    String value() default "";
}
