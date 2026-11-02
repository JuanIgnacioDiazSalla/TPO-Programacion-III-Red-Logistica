# Endpoints — TPO Programación III: Red logística

**Dominio:** depósitos conectados por rutas ponderadas en kilómetros; paquetes con peso (kg) y valor; camiones/contenedores para distribuir carga. La aplicación utiliza Spring Boot, Neo4j y una representación del grafo en memoria. Los algoritmos se ejecutan en el paquete `algorithm`, sin consultas a la base durante sus ciclos.

**Base:** `http://localhost:8080`. Las respuestas de ejemplo corresponden a los datos semilla del proyecto. `V` = vértices, `E` = aristas, `n` = paquetes, `W` = capacidad entera, `k` = camiones.

## GET /api/grafo/resumen

**Propósito:** verificar la carga del grafo. **Parámetro opcional:** `dirigido` (`true` por defecto). **Ejemplo:** `/api/grafo/resumen`. **Respuesta esperada:** 8 vértices, 12 conexiones dirigidas, identificadores A–H. No implementa un algoritmo evaluable.

## GET /api/grafo/bfs

**Propósito:** recorrer los depósitos por niveles a partir del origen. **Parámetro:** `origen` (ID). **Ejemplo:** `/api/grafo/bfs?origen=A`. **Respuesta:** arreglo JSON con los IDs visitados en orden. **Estructuras:** cola FIFO (`ArrayDeque`) y arreglo de visitados. **Tiempo:** O(V + E), porque cada vértice se encola una vez y cada arista se inspecciona una vez. **Espacio:** O(V), para cola, visitados y resultado.

## GET /api/grafo/dfs

**Propósito:** recorrido en profundidad. **Parámetro:** `origen` (ID). **Ejemplo:** `/api/grafo/dfs?origen=A`. **Respuesta:** arreglo JSON con los IDs visitados en orden. **Estructuras:** recursión y arreglo de visitados. **Tiempo:** O(V + E), ya que cada vértice se visita una vez y se examinan sus aristas. **Espacio:** O(V), por visitados, resultado y pila de recursión.

## GET /api/grafo/dijkstra

**Propósito:** camino mínimo entre dos depósitos, con distancias no negativas. **Parámetros:** `origen`, `destino`. **Ejemplo:** `/api/grafo/dijkstra?origen=A&destino=H`. **Respuesta comprobada:**

```json
{"vertices":["A","B","C","D","F","H"],"costoTotal":20.0}
```

**Estructuras:** lista de adyacencia, `PriorityQueue`, arreglo de distancias y predecesores. **Tiempo:** O(V + E log E) con inserciones duplicadas en la cola y descarte de entradas obsoletas; para grafos simples, O((V + E) log V). La implementación valida también las aristas, lo que cuesta O(V + E). **Espacio:** O(V + E), por las entradas potenciales en la cola y los arreglos.

## GET /api/grafo/prim

**Propósito:** árbol de expansión mínima de la red **no dirigida**. **Parámetro:** `origen`. **Ejemplo:** `/api/grafo/prim?origen=A`. **Respuesta comprobada:** 7 aristas, `costoTotal: 27.0`. **Estructuras:** arreglos `key`, `parent`, `inMST`; selección lineal del menor costo. **Tiempo:** O(V² + E), porque se busca el siguiente vértice con un barrido de V posiciones en cada una de V iteraciones, además de examinar las aristas. **Espacio:** O(V), sin contar el grafo recibido.

## GET /api/grafo/kruskal

**Propósito:** árbol de expansión mínima de la red **no dirigida**. **Parámetros:** ninguno. **Ejemplo:** `/api/grafo/kruskal`. **Respuesta comprobada:** 7 aristas, `costoTotal: 27.0`. **Estructuras:** lista de aristas ordenadas, `HashSet` para evitar duplicados inversos y Union-Find con compresión de caminos y unión por rango. **Tiempo:** O(E log E + E α(V)), dominado por ordenar aristas; α es la inversa de Ackermann. **Espacio:** O(V + E). La igualdad de costos con Prim sirve como comprobación cruzada.

