package dev.estudos.springcore.minidi.app;

import dev.estudos.springcore.minidi.Component;
import dev.estudos.springcore.minidi.Init;
import dev.estudos.springcore.minidi.MiniContext;

@Component
public class OrderRepository {
    @Init void started() { MiniContext.log.add("[init] OrderRepository pronto"); }
    String save(String id) { return "salvo " + id; }
}
