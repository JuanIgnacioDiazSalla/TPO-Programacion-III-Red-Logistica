
package uade.prog3.tpo;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.algorithm.*;
import uade.prog3.tpo.model.Item;
import uade.prog3.tpo.service.Grafo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AlgoritmosTest {

        private Grafo grafo(boolean dirigido) {
                Grafo g = new Grafo(dirigido);
                g.agregarArista("A", "B", 4);
                g.agregarArista("A", "C", 9);
                g.agregarArista("B", "C", 3);
                g.agregarArista("C", "D", 2);
                return g;
        }

        private List<Item> paquetes() {
                return List.of(
                                new Item("I1", "Item 1", 5, 10, "A"),
                                new Item("I2", "Item 2", 4, 40, "B"),
                                new Item("I3", "Item 3", 6, 30, "C"),
                                new Item("I4", "Item 4", 3, 50, "D"),
                                new Item("I5", "Item 5", 7, 55, "E"));
        }

        private List<String> ids(List<Item> items) {
                return items.stream()
                                .map(Item::getId)
                                .collect(Collectors.toList());
        }

        @Test
        void bfsRecorrePorNiveles() {
                var resultado = new Recorridos().bfs(grafo(true), "A");

                assertEquals(List.of("A", "B", "C", "D"), resultado);
        }

        @Test
        void dfsRecorreEnProfundidad() {
                var resultado = new Recorridos().dfs(grafo(true), "A");

                assertEquals(List.of("A", "B", "C", "D"), resultado);
        }

        @Test
        void dijkstraEncuentraCaminoMasCorto() {
                var resultado = new CaminosMinimos()
                                .dijkstra(grafo(true), "A", "D");

                assertEquals(List.of("A", "B", "C", "D"),
                                resultado.vertices());
                assertEquals(9.0, resultado.costoTotal(), 0.0001);
        }

        @Test
        void dijkstraRechazaPesosNegativos() {
                Grafo g = new Grafo(true);
                g.agregarArista("A", "B", -2);

                assertThrows(IllegalArgumentException.class,
                                () -> new CaminosMinimos()
                                                .dijkstra(g, "A", "B"));
        }

        @Test
        void primObtieneArbolMinimo() {
                var resultado = new ArbolRecubrimiento()
                                .prim(grafo(false), "A");

                assertEquals(3, resultado.aristas().size());
                assertEquals(9.0, resultado.costoTotal(), 0.0001);
        }

        @Test
        void kruskalObtieneArbolMinimo() {
                var resultado = new ArbolRecubrimiento()
                                .kruskal(grafo(false));

                assertEquals(3, resultado.aristas().size());
                assertEquals(9.0, resultado.costoTotal(), 0.0001);
        }

        @Test
        void quickSortOrdenaSinModificarOriginal() {
                List<Item> original = new ArrayList<>(paquetes());
                List<String> ordenInicial = ids(original);

                var resultado = new Ordenamiento().quickSort(
                                original,
                                Comparator.comparingDouble(Item::getPeso));

                assertEquals(
                                List.of("I4", "I2", "I1", "I3", "I5"),
                                ids(resultado));

                assertEquals(ordenInicial, ids(original));
        }

        @Test
        void mergeSortOrdenaSinModificarOriginal() {
                List<Item> original = new ArrayList<>(paquetes());
                List<String> ordenInicial = ids(original);

                var resultado = new Ordenamiento().mergeSort(
                                original,
                                Comparator.comparingDouble(Item::getValor));

                assertEquals(
                                List.of("I1", "I3", "I2", "I4", "I5"),
                                ids(resultado));

                assertEquals(ordenInicial, ids(original));
        }

        @Test
        void mergeSortMantieneEstabilidad() {
                List<Item> items = List.of(
                                new Item("X", "Primero", 2, 10, "A"),
                                new Item("Y", "Segundo", 2, 20, "B"),
                                new Item("Z", "Tercero", 1, 30, "C"));

                var resultado = new Ordenamiento().mergeSort(
                                items,
                                Comparator.comparingDouble(Item::getPeso));

                assertEquals(List.of("Z", "X", "Y"), ids(resultado));
        }

        @Test
        void greedyRespetaCapacidad() {
                var resultado = new Seleccion()
                                .greedy(paquetes(), 10);

                assertEquals(List.of("I4", "I2"),
                                ids(resultado.elegidos()));
                assertEquals(90.0, resultado.valorTotal(), 0.0001);
                assertEquals(7.0, resultado.pesoTotal(), 0.0001);
        }

        @Test
        void mochilaDinamicaSuperaAGreedy() {
                Seleccion seleccion = new Seleccion();

                var greedy = seleccion.greedy(paquetes(), 10);
                var optimo = seleccion.programacionDinamica(
                                paquetes(), 10);

                assertEquals(
                                List.of("I4", "I5"),
                                ids(optimo.elegidos()));
                assertEquals(105.0, optimo.valorTotal(), 0.0001);
                assertEquals(10.0, optimo.pesoTotal(), 0.0001);

                assertTrue(optimo.valorTotal() > greedy.valorTotal());
        }

        @Test
        void backtrackingEncuentraTodasLasRutasValidas() {
                var rutas = new Backtracking().rutasSimples(
                                grafo(true), "A", "D", 15, 3);

                assertEquals(2, rutas.size());

                Set<List<String>> caminos = rutas.stream()
                                .map(Backtracking.Ruta::vertices)
                                .collect(Collectors.toSet());

                assertEquals(Set.of(
                                List.of("A", "B", "C", "D"),
                                List.of("A", "C", "D")), caminos);

                assertTrue(rutas.stream()
                                .allMatch(r -> r.costoTotal() <= 15));
        }

        @Test
        void branchAndBoundMinimizaCargaMaxima() {
                var resultado = new RamificacionYPoda()
                                .repartir(paquetes(), 3);

                assertEquals(9.0, resultado.cargaMaxima(), 0.0001);

                List<Item> asignados = resultado.porContenedor()
                                .values()
                                .stream()
                                .flatMap(List::stream)
                                .collect(Collectors.toList());

                assertEquals(5, asignados.size());

                assertEquals(
                                Set.of("I1", "I2", "I3", "I4", "I5"),
                                Set.copyOf(ids(asignados)));

                for (List<Item> camion : resultado.porContenedor().values()) {

                        double carga = camion.stream()
                                        .mapToDouble(Item::getPeso)
                                        .sum();

                        assertTrue(carga <= resultado.cargaMaxima() + 0.0001);
                }
        }

        @Test
        void compararBacktrackingConYSinPoda() {

                Grafo grafo = new Grafo(true);

                grafo.agregarArista("A", "B", 4);
                grafo.agregarArista("A", "C", 9);
                grafo.agregarArista("B", "C", 3);
                grafo.agregarArista("B", "D", 12);
                grafo.agregarArista("C", "D", 2);
                grafo.agregarArista("D", "E", 5);
                grafo.agregarArista("C", "E", 20);

                Backtracking algoritmo = new Backtracking();

                var resultado = algoritmo.compararPoda(
                                grafo,
                                "A",
                                "E",
                                15,
                                4);

                System.out.println(
                                "BACKTRACKING - COMPARACION DE PODA");

                System.out.println(
                                "Estados con poda: "
                                                + resultado.estadosConPoda());

                System.out.println(
                                "Estados sin poda: "
                                                + resultado.estadosSinPoda());

                System.out.println(
                                "Rutas validas con poda: "
                                                + resultado.rutasValidasConPoda());

                System.out.println(
                                "Rutas validas sin poda: "
                                                + resultado.rutasValidasSinPoda());

                // La poda no debe eliminar soluciones validas.
                assertEquals(
                                resultado.rutasValidasSinPoda(),
                                resultado.rutasValidasConPoda());

                // La poda debe reducir el trabajo.
                assertTrue(
                                resultado.estadosConPoda() < resultado.estadosSinPoda());

                // Verificar contra el algoritmo original.
                var rutas = algoritmo.rutasSimples(
                                grafo, "A", "E", 15, 4);

                assertEquals(
                                rutas.size(),
                                resultado.rutasValidasConPoda());
        }
}
