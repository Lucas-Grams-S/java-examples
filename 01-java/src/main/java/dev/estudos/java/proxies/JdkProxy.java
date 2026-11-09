package dev.estudos.java.proxies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

/**
 * J11-01 · java.lang.reflect.Proxy: proxy de interface, default methods, exceções e equals/toString.
 * Documento: java-docs/01-java/11-proxies-dinamicos-e-bytecode.md §2.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * place -&gt; placed 42
 * default -&gt; hi
 * toString passa pelo handler -&gt; proxy&#64;toString
 * checked declarada chega intacta -&gt; checked do alvo
 * chamadas vistas pelo handler -&gt; [place, hi, toString, fail]
 * isProxyClass -&gt; true
 * é OrderServiceImpl? -&gt; false
 * cast para a classe concreta -&gt; ClassCastException
 * checked não declarada -&gt; UndeclaredThrowableException(IOException)
 * </pre>
 *
 * <p>Variações: troque invokeDefault por method.invoke(real, args) e veja o que muda no método default.
 */
public class JdkProxy {
    public interface OrderService {
        String place(String id);
        default String hi() { return "hi"; }
        void fail() throws Exception;
    }

    static class OrderServiceImpl implements OrderService {
        public String place(String id) { return "placed " + id; }
        public void fail() throws Exception { throw new Exception("checked do alvo"); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        List<String> calls = new ArrayList<>();
        OrderService real = new OrderServiceImpl();
        OrderService proxy = (OrderService) Proxy.newProxyInstance(
            OrderService.class.getClassLoader(), new Class<?>[]{OrderService.class},
            (p, method, args) -> {
                calls.add(method.getName());
                if (method.isDefault()) return InvocationHandler.invokeDefault(p, method, args);
                if (method.getDeclaringClass() == Object.class) return "proxy@" + method.getName();
                try { return method.invoke(real, args); }
                catch (InvocationTargetException e) { throw e.getCause(); }
            });
        out.add("place -> " + proxy.place("42"));
        out.add("default -> " + proxy.hi());
        out.add("toString passa pelo handler -> " + proxy);
        try { proxy.fail(); } catch (Exception e) { out.add("checked declarada chega intacta -> " + e.getMessage()); }
        out.add("chamadas vistas pelo handler -> " + calls);
        out.add("isProxyClass -> " + Proxy.isProxyClass(proxy.getClass()));
        out.add("é OrderServiceImpl? -> " + (proxy instanceof OrderServiceImpl));
        try { OrderServiceImpl impl = (OrderServiceImpl) (Object) proxy; }
        catch (ClassCastException e) { out.add("cast para a classe concreta -> ClassCastException"); }
        OrderService undeclared = (OrderService) Proxy.newProxyInstance(OrderService.class.getClassLoader(),
            new Class<?>[]{OrderService.class}, (p, m, a) -> { throw new java.io.IOException("não declarada em place()"); });
        try { undeclared.place("x"); } catch (UndeclaredThrowableException e) { out.add("checked não declarada -> UndeclaredThrowableException(" + e.getCause().getClass().getSimpleName() + ")"); }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
