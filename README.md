# Proyecto realizado por Juan Ignacio Diaz Salla

# TPO Programación III — Red logística

## Descripción del proyecto

Aplicación REST desarrollada en **Java, Spring Boot y Neo4j** para representar una red de logística y resolver problemas de recorridos, rutas, conexiones mínimas, selección de paquetes y distribución de carga.

- **Depósitos:** vértices del grafo (identificadores A–H en los datos de ejemplo).
- **Rutas:** aristas ponderadas. **El costo representa distancia en kilómetros**, no dinero ni tiempo.
- **Paquetes:** elementos con identificador, nombre, peso en kilogramos, valor y depósito asociado.
- **Camiones/contenedores:** recursos de carga usados para seleccionar o repartir paquetes. En la distribución por Branch and Bound, el objetivo es minimizar la mayor carga, no verificar capacidades individuales de camiones.

Los datos se almacenan en Neo4j y se cargan en memoria antes de ejecutar los algoritmos. La lógica algorítmica se encuentra en `src/main/java/uade/prog3/tpo/algorithm/`, separada de los controladores y repositorios.

## Tecnologías y requisitos

- Java **17 o superior** (el `pom.xml` establece Java 17).
- Maven Wrapper incluido (`mvnw` / `mvnw.cmd`).
- Spring Boot y Spring Data Neo4j.
- Neo4j local o una instancia accesible.
- JUnit 5 para las pruebas.

## Configuración y ejecución en Windows

1. Iniciar Neo4j y comprobar que está disponible el puerto Bolt (habitualmente `7687`).
2. Configurar `src/main/resources/application.properties` para **no guardar contraseñas**. La configuración recomendada es:

   ```properties
   spring.application.name=tpo-prog3
   spring.neo4j.uri=${NEO4J_URI:bolt://localhost:7687}
   spring.neo4j.authentication.username=${NEO4J_USER:neo4j}
   spring.neo4j.authentication.password=${NEO4J_PASSWORD}
   app.seed.enabled=true
   logging.level.uade.prog3.tpo=INFO
   ```

3. Abrir PowerShell en la raíz del proyecto y definir la contraseña **solo en el entorno local**:

   ```powershell
   $env:NEO4J_PASSWORD="TU_CONTRASENA_LOCAL"
   ```

   Si hace falta, también se pueden establecer `$env:NEO4J_URI` y `$env:NEO4J_USER`. No subir secretos al repositorio.

4. Ejecutar las pruebas y luego iniciar la aplicación:

   ```powershell
   .\mvnw.cmd clean test
   .\mvnw.cmd spring-boot:run
   ```

5. Verificar la carga del grafo en `http://localhost:8080/api/grafo/resumen`.

Con `app.seed.enabled=true`, el proyecto utiliza datos de ejemplo: ocho depósitos, doce rutas dirigidas y cinco paquetes. Para trabajar con datos propios, revisar la carga inicial y configurar la opción según corresponda.

## Algoritmos implementados

| Algoritmo | Aplicación al dominio | Endpoint GET |
|---|---|---|
| BFS | Recorrer depósitos por niveles | `/api/grafo/bfs?origen=A` |
| DFS | Recorrer depósitos en profundidad | `/api/grafo/dfs?origen=A` |
| Dijkstra | Encontrar la ruta más corta en km | `/api/grafo/dijkstra?origen=A&destino=H` |
| Prim | Conectar todos los depósitos con costo mínimo | `/api/grafo/prim?origen=A` |
| Kruskal | Obtener un árbol de expansión mínima | `/api/grafo/kruskal` |
| QuickSort | Ordenar paquetes por peso, valor o ratio | `/api/seleccion/quicksort?criterio=peso` |
| MergeSort | Ordenar paquetes de forma estable | `/api/seleccion/mergesort?criterio=valor` |
| Greedy | Seleccionar paquetes por valor/peso | `/api/seleccion/greedy?capacidad=10` |
| Mochila 0/1 (PD) | Maximizar valor sin exceder capacidad entera | `/api/seleccion/dinamica?capacidad=10` |
| Backtracking | Enumerar rutas simples bajo restricciones | `/api/grafo/rutas?origen=A&destino=H&costoMaximo=30&saltosMaximos=5` |
| Branch and Bound | Distribuir paquetes minimizando la carga máxima | `/api/seleccion/repartir?contenedores=3` |

