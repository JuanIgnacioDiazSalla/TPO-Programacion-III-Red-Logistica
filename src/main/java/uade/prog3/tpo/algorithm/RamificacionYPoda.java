package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * UNIDAD: Branch and Bound (clase 12) PUNTAJE: 1 punto
 *
 * Es backtracking mas una cota. La diferencia con la clase Backtracking
 * es el criterio de poda: alli se podaba por VIABILIDAD, aca se poda por
 * OPTIMALIDAD.
 */
@Component
public class RamificacionYPoda {

    /** Asignacion de items a contenedores o responsables. */
    public record Asignacion(Map<String, List<Item>> porContenedor, double cargaMaxima) {
    }

    /**
     * Repartir items entre k contenedores identicos minimizando la carga del
     * contenedor mas cargado (problema de makespan).
     *
     * Requisitos:
     * - ramificar: cada item puede ir a cualquiera de los k contenedores
     * - acotar: definir una cota INFERIOR del makespan alcanzable desde el
     * nodo actual. Una cota valida y simple es
     * max( carga maxima actual , suma de todos los pesos / k )
     * - podar: si la cota es mayor o igual al mejor makespan ya encontrado,
     * abandonar la rama
     * - la cota tiene que ser OPTIMISTA. Si subestima el potencial de una
     * rama se pierden soluciones validas y el resultado deja de ser optimo
     *
     * Complejidad esperada: exponencial, k^n en el peor caso, mucho menor con
     * buena poda.
     *
     * Sugerencia: reportar en la respuesta cuantos nodos se exploraron y
     * cuantos se podaron. Es la mejor evidencia de que la poda funciona.
     */
    public Asignacion repartir(
            List<Item> items,
            int cantidadContenedores) {

        if (cantidadContenedores < 1) {
            throw new IllegalArgumentException(
                    "Debe existir al menos un contenedor");
        }

        double pesoTotal = 0;

        for (Item item : items) {
            if (!Double.isFinite(item.getPeso())
                    || item.getPeso() < 0) {
                throw new IllegalArgumentException(
                        "Los pesos deben ser finitos y no negativos");
            }
            pesoTotal += item.getPeso();
        }

        if (!Double.isFinite(pesoTotal)) {
            throw new IllegalArgumentException(
                    "El peso total no es valido");
        }

        double[] cargas = new double[cantidadContenedores];
        int[] asignacionActual = new int[items.size()];

        EstadoBusqueda estado = new EstadoBusqueda(items.size());

        // Cota inferior global: promedio de carga.
        double promedio = pesoTotal / cantidadContenedores;

        buscar(
                items,
                0,
                cargas,
                asignacionActual,
                promedio,
                estado);

        Map<String, List<Item>> resultado = new LinkedHashMap<>();

        for (int i = 0; i < cantidadContenedores; i++) {
            resultado.put("Camion " + (i + 1), new ArrayList<>());
        }

        for (int i = 0; i < items.size(); i++) {
            int camion = estado.mejorAsignacion[i];

            resultado.get("Camion " + (camion + 1))
                    .add(items.get(i));
        }

        return new Asignacion(resultado, estado.mejorCarga);
    }

    // Guarda la mejor solucion encontrada.
    private static class EstadoBusqueda {

        double mejorCarga = Double.POSITIVE_INFINITY;
        int[] mejorAsignacion;

        EstadoBusqueda(int cantidadItems) {
            mejorAsignacion = new int[cantidadItems];
        }
    }

    private void buscar(
            List<Item> items,
            int indice,
            double[] cargas,
            int[] asignacionActual,
            double promedio,
            EstadoBusqueda estado) {

        double cargaMaximaActual = 0;

        for (double carga : cargas) {
            cargaMaximaActual = Math.max(
                    cargaMaximaActual, carga);
        }

        // Cota inferior optimista del makespan.
        double cotaInferior = Math.max(
                cargaMaximaActual, promedio);

        // Poda por optimalidad.
        if (cotaInferior >= estado.mejorCarga) {
            return;
        }

        // Caso base: todos los paquetes fueron asignados.
        if (indice == items.size()) {

            estado.mejorCarga = cargaMaximaActual;

            estado.mejorAsignacion = asignacionActual.clone();

            return;
        }

        Item item = items.get(indice);

        // Ramificar: probar cada camion.
        for (int camion = 0; camion < cargas.length; camion++) {

            // Evitar explorar camiones con la misma
            // carga: son estados simetricos.
            boolean repetido = false;

            for (int anterior = 0; anterior < camion; anterior++) {
                if (cargas[anterior] == cargas[camion]) {
                    repetido = true;
                    break;
                }
            }

            if (repetido) {
                continue;
            }

            // Tomar decision.
            cargas[camion] += item.getPeso();
            asignacionActual[indice] = camion;

            buscar(
                    items,
                    indice + 1,
                    cargas,
                    asignacionActual,
                    promedio,
                    estado);

            // Retroceder.
            cargas[camion] -= item.getPeso();
        }
    }
}
