package dev.estudos.springcore.minidi;

import java.lang.annotation.*;

/** Equivalente ao {@code @PostConstruct}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Init {}
