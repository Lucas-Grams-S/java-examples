package dev.estudos.springcore.aop;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes de SC07-09 a 12 e J11-10. */
class AopTest {
    @Test void sc0709_self_invocation() {
        assertThat(Aop.run()).containsExactly("é proxy CGLIB: aop=true cglib=true",
            "-- place (chama notify internamente)", "  [aspect] antes place", "  [aspect] depois", "placed o-1 / notified o-1",
            "-- viaProxy (AopContext)", "  [aspect] antes viaProxy", "  [aspect] antes notify", "  [aspect] depois",
            "  [aspect] depois", "notified o-2", "-- final", "final");
    }
    @Test void sc0710_jdk_vs_cglib() {
        assertThat(JdkVsCglib.run()).containsExactly("proxy padrão: jdk=true cglib=false",
            "(Impl) proxy JDK -> ClassCastException", "proxy forçado: jdk=false cglib=true, é Impl: true",
            "classe real por trás do proxy JDK -> Impl");
    }
    @Test void sc0711_corrigir_self_invocation() {
        assertThat(CorrigirSelfInvocation.run()).containsExactly("0 interna:     [intercepta place]",
            "1 outro bean:  [intercepta place, intercepta notifyCustomer]", "2 auto-@Lazy:  [intercepta a, intercepta b]");
    }
    @Test void sc0712_auditoria() {
        assertThat(AuditoriaAspect.run()).containsExactly(
            "AUDIT OrderService.place(..) executado com [o-1, 1500] => placed o-1",
            "(cancel não casa com place*: sem auditoria)");
    }
    @Test void j1110_proxy_de_fora() {
        var out = ProxyDeFora.run();
        assertThat(out.get(0)).contains("$$SpringCGLIB$$0");
        assertThat(out.subList(1, 5)).containsExactly("AopUtils.isCglibProxy -> true",
            "getClass() == OrderService.class -> false", "ultimateTargetClass -> OrderService",
            "alvo é outro objeto (não o próprio proxy) -> true");
    }
}
