package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

/**
 * UNIDAD: Greedy (clase 3) y Programacion Dinamica (clase 6)
 * PUNTAJE: 1 punto greedy + 1 punto programacion dinamica
 *
 * Los dos metodos resuelven EL MISMO problema con tecnicas distintas.
 * Esa comparacion es justamente lo que se evalua: no alcanza con que
 * funcionen, hay que poder mostrar un caso donde greedy NO da el optimo.
 */
@Component
public class Seleccion {

    /** Resultado de una seleccion de items bajo restriccion de capacidad. */
    public record Resultado(List<Item> elegidos, double valorTotal, double pesoTotal, String tecnica) {
    }

    /**
     * GREEDY: seleccionar items maximizando el valor total sin superar la
     * capacidad,
     * ordenando por ratio valor/peso descendente y tomando mientras entren.
     *
     * Requisitos:
     * - declarar EXPLICITAMENTE cual es la funcion objetivo
     * - usar el ratio valor/peso como criterio, no el valor a secas
     * - documentar la complejidad, que esta dominada por el ordenamiento
     *
     * Complejidad esperada: O(n log n).
     *
     * OJO: sobre items indivisibles (mochila 0/1) este algoritmo NO garantiza
     * el optimo. Eso no es un error a corregir: es el punto de la comparacion.
     * En el coloquio hay que traer un juego de datos donde greedy pierda.
     */
    public Resultado greedy(List<Item> items, double capacidad) {
        if (!Double.isFinite(capacidad) || capacidad < 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser un numero finito y no negativo");
        }

        // Copiamos la lista para no modificar los datos originales.
        List<Item> ordenados = new ArrayList<>(items);

        // Validamos los pesos y valores.
        for (Item item : ordenados) {
            if (!Double.isFinite(item.getPeso())
                    || item.getPeso() < 0
                    || !Double.isFinite(item.getValor())
                    || item.getValor() < 0) {

                throw new IllegalArgumentException(
                        "Los paquetes deben tener pesos y valores validos");
            }
        }

        // Ordenar de mayor a menor ratio valor/peso.
        ordenados.sort(
                Comparator.comparingDouble(Item::ratioValorPeso)
                        .reversed());

        List<Item> elegidos = new ArrayList<>();

        double pesoTotal = 0;
        double valorTotal = 0;

        // Elegir cada paquete si entra completo en el camion.
        for (Item item : ordenados) {

            if (item.getPeso() <= capacidad - pesoTotal) {

                elegidos.add(item);
                pesoTotal += item.getPeso();
                valorTotal += item.getValor();
            }
        }

        return new Resultado(
                elegidos,
                valorTotal,
                pesoTotal,
                "Greedy");
    }

    /**
     * PROGRAMACION DINAMICA: mochila 0/1 con tabla dp.
     *
     * Requisitos:
     * - construir la tabla dp[i][j] de forma iterativa (bottom-up)
     * - RECUPERAR EL CAMINO: devolver que items componen la solucion optima,
     * no solo el valor maximo. Eso se hace recorriendo la tabla hacia atras
     * - los pesos deben ser enteros o discretizarse para indexar la tabla
     *
     * Complejidad esperada: O(n * W) en tiempo y en espacio.
     *
     * Para el coloquio: explicar por que O(n*W) es pseudopolinomica.
     */
    public Resultado programacionDinamica(List<Item> items, int capacidad) {
        if (capacidad < 0) {
            throw new IllegalArgumentException(
                    "La capacidad no puede ser negativa");
        }

        int n = items.size();

        // Validar los datos y convertir los pesos a enteros.
        int[] pesos = new int[n];

        for (int i = 0; i < n; i++) {
            Item item = items.get(i);

            double peso = item.getPeso();
            double valor = item.getValor();

            if (!Double.isFinite(peso)
                    || peso < 0
                    || peso != Math.floor(peso)
                    || peso > Integer.MAX_VALUE
                    || !Double.isFinite(valor)
                    || valor < 0) {

                throw new IllegalArgumentException(
                        "Mochila 0/1 requiere pesos enteros "
                                + "no negativos y valores validos");
            }

            pesos[i] = (int) peso;
        }

        // dp[i][w] representa el mayor valor posible
        // usando los primeros i items con capacidad w.
        double[][] dp = new double[n + 1][capacidad + 1];

        // Construccion bottom-up de la tabla.
        for (int i = 1; i <= n; i++) {

            Item item = items.get(i - 1);
            int peso = pesos[i - 1];
            double valor = item.getValor();

            for (int w = 0; w <= capacidad; w++) {

                // Opcion 1: no incluir el item.
                dp[i][w] = dp[i - 1][w];

                // Opcion 2: incluirlo si entra.
                if (peso <= w) {

                    double conItem = dp[i - 1][w - peso] + valor;

                    if (conItem > dp[i][w]) {
                        dp[i][w] = conItem;
                    }
                }
            }
        }

        // Reconstruir los items elegidos.
        List<Item> elegidos = new ArrayList<>();

        int capacidadRestante = capacidad;
        double pesoTotal = 0;

        for (int i = n; i >= 1; i--) {

            // Si el valor cambia respecto a la fila anterior,
            // significa que este item fue incluido.
            if (dp[i][capacidadRestante] > dp[i - 1][capacidadRestante]) {

                Item item = items.get(i - 1);

                elegidos.add(item);
                pesoTotal += item.getPeso();

                capacidadRestante -= pesos[i - 1];
            }
        }

        // Devolver los items en el orden original.
        Collections.reverse(elegidos);

        return new Resultado(
                elegidos,
                dp[n][capacidad],
                pesoTotal,
                "Programacion Dinamica");
    }
}
