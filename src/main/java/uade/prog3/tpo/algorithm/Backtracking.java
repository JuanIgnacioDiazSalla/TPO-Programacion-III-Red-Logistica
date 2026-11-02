package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.service.Grafo;

import java.util.List;
import java.util.ArrayList;

/**
 * UNIDAD: Backtracking (clases 9 y 10) PUNTAJE: 1 punto
 *
 * El problema propuesto es enumerar caminos simples bajo restricciones.
 * Cada grupo puede reemplazarlo por otro de su dominio, siempre que:
 * - construya la solucion de forma incremental
 * - tenga un criterio de viabilidad que pode ramas
 * - deshaga la ultima decision al retroceder
 */
@Component
public class Backtracking {

    /** Un camino simple encontrado, con su costo acumulado. */
    public record Ruta(List<String> vertices, double costoTotal) {
    }

    /**
     * Enumerar TODOS los caminos simples de origen a destino que no superen
     * un costo maximo ni una cantidad maxima de saltos.
     *
     * Requisitos:
     * - camino SIMPLE: no repetir vertices. Marcar y desmarcar el visitado
     * es literalmente el retroceso del backtracking
     * - podar en cuanto el costo parcial supere el maximo, sin esperar a
     * completar el camino
     * - devolver todas las rutas validas, no la primera
     *
     * Complejidad esperada: exponencial en el peor caso. Hay que poder
     * justificar por que, y explicar cuanto ayuda la poda en la practica.
     *
     * Sugerencia de verificacion: contar los nodos visitados con y sin poda
     * sobre el mismo grafo. Esa comparacion es material de coloquio.
     */
    public List<Ruta> rutasSimples(
            Grafo grafo,
            String origenId,
            String destinoId,
            double costoMaximo,
            int saltosMaximos) {

        if (!Double.isFinite(costoMaximo)
                || costoMaximo < 0
                || saltosMaximos < 0) {

            throw new IllegalArgumentException(
                    "Los limites deben ser validos y no negativos");
        }

        int origen = grafo.indiceDe(origenId);
        int destino = grafo.indiceDe(destinoId);

        // La poda por costo requiere aristas no negativas.
        for (int i = 0; i < grafo.cantidadVertices(); i++) {
            for (Grafo.Arista arista : grafo.vecinos(i)) {
                if (!Double.isFinite(arista.costo())
                        || arista.costo() < 0) {

                    throw new IllegalArgumentException(
                            "Las distancias deben ser finitas y no negativas");
                }
            }
        }

        boolean[] visitados = new boolean[grafo.cantidadVertices()];

        List<String> caminoActual = new ArrayList<>();
        List<Ruta> resultados = new ArrayList<>();

        buscarRutas(
                grafo,
                origen,
                destino,
                costoMaximo,
                saltosMaximos,
                0,
                0,
                visitados,
                caminoActual,
                resultados);

        return resultados;
    }

    private void buscarRutas(
            Grafo grafo,
            int actual,
            int destino,
            double costoMaximo,
            int saltosMaximos,
            double costoActual,
            int saltosActuales,
            boolean[] visitados,
            List<String> caminoActual,
            List<Ruta> resultados) {

        // Poda: abandonar una rama que supera los limites.
        if (costoActual > costoMaximo
                || saltosActuales > saltosMaximos) {
            return;
        }

        // Tomar una decision: incorporar el deposito.
        visitados[actual] = true;
        caminoActual.add(grafo.idDe(actual));

        if (actual == destino) {

            // Guardar una copia de la ruta encontrada.
            resultados.add(
                    new Ruta(
                            new ArrayList<>(caminoActual),
                            costoActual));

        } else {

            for (Grafo.Arista arista : grafo.vecinos(actual)) {

                int vecino = arista.destino();

                if (!visitados[vecino]) {

                    double nuevoCosto = costoActual + arista.costo();

                    int nuevosSaltos = saltosActuales + 1;

                    // Poda antes de explorar la siguiente rama.
                    if (nuevoCosto <= costoMaximo
                            && nuevosSaltos <= saltosMaximos) {

                        buscarRutas(
                                grafo,
                                vecino,
                                destino,
                                costoMaximo,
                                saltosMaximos,
                                nuevoCosto,
                                nuevosSaltos,
                                visitados,
                                caminoActual,
                                resultados);
                    }
                }
            }
        }

        // Retroceso: deshacer la ultima decision.
        caminoActual.remove(caminoActual.size() - 1);
        visitados[actual] = false;
    }

