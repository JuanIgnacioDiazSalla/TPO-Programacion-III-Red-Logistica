package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.service.Grafo;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * UNIDAD: Grafos I (clase 4)          PUNTAJE: 2 puntos
 *
 * Recorridos sobre grafos. Es lo primero que conviene implementar:
 * es lo mas simple, vale 2 puntos, y ademas DFS es la base estructural
 * del backtracking que van a necesitar mas adelante.
 */
@Component
public class Recorridos {

    /**
     * Recorrido en profundidad desde un vertice.
     *
     * Requisitos:
     *   - implementacion recursiva o con pila explicita (no usar librerias)
     *   - marcar visitados para no entrar en ciclos infinitos
     *   - devolver los ids de los vertices EN EL ORDEN EN QUE SE VISITAN
     *
     * Complejidad esperada: O(V + E) con lista de adyacencia.
     *
     * @param grafo grafo cargado en memoria
     * @param origenId id del vertice de partida
     * @return lista de ids en orden de visita
     */
     // DFS: recorrido en profundidad
    public List<String> dfs(Grafo grafo, String origenId) {
        int origen = grafo.indiceDe(origenId);

        boolean[] visitados =
                new boolean[grafo.cantidadVertices()];

        List<String> resultado = new ArrayList<>();

        dfsRecursivo(grafo, origen, visitados, resultado);

        return resultado;
    }

    private void dfsRecursivo(
            Grafo grafo,
            int actual,
            boolean[] visitados,
            List<String> resultado) {

        visitados[actual] = true;
        resultado.add(grafo.idDe(actual));

        for (Grafo.Arista arista : grafo.vecinos(actual)) {
            int vecino = arista.destino();

            if (!visitados[vecino]) {
                dfsRecursivo(
                        grafo, vecino, visitados, resultado);
            }
        }
    }
    /**
     * Recorrido en anchura desde un vertice.
     *
     * Requisitos:
     *   - usar una cola FIFO
     *   - marcar como visitado AL ENCOLAR, no al desencolar
     *   - devolver los ids en orden de visita
     *
     * Complejidad esperada: O(V + E).
     *
     * Para el coloquio: saber explicar por que BFS da el camino minimo
     * en cantidad de aristas pero NO en costo cuando el grafo es ponderado.
     */
    // BFS: recorrido en anchura
    public List<String> bfs(Grafo grafo, String origenId) {
        int origen = grafo.indiceDe(origenId);

        boolean[] visitados =
                new boolean[grafo.cantidadVertices()];

        List<String> resultado = new ArrayList<>();
        Queue<Integer> cola = new ArrayDeque<>();

        visitados[origen] = true;
        cola.add(origen);

        while (!cola.isEmpty()) {
            int actual = cola.poll();
            resultado.add(grafo.idDe(actual));

            for (Grafo.Arista arista : grafo.vecinos(actual)) {
                int vecino = arista.destino();

                if (!visitados[vecino]) {
                    visitados[vecino] = true;
                    cola.add(vecino);
                }
            }
        }

        return resultado;
    }
}
