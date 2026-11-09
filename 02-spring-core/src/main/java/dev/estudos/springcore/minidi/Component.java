package dev.estudos.springcore.minidi;

import java.lang.annotation.*;

/** Equivalente (muito simplificado) ao {@code @Component}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {}