**Importante:** Prim y Kruskal trabajan con la versión **no dirigida** del grafo. Los recorridos y Dijkstra utilizan la versión dirigida. Los métodos de Floyd-Warshall y UCS pueden seguir presentes en el scaffold, pero **no forman parte de los once algoritmos implementados para esta entrega**; sus endpoints no deben presentarse como operativos.

La descripción de entradas, salidas, complejidades y ejemplos está en [`docs/ENDPOINTS.md`](docs/ENDPOINTS.md).

## Casos de prueba representativos

- **Dijkstra A → H:** ruta `A → B → C → D → F → H`, distancia **20 km**.
- **Prim y Kruskal:** ambos obtienen un árbol de siete aristas con costo total **27 km**.
- **Ordenamiento por peso:** `I4, I2, I1, I3, I5`.
- **Ordenamiento por valor:** `I1, I3, I2, I4, I5`.
- **Greedy, capacidad 10 kg:** `I4 + I2`, peso **7 kg**, valor **90**.
- **Mochila 0/1, capacidad 10 kg:** `I4 + I5`, peso **10 kg**, valor **105**. Demuestra que Greedy no siempre alcanza el óptimo.
- **Backtracking A → H, hasta 30 km y 5 saltos:** devuelve nueve rutas válidas con los datos de ejemplo.
- **Branch and Bound, tres camiones:** distribución de los cinco paquetes con cargas **9, 9 y 7 kg**; carga máxima óptima **9 kg**.

## Pruebas

Ejecutar:

```powershell
.\mvnw.cmd clean test
```

El proyecto contiene `GrafoTest.java` para la estructura del grafo y `AlgoritmosTest.java` para los once algoritmos, incluyendo comprobaciones de ordenamiento, rutas, selección y reparto. En la última ejecución local informada, Maven terminó con **BUILD SUCCESS**.

Las pruebas unitarias no requieren ejecutar los endpoints. Para probar la API REST sí debe estar iniciado Spring Boot y disponible Neo4j.

## Estructura del proyecto

```text
src/main/java/uade/prog3/tpo/
  algorithm/       Implementaciones de los algoritmos
  controller/      Endpoints REST
  model/           Entidades de depósitos, rutas y paquetes
  repository/      Persistencia en Neo4j
  service/         Construcción del grafo en memoria
  seed/            Datos de ejemplo
src/test/java/uade/prog3/tpo/
  GrafoTest.java
  AlgoritmosTest.java
docs/
  ENDPOINTS.md     Contratos y análisis de complejidad
  api.http         Solicitudes HTTP del scaffold
```

## Uso de herramientas de IA

Se utilizó **ChatGPT** como herramienta de apoyo durante el desarrollo para explicar algoritmos, proponer implementaciones, revisar errores, diseñar casos de prueba y redactar documentación. El código se incorporó al proyecto y se comprobó mediante compilación, pruebas automatizadas y solicitudes HTTP a los endpoints. El uso de IA no reemplaza la comprensión, revisión ni responsabilidad del equipo sobre la solución presentada.

## Verificaciones antes de entregar

- Ejecutar ` .\mvnw.cmd clean test ` y confirmar `BUILD SUCCESS`.
- Revisar que `application.properties`, archivos `.env`, commits y documentación **no incluyan contraseñas reales**.
- Confirmar que `AlgoritmosTest.java` y `docs/ENDPOINTS.md` estén incluidos en Git.
- No afirmar que Floyd-Warshall o UCS están implementados.
- Completar los nombres de integrantes y datos de comisión si la cátedra los requiere.
- Completar, si la rúbrica lo exige, la medición comparativa de nodos explorados por Backtracking con y sin poda.
