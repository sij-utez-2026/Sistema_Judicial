# Instrucciones para asistentes de IA — Sistema Judicial Integral

Este repositorio es **un solo proyecto web Jakarta EE 11 con Servlets y JSP (MVC, sin Spring)** que corre en **Tomcat 11**, con las pantallas en `src/main/webapp/` y 5 módulos, cada uno de un equipo:

| Equipo | Módulo | Carpeta |
| :---- | :---- | :---- |
| 1 | Oficialía de Partes | `oficialia/` |
| 2 | Audiencias | `audiencias/` |
| 3 | Peritajes | `peritajes/` |
| 4 | Segunda Instancia | `segunda/` |
| 5 | Copias Certificadas | `copias/` |

Además hay un **núcleo común** que mantienen los integradores:
- en Java, `nucleo/`: `FiltroNucleo` (seguridad y CSRF), herramientas para servlets, conexión a la base, login, usuarios, notificaciones, bitácora, archivos, PDF y contratos;
- en `src/main/webapp/`: `WEB-INF/web.xml`, el layout y los componentes de `WEB-INF/tags/`.

Los integrantes son estudiantes: el código debe ser simple y fácil de entender.

Antes de escribir código o ejecutar comandos de Git:

1. Lee **`docs/FLUJO_DE_TRABAJO.md`** (la sección 8 es obligatoria), **`docs/ARQUITECTURA.md`** y el documento del módulo en `docs/modulos/`. Si hay comunicación con otro módulo, lee **`docs/CONTRATOS.md`**. Si vas a pasar código hecho con Spring, lee **`docs/MIGRACION_SPRING_A_SERVLETS.md`**.
2. Ejecuta `git branch --show-current`.
   - Solo se trabaja en ramas personales `equipo-N/<tipo>-<descripcion>`.
   - Si estás en `main`, `develop` o `equipo-N`, **no hagas cambios**: propone crear la rama personal.
3. Modifica **solo** las carpetas del módulo del equipo N: su paquete Java, `src/main/webapp/WEB-INF/jsp/<prefijo>/`, `src/main/webapp/recursos/<prefijo>/`, su script `bd/NN_<prefijo>.sql` y sus plantillas de PDF y correo. No toques el núcleo, `nucleo/contratos/`, `FiltroNucleo`, `web.xml` ni `pom.xml`: explica el cambio y sugiere un issue `contrato`.
4. Solo tecnologías del proyecto: Java 21, Servlets 6.1, JSP + JSTL, Bootstrap 5, JDBC con DAO y MySQL (con `ConexionBD` del núcleo), Jakarta Validation, Jsoup y los servicios del núcleo. Thymeleaf solo para plantillas de PDF y correo. **No uses Spring, JPA ni Lombok** y no agregues otras librerías.
5. Servlets: uno por pantalla o acción con `@WebServlet("/<prefijo>/...")`; el id va como parámetro (`?id=5`). Usa `Vistas`, `Parametros`, `Validador`, `FlashMessage` y `Pagina` de `nucleo/web/`. Después de un `POST` exitoso, `Vistas.redirigir(...)` y `return;`. SQL solo con `PreparedStatement` y `?`, conexiones en `try-with-resources`.
6. En las JSP: todo dato con `<c:out>`, sin *scriptlets*, formularios `POST` con `<n:csrf/>`, errores con `<n:error campo="..."/>`, enlaces con `<c:url>` y la página dentro de `<t:layout modulo="<prefijo>" seccion="...">`. Usa los componentes de `src/main/webapp/WEB-INF/tags/nucleo/` (`<n:migas>`, `<n:encabezado>`, `<n:tarjeta>`, `<n:estado>`, `<n:paginacion>`, `<n:confirmar>`...) y las clases de `estilos.css`; no copies el menú ni el encabezado. Las opciones del menú lateral van en `src/main/webapp/WEB-INF/jsp/<prefijo>/menu.jsp`. Guía: `docs/FRONT_END.md` y `docs/ARQUITECTURA.md`, sección 16.
7. Nunca uses DAOs, services ni clases de `model` de otro módulo. Usa el núcleo y los contratos.
8. Nunca hagas `push --force`, `rebase` o `reset --hard` en ramas compartidas, ni push, PR o merge sin que la persona lo pida, ni subas `bd.properties`, contraseñas o llaves.
9. Si un requisito no está en el DFR del módulo o está marcado [Confirmar], pregunta; no lo inventes.

Comandos:

- Compilar, probar y generar `target/sij.war`: `./mvnw verify`
- Ejecutar: desde IntelliJ con Tomcat 11 (artefacto `sij:war exploded`, contexto `/sij`) → `http://localhost:8080/sij/`
