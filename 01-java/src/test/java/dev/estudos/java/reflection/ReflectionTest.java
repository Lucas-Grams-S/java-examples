package dev.estudos.java.reflection;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J10-01 a J10-14. */
class ReflectionTest {
    @Test void j1001_declarando() throws Exception {
        assertThat(AnnotationDeclaration.run()).contains("RUNTIME visível -> true", "CLASS não visível por reflection -> false",
                "greet -> value=greet level=2", "plain (defaults) -> value='' level=1",
                "o objeto da anotação é um proxy -> true");
    }

    @Test void j1002_lendo() throws Exception {
        assertThat(ReadingAnnotations.run()).contains("método greet level -> 2", "@Inherited: subclasse herda -> true",
                "meta-anotação: getAnnotation não procura -> false", "meta-anotação: busca manual acha -> true",
                "anotação de interface não chega à implementação -> true");
    }

    @Test void j1003_basica() throws Exception {
        assertThat(BasicReflection.run()).contains("invoke -> hi Bob from Ana", "campo privado alterado -> hi Bob from Carla",
                "getDeclaredMethods (próprios, qualquer visibilidade) -> [greet, secret]",
                "Class.forName inexistente -> ClassNotFoundException");
    }

    @Test void j1008_method_handles() throws Throwable {
        assertThat(MethodHandleDemo.run()).contains("invokeExact -> hi Cid from Ana", "tipo do handle -> (Customer,String)String",
                "compareAndSet(7,8) -> true, age=8", "invokeExact com tipo de retorno errado -> WrongMethodTypeException");
    }

    @Test void j1010_validador() throws Exception {
        assertThat(MiniValidator.run()).contains("válido -> []", "nome em branco -> [name: must not be blank]",
                "ambos inválidos -> [name: must not be blank, email: é obrigatório]");
    }

    @Test void j1011_container() throws Exception {
        assertThat(MiniContainer.run()).contains("controller -> service usa dados / dados",
                "tipos dos parâmetros -> [Service, Repo]");
    }

    @Test void j1013_record() throws Exception {
        assertThat(RecordInspection.run()).contains("Point -> x=3 y=4", "Person -> name=Ana tags=[a, b]",
                "tipo genérico preservado -> java.util.List<java.lang.String>");
    }

    @Test void j1014_invocation_target() throws Exception {
        assertThat(UnwrapInvocation.run()).contains(
                "sem desembrulhar -> InvocationTargetException, causa java.lang.IllegalStateException: falha real",
                "desembrulhada -> IllegalStateException: falha real", "caminho feliz -> ok x");
    }
}