    /**
     * Resultado de comparar la busqueda con y sin poda.
     */
    public record EstadisticasPoda(
            long estadosConPoda,
            long estadosSinPoda,
            long rutasValidasConPoda,
            long rutasValidasSinPoda) {
    }

    /**
     * Compara ambas estrategias sobre el mismo grafo.
     * No modifica el funcionamiento de rutasSimples().
     */
    public EstadisticasPoda compararPoda(
            Grafo grafo,
            String origenId,
            String destinoId,
            double costoMaximo,
            int saltosMaximos) {

        if (!Double.isFinite(costoMaximo)
                || costoMaximo < 0
                || saltosMaximos < 0) {
            throw new IllegalArgumentException(
                    "Los limites deben ser validos y no negativos");
        }

        int origen = grafo.indiceDe(origenId);
        int destino = grafo.indiceDe(destinoId);

        for (int i = 0; i < grafo.cantidadVertices(); i++) {
            for (Grafo.Arista arista : grafo.vecinos(i)) {
                if (!Double.isFinite(arista.costo())
                        || arista.costo() < 0) {
                    throw new IllegalArgumentException(
                            "Las distancias deben ser finitas y no negativas");
                }
            }
        }

        long[] conPoda = new long[2];
        long[] sinPoda = new long[2];

        medirRecursivo(
                grafo, origen, destino,
                costoMaximo, saltosMaximos,
                0, 0,
                new boolean[grafo.cantidadVertices()],
                true, conPoda);

        medirRecursivo(
                grafo, origen, destino,
                costoMaximo, saltosMaximos,
                0, 0,
                new boolean[grafo.cantidadVertices()],
                false, sinPoda);

        return new EstadisticasPoda(
                conPoda[0],
                sinPoda[0],
                conPoda[1],
                sinPoda[1]);
    }

    /**
     * contadores[0]: estados explorados.
     * contadores[1]: rutas validas encontradas.
     */
    private void medirRecursivo(
            Grafo grafo,
            int actual,
            int destino,
            double costoMaximo,
            int saltosMaximos,
            double costoActual,
            int saltosActuales,
            boolean[] visitados,
            boolean usarPoda,
            long[] contadores) {

        // Cada llamada recursiva representa un estado explorado.
        contadores[0]++;

        // Con poda: descartar inmediatamente estados inviables.
        if (usarPoda
                && (costoActual > costoMaximo
                        || saltosActuales > saltosMaximos)) {
            return;
        }

        visitados[actual] = true;

        if (actual == destino) {

            // Ambas estrategias cuentan solo rutas validas.
            if (costoActual <= costoMaximo
                    && saltosActuales <= saltosMaximos) {
                contadores[1]++;
            }

        } else {

            for (Grafo.Arista arista : grafo.vecinos(actual)) {

                int vecino = arista.destino();

                if (visitados[vecino]) {
                    continue;
                }

                double nuevoCosto = costoActual + arista.costo();

                int nuevosSaltos = saltosActuales + 1;

                // La estrategia con poda evita generar
                // estados que ya superan las restricciones.
                if (usarPoda
                        && (nuevoCosto > costoMaximo
                                || nuevosSaltos > saltosMaximos)) {
                    continue;
                }

                medirRecursivo(
                        grafo,
                        vecino,
                        destino,
                        costoMaximo,
                        saltosMaximos,
                        nuevoCosto,
                        nuevosSaltos,
                        visitados,
                        usarPoda,
                        contadores);
            }
        }

        // Retroceso.
        visitados[actual] = false;
    }
}
