package dev.estudos.java.generics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J06-01 a J06-15. */
class GenericsTest {
    @Test void j0601_basicos() {
        assertThat(GenericBasics.run()).contains("Box<Customer>.get() -> Customer[name=Ana]",
                "antes do Java 5 -> ClassCastException só em execução");
    }

    @Test void j0602_bounds() {
        assertThat(Bounds.run()).contains("max de strings -> uva", "maxNumber (Number & Comparable) -> 2.5",
                "maxSuper com Comparable<? super T> -> Dog5");
    }

    @Test void j0603_invariancia() {
        assertThat(Invariance.run()).contains("array covariante -> ArrayStoreException: java.lang.Double",
                "ArrayList<Integer> e ArrayList<Number> têm a mesma classe -> true");
    }

    @Test void j0605_pecs() {
        assertThat(Pecs.run()).contains("copyAll(List<Integer> -> List<Number>) -> [1, 2, 3]",
                "List<? super Integer>: grava Integer, lê Object -> 7", "Comparator<Object> ordena List<String> -> [a, b, c]");
    }

    @Test void j0608_reificacao() {
        assertThat(ReificationArrays.run()).contains("newArray(String.class, 2) -> String[] length=2",
                "List<String> e List<Integer> -> mesma classe: true", "array de List (sem argumento) -> List[]");
    }

    @Test void j0609_metodos_ponte() {
        assertThat(BridgeMethods.run()).contains("compareTo(Name) -> bridge=false synthetic=false",
                "compareTo(Object) -> bridge=true synthetic=true", "chamada pelo tipo raw -> ClassCastException lançada pela ponte");
    }

    @Test void j0610_metadados_sobrevivem() throws Exception {
        assertThat(SignatureMetadata.run()).contains("Field.getGenericType() -> java.util.List<java.lang.String>",
                "super type token -> java.util.Map<java.lang.String, java.lang.Integer>");
    }

    @Test void j0611_heap_pollution() {
        assertThat(HeapPollution.run()).contains("depois de raw.add(42), strings.size() -> 1",
                "strings.get(0) -> ClassCastException (longe da causa)");
    }

    @Test void j0612_repositorio_generico() {
        assertThat(GenericRepository.run()).contains("findById(9) -> Optional.empty",
                "findAll -> [Customer[id=1, name=Ana], Customer[id=2, name=Bia]]");
    }

    @Test void j0613_soma_com_wildcard() {
        assertThat(WildcardSum.run()).containsExactly("total(List<Integer>) -> 6.0", "total(List<Double>) -> 4.0");
    }

    @Test void j0614_resultado_generico() {
        assertThat(GenericResult.run()).contains("Ok.map(x -> x * 2) -> Ok[value=40]", "Err.map(x -> x * 2) -> Err[message=falhou]");
    }

    @Test void j0615_captura_de_wildcard() {
        assertThat(WildcardCapture.run()).contains("swapFirstTwo -> [b, a, c]", "emptyList inferido -> []");
    }
}