## GET /api/seleccion/quicksort

**Propósito:** ordenar paquetes sin modificar la lista original. **Parámetro opcional:** `criterio=valor|peso|ratio` (por defecto `ratio`). **Ejemplo:** `/api/seleccion/quicksort?criterio=peso`. **Respuesta:** lista JSON de paquetes ordenados; para peso ascendente, IDs `I4, I2, I1, I3, I5`. **Estructuras:** copia `ArrayList`, partición de Lomuto, recursión, pivote final. **Tiempo:** promedio O(n log n), peor caso O(n²) por particiones desequilibradas. **Espacio adicional:** O(n) por la copia más O(log n) de pila promedio, O(n) en el peor caso.

## GET /api/seleccion/mergesort

**Propósito:** ordenar paquetes de forma estable sin modificar la lista original. **Parámetro opcional:** `criterio=valor|peso|ratio`. **Ejemplo:** `/api/seleccion/mergesort?criterio=valor`. **Respuesta:** lista JSON ordenada; para valor ascendente, IDs `I1, I3, I2, I4, I5`. **Estructuras:** listas auxiliares y recursión de división y mezcla. **Tiempo:** O(n log n) en todos los casos, por dividir en mitades y mezclar linealmente en cada nivel. **Espacio:** O(n) auxiliar más O(log n) de pila.

## GET /api/seleccion/greedy

**Propósito:** seleccionar paquetes indivisibles maximizando valor de manera heurística, sin exceder la capacidad. **Parámetro:** `capacidad` (kg, número no negativo). **Ejemplo:** `/api/seleccion/greedy?capacidad=10`. **Respuesta comprobada:**

```json
{"elegidos":[{"id":"I4","nombre":"Item 4","peso":3.0,"valor":50.0,"nodoId":"D"},{"id":"I2","nombre":"Item 2","peso":4.0,"valor":40.0,"nodoId":"B"}],"valorTotal":90.0,"pesoTotal":7.0,"tecnica":"Greedy"}
```

**Estrategia:** ordenar por ratio valor/peso descendente y tomar cada paquete que entre. **Estructuras:** copia de lista, comparador, lista de seleccionados. **Tiempo:** O(n log n) por ordenar; selección O(n). **Espacio:** O(n). **No garantiza óptimo para mochila 0/1:** con capacidad 10 elige I4 + I2 (valor 90), mientras la solución óptima es I4 + I5 (valor 105).

## GET /api/seleccion/dinamica

**Propósito:** resolver exactamente la mochila 0/1 con pesos enteros no negativos. **Parámetro:** `capacidad` (entero). **Ejemplo:** `/api/seleccion/dinamica?capacidad=10`. **Respuesta comprobada:**

```json
{"elegidos":[{"id":"I4","nombre":"Item 4","peso":3.0,"valor":50.0,"nodoId":"D"},{"id":"I5","nombre":"Item 5","peso":7.0,"valor":55.0,"nodoId":"E"}],"valorTotal":105.0,"pesoTotal":10.0,"tecnica":"Programacion Dinamica"}
```

**Estructura:** tabla `dp[i][w]` de dimensión `(n+1) × (W+1)`; reconstrucción de los elementos elegidos. **Recurrencia:** `dp[i][w] = dp[i-1][w]` si el paquete i no entra; si entra, `max(dp[i-1][w], valor_i + dp[i-1][w-peso_i])`. **Tiempo:** O(nW), porque se llena cada celda una vez. **Espacio:** O(nW), para conservar la tabla y reconstruir la selección. Es pseudopolinomial en W.

**Ejemplo reducido de tabla DP** (solo paquetes I4: peso 3/valor 50 e I5: peso 7/valor 55):

| Paquetes disponibles / capacidad | 0 | 3 | 7 | 10 |
|---|---:|---:|---:|---:|
| Ninguno | 0 | 0 | 0 | 0 |
| I4 | 0 | 50 | 50 | 50 |
| I4, I5 | 0 | 50 | 55 | 105 |

