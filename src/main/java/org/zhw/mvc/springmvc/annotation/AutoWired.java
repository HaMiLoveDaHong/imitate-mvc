package org.zhw.mvc.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @Retention注解表示Annotation的保留策略
 * RetentionPolicy.Class：运行时不保留，不可以通过反射读取。
 * RetentionPolicy.RUNTIME：运行是保留，可以通过反射读取。
 * RetentionPolicy.SOURCE：丢弃。
 * @Author zhw
 * @since 2022/1/17
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoWired {
    String value() default "";
}
