package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * UNIDAD: Divide y Conquista (clase 2) PUNTAJE: 1 punto
 *
 * IMPORTANTE: no vale llamar a Collections.sort ni a Arrays.sort.
 * El punto se asigna por implementar el algoritmo, no por usarlo.
 */
@Component
public class Ordenamiento {

    /**
     * QuickSort sobre una lista de items.
     *
     * Requisitos:
     * - implementar la particion (partition) a mano
     * - documentar que estrategia de pivote usaron y por que
     * - NO modificar la lista de entrada: devolver una lista nueva
     *
     * Complejidad esperada: O(n log n) promedio, O(n^2) peor caso.
     * Hay que poder explicar cuando ocurre el peor caso.
     */
    public List<Item> quickSort(
            List<Item> items,
            Comparator<Item> criterio) {

        // Copiamos la lista para no modificar la original.
        List<Item> copia = new ArrayList<>(items);

        quickSortRecursivo(
                copia, 0, copia.size() - 1, criterio);

        return copia;
    }

    private void quickSortRecursivo(
            List<Item> items,
            int inicio,
            int fin,
            Comparator<Item> criterio) {

        if (inicio >= fin) {
            return;
        }

        int pivote = particionar(
                items, inicio, fin, criterio);

        quickSortRecursivo(
                items, inicio, pivote - 1, criterio);

        quickSortRecursivo(
                items, pivote + 1, fin, criterio);
    }

    private int particionar(
            List<Item> items,
            int inicio,
            int fin,
            Comparator<Item> criterio) {

        // Estrategia: utilizar el ultimo elemento
        // como pivote (particion de Lomuto).
        Item pivote = items.get(fin);

        int i = inicio - 1;

        for (int j = inicio; j < fin; j++) {

            if (criterio.compare(items.get(j), pivote) <= 0) {
                i++;

                Item temporal = items.get(i);
                items.set(i, items.get(j));
                items.set(j, temporal);
            }
        }

        Item temporal = items.get(i + 1);
        items.set(i + 1, items.get(fin));
        items.set(fin, temporal);

        return i + 1;
    }

    /**
     * MergeSort sobre una lista de items.
     *
     * Requisitos:
     * - implementar la mezcla (merge) a mano
     * - respetar la estabilidad del algoritmo
     *
     * Complejidad esperada: O(n log n) siempre.
     *
     * Para el coloquio: escribir la recurrencia T(n) = 2T(n/2) + O(n)
     * y resolverla con la regla practica (a, b, k).
     */
    public List<Item> mergeSort(
            List<Item> items,
            Comparator<Item> criterio) {

        // Copiamos para no modificar la lista original.
        List<Item> copia = new ArrayList<>(items);

        return mergeSortRecursivo(copia, criterio);
    }

    private List<Item> mergeSortRecursivo(
            List<Item> items,
            Comparator<Item> criterio) {

        // Caso base: lista vacia o con un elemento.
        if (items.size() <= 1) {
            return items;
        }

        int mitad = items.size() / 2;

        // Dividir la lista en dos mitades.
        List<Item> izquierda = mergeSortRecursivo(
                new ArrayList<>(items.subList(0, mitad)),
                criterio);

        List<Item> derecha = mergeSortRecursivo(
                new ArrayList<>(items.subList(mitad, items.size())),
                criterio);

        // Combinar las mitades ordenadas.
        return mezclar(izquierda, derecha, criterio);
    }

    private List<Item> mezclar(
            List<Item> izquierda,
            List<Item> derecha,
            Comparator<Item> criterio) {

        List<Item> resultado = new ArrayList<>();

        int i = 0;
        int j = 0;

        while (i < izquierda.size() && j < derecha.size()) {

            if (criterio.compare(
                    izquierda.get(i), derecha.get(j)) <= 0) {

                resultado.add(izquierda.get(i));
                i++;

            } else {

                resultado.add(derecha.get(j));
                j++;
            }
        }

        // Agregar los elementos restantes.
        while (i < izquierda.size()) {
            resultado.add(izquierda.get(i));
            i++;
        }

        while (j < derecha.size()) {
            resultado.add(derecha.get(j));
            j++;
        }

        return resultado;
    }
}