Esta tabla es ilustrativa de dos paquetes, no la tabla completa de los cinco elementos.

## GET /api/grafo/rutas

**Propósito:** enumerar rutas simples entre depósitos con límites de distancia y saltos. **Parámetros:** `origen`, `destino`, `costoMaximo` (por defecto 1000), `saltosMaximos` (por defecto 10). **Ejemplo:** `/api/grafo/rutas?origen=A&destino=H&costoMaximo=30&saltosMaximos=5`. **Respuesta comprobada:** 9 rutas; entre ellas `A-B-C-D-F-H` con costo 20.0. **Estructuras:** recursión, arreglo de visitados, camino actual y lista de resultados. **Poda:** no explorar ramas con costo acumulado mayor que `costoMaximo` o saltos superiores a `saltosMaximos`. **Tiempo:** exponencial/factorial en el peor caso por la enumeración de caminos simples; las restricciones reducen el espacio explorado. **Espacio:** O(V) de estado de búsqueda, más O(R·V) para almacenar R rutas encontradas.

**Medición de nodos explorados con y sin poda:** no instrumentada en la implementación actual; no se reportan cifras inventadas. Para medirla se necesitaría agregar un contador de llamadas recursivas y comparar ambas variantes sobre el mismo grafo.

## GET /api/seleccion/repartir

**Propósito:** distribuir paquetes entre `k` camiones minimizando la carga máxima (*makespan*). **Parámetro:** `contenedores` (entero positivo). **Ejemplo:** `/api/seleccion/repartir?contenedores=3`. **Respuesta comprobada:**

```json
{"porContenedor":{"Camion 1":[{"id":"I1","nombre":"Item 1","peso":5.0,"valor":10.0,"nodoId":"A"},{"id":"I2","nombre":"Item 2","peso":4.0,"valor":40.0,"nodoId":"B"}],"Camion 2":[{"id":"I3","nombre":"Item 3","peso":6.0,"valor":30.0,"nodoId":"C"},{"id":"I4","nombre":"Item 4","peso":3.0,"valor":50.0,"nodoId":"D"}],"Camion 3":[{"id":"I5","nombre":"Item 5","peso":7.0,"valor":55.0,"nodoId":"E"}]},"cargaMaxima":9.0}
```

**Estructuras:** recursión, arreglo de cargas por camión, asignación parcial y mejor solución conocida. **Cota inferior:** `LB = max(cargaMaximaActual, pesoTotal/k)`. Es optimista porque ninguna solución final puede disminuir una carga ya asignada ni reducir el promedio de peso total por camión. **Poda:** descartar una rama cuando `LB >= mejorCarga`; también evitar estados simétricos de camiones con la misma carga. **Tiempo:** O(k^n · k²) como cota conservadora para esta implementación, debido a las k asignaciones posibles por paquete y las comprobaciones de cargas/simetrías en cada nodo. **Espacio:** O(n + k) de estado recursivo, más O(n + k) para el resultado.

**Prueba de optimalidad del ejemplo:** el peso total es 25 kg. Con 3 camiones, la carga máxima no puede ser menor que `25/3 ≈ 8.33 kg`. Como todos los paquetes pesan kilogramos enteros, la cota mínima es 9 kg. La solución encontrada alcanza exactamente 9 kg, luego es óptima.

---

## Pruebas

- Pruebas unitarias: `./mvnw test` en Linux/macOS o `.\mvnw.cmd clean test` en Windows.
- Archivo de pruebas: `src/test/java/uade/prog3/tpo/AlgoritmosTest.java` (13 pruebas para los 11 algoritmos), además de `GrafoTest.java`.
- Los endpoints de ordenamiento fueron comprobados por peso y por valor; los otros algoritmos fueron comprobados con los datos semilla.
- Los endpoints `/api/grafo/floyd` y `/api/grafo/ucs` pertenecen al scaffold original y no forman parte de los once algoritmos exigidos para esta entrega; sus métodos siguen sin implementar.
