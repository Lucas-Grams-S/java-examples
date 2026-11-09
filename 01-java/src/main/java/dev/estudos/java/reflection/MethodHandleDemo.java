package dev.estudos.java.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;

/**
 * J10-08 · MethodHandle e VarHandle: acesso verificado na criação, invocação tipada.
 * Documento: java-docs/01-java/10-anotacoes-e-reflection.md §3.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * invokeExact -&gt; hi Cid from Ana
 * tipo do handle -&gt; (Customer,String)String
 * VarHandle get -&gt; 7
 * compareAndSet(7,8) -&gt; true, age=8
 * invokeExact com tipo de retorno errado -&gt; WrongMethodTypeException
 * </pre>
 *
 * <p>Variações: troque invokeExact por invoke e mude o tipo do cast para ver a diferença de rigor.
 */
public class MethodHandleDemo {
    static class Customer {
        private int age;
        private final String name;
        Customer(String name) { this.name = name; }
        public String greet(String other) { return "hi " + other + " from " + name; }
    }

    public static List<String> run() throws Throwable {
        List<String> out = new ArrayList<>();
        var lookup = MethodHandles.lookup();
        MethodHandle mh = lookup.findVirtual(Customer.class, "greet", MethodType.methodType(String.class, String.class));
        var customer = new Customer("Ana");
        String s = (String) mh.invokeExact(customer, "Cid");
        out.add("invokeExact -> " + s);
        out.add("tipo do handle -> " + mh.type());
        VarHandle vh = MethodHandles.privateLookupIn(Customer.class, lookup).findVarHandle(Customer.class, "age", int.class);
        vh.set(customer, 7);
        out.add("VarHandle get -> " + (int) vh.get(customer));
        boolean cas = vh.compareAndSet(customer, 7, 8);
        out.add("compareAndSet(7,8) -> " + cas + ", age=" + (int) vh.get(customer));
        try { Object o = mh.invokeExact(customer, "Cid"); }
        catch (java.lang.invoke.WrongMethodTypeException e) { out.add("invokeExact com tipo de retorno errado -> WrongMethodTypeException"); }
        return out;
    }

    public static void main(String[] args) throws Throwable { run().forEach(System.out::println); }
}
