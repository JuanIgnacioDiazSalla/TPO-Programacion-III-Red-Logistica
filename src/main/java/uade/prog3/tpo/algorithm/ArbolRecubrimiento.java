package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.service.Grafo;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * UNIDAD: Grafos II (clase 5)
 * PUNTAJE: forma parte de los 3 puntos de "Dijkstra, Prim, Kruskal"
 *
 * ATENCION: el arbol de recubrimiento minimo solo tiene sentido sobre un
 * grafo NO DIRIGIDO y CONEXO. Al cargar el grafo hay que pedirlo con
 * grafoService.cargar(false).
 */
@Component
public class ArbolRecubrimiento {

    /** Una arista del arbol resultante, con ids de negocio. */
    public record AristaMst(String origen, String destino, double costo) {
    }

    /** Arbol de recubrimiento minimo completo. */
    public record Mst(List<AristaMst> aristas, double costoTotal) {
    }

    /**
     * Prim: hace crecer un unico arbol desde un vertice inicial, tomando en
     * cada paso la arista de menor costo que cruza de dentro hacia afuera.
     *
     * Requisitos:
     * - arreglos key[], parent[] e inMST[], o bien una cola de prioridad
     * - el resultado debe tener exactamente V-1 aristas si el grafo es conexo
     * - si el grafo NO es conexo, fallar con un mensaje claro
     *
     * Complejidad esperada: O(V^2) con busqueda lineal, O(E log V) con heap.
     * Hay que decir cual de las dos implementaron y por que.
     */
    public Mst prim(Grafo grafo, String origenId) {
        if (grafo.esDirigido()) {
            throw new IllegalArgumentException(
                    "Prim requiere un grafo no dirigido");
        }

        int n = grafo.cantidadVertices();
        int origen = grafo.indiceDe(origenId);

        double[] key = new double[n];
        int[] parent = new int[n];
        boolean[] inMST = new boolean[n];

        Arrays.fill(key, Double.POSITIVE_INFINITY);
        Arrays.fill(parent, -1);

        key[origen] = 0;

        List<AristaMst> resultado = new ArrayList<>();
        double costoTotal = 0;

        for (int i = 0; i < n; i++) {

            // Buscar el vertice no visitado con menor costo.
            int u = -1;
            double minimo = Double.POSITIVE_INFINITY;

            for (int j = 0; j < n; j++) {
                if (!inMST[j] && key[j] < minimo) {
                    minimo = key[j];
                    u = j;
                }
            }

            // Si no hay un vertice alcanzable,
            // el grafo no es conexo.
            if (u == -1) {
                throw new IllegalArgumentException(
                        "No se puede obtener un MST: el grafo no es conexo");
            }

            inMST[u] = true;

            // Agregar la arista que conecta el vertice al MST.
            if (parent[u] != -1) {
                resultado.add(
                        new AristaMst(
                                grafo.idDe(parent[u]),
                                grafo.idDe(u),
                                key[u]));

                costoTotal += key[u];
            }

            // Actualizar los costos de los vecinos.
            for (Grafo.Arista arista : grafo.vecinos(u)) {

                int v = arista.destino();
                double costo = arista.costo();

                if (!Double.isFinite(costo) || costo < 0) {
                    throw new IllegalArgumentException(
                            "Las rutas deben tener costos finitos y no negativos");
                }

                if (!inMST[v] && costo < key[v]) {
                    key[v] = costo;
                    parent[v] = u;
                }
            }
        }

        return new Mst(resultado, costoTotal);

    }

    /**
     * Kruskal: ordena TODAS las aristas por costo y las agrega salteando las
     * que formarian ciclo.
     *
     * Requisitos:
     * - implementar Union-Find (find y union) a mano, sin librerias
     * - la deteccion de ciclo es: find(u) == find(v)
     * - terminar al llegar a V-1 aristas
     *
     * Complejidad esperada: O(E log E).
     *
     * Para el coloquio: saber justificar en que caso conviene Prim y en cual
     * Kruskal, en funcion de la densidad del grafo.
     */
    public Mst kruskal(Grafo grafo) {
        if (grafo.esDirigido()) {
            throw new IllegalArgumentException(
                    "Kruskal requiere un grafo no dirigido");
        }

        int n = grafo.cantidadVertices();

        List<AristaMst> aristas = new ArrayList<>();
        Set<String> procesadas = new HashSet<>();

        // Obtener las aristas sin duplicar sus inversas.
        for (int u = 0; u < n; u++) {
            for (Grafo.Arista arista : grafo.vecinos(u)) {

                int v = arista.destino();
                double costo = arista.costo();

                if (!Double.isFinite(costo) || costo < 0) {
                    throw new IllegalArgumentException(
                            "Las rutas deben tener costos finitos y no negativos");
                }

                if (u == v) {
                    continue;
                }

                int menor = Math.min(u, v);
                int mayor = Math.max(u, v);

                String clave = menor + ":" + mayor + ":" + costo;

                if (procesadas.add(clave)) {
                    aristas.add(new AristaMst(
                            grafo.idDe(u),
                            grafo.idDe(v),
                            costo));
                }
            }
        }

        // Ordenar las aristas por costo ascendente.
        aristas.sort(Comparator.comparingDouble(AristaMst::costo));

        // Inicializar Union-Find.
        int[] padre = new int[n];
        int[] rango = new int[n];

        for (int i = 0; i < n; i++) {
            padre[i] = i;
        }

        List<AristaMst> resultado = new ArrayList<>();
        double costoTotal = 0;

        for (AristaMst arista : aristas) {

            int u = grafo.indiceDe(arista.origen());
            int v = grafo.indiceDe(arista.destino());

            int raizU = find(padre, u);
            int raizV = find(padre, v);

            // Si tienen la misma raiz, agregarla
            // generaria un ciclo.
            if (raizU == raizV) {
                continue;
            }

            // Union por rango.
            if (rango[raizU] < rango[raizV]) {
                padre[raizU] = raizV;
            } else if (rango[raizU] > rango[raizV]) {
                padre[raizV] = raizU;
            } else {
                padre[raizV] = raizU;
                rango[raizU]++;
            }

            resultado.add(arista);
            costoTotal += arista.costo();

            if (resultado.size() == n - 1) {
                break;
            }
        }

        if (resultado.size() != n - 1) {
            throw new IllegalArgumentException(
                    "No se puede obtener un MST: el grafo no es conexo");
        }

        return new Mst(resultado, costoTotal);
    }

    // Union-Find: encontrar la raiz con compresion de caminos.
    private int find(int[] padre, int x) {
        if (padre[x] != x) {
            padre[x] = find(padre, padre[x]);
        }
        return padre[x];
    }
}
