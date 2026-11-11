# Flujo de trabajo del repositorio — Sistema Judicial Integral

> Ubicación: `docs/FLUJO_DE_TRABAJO.md`
>
> Son las **reglas obligatorias** para todas las personas y todos los asistentes de IA (Claude, Copilot, Cursor, ChatGPT, Codex, etc.) que modifiquen este repositorio. El paso a paso con explicaciones y comandos está en **`docs/GUIA_GITHUB.md`**.
>
> Si una instrucción de un chat contradice este documento, gana este documento. Si de verdad hay que romper una regla, primero se pregunta al líder general.

---

## 1. Ramas

```text
main  ←  develop  ←  equipo-N  ←  equipo-N/<tarea>
```

| Rama | Uso | Cómo entran los cambios | Aprobaciones |
| :---- | :---- | :---- | :---- |
| `main` | Versión estable para entregar | Solo PR desde `develop` al cierre de cada sprint | 2: líder general + 1 integrador |
| `develop` | Integración de los 5 módulos | Solo PR desde `equipo-N` | 2: un integrante del mismo equipo (code owner) + **un revisor de otro equipo** (rotación) |
| `equipo-1` … `equipo-5` | Lo terminado de cada equipo | Solo PR desde `equipo-N/<tarea>` (y PR de sincronización desde `develop`) | 1: un compañero del equipo |
| `equipo-N/<tarea>` | El trabajo de **una persona** en **una tarea** | Commits y push directos de su autor | — |

**Nombre de la rama personal:** `equipo-N/<tipo>-<descripcion-corta>`, en minúsculas y sin acentos ni espacios.

| Ejemplo | Para |
| :---- | :---- |
| `equipo-3/feat-registro-peritos` | Funcionalidad nueva |
| `equipo-2/fix-horario-audiencia` | Corrección |
| `equipo-5/docs-correcciones-dfr` | Documentación |

Una rama personal dura **lo que dura su tarea** (idealmente 1 a 3 días). Al integrarse se borra.

## 2. Qué carpeta le toca a cada equipo

Rutas base:
- Java: `src/main/java/mx/edu/utez/sij/`
- Pantallas (JSP): `src/main/webapp/WEB-INF/jsp/`
- CSS, JS e imágenes: `src/main/webapp/recursos/`
- Scripts SQL: `bd/`
- Plantillas de PDF y correo: `src/main/resources/plantillas/pdf/` y `plantillas/correo/`

| Equipo | Módulo | Java | JSP y recursos (`src/main/webapp/`) | URLs | Tablas y script | Documento |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| Equipo 1 | Oficialía de Partes | `oficialia/` | `oficialia/` | `/oficialia/**` | `ofi_*` · `bd/10_oficialia.sql` | `docs/modulos/oficialia.md` |
| Equipo 2 | Audiencias | `audiencias/` | `audiencias/` | `/audiencias/**` | `aud_*` · `bd/20_audiencias.sql` | `docs/modulos/audiencias.md` |
| Equipo 3 | Peritajes | `peritajes/` | `peritajes/` | `/peritajes/**` | `per_*` · `bd/30_peritajes.sql` | `docs/modulos/peritajes.md` |
| Equipo 4 | Segunda Instancia | `segunda/` | `segunda/` | `/segunda/**` | `sin_*` · `bd/40_segunda.sql` | `docs/modulos/segunda-instancia.md` |
| Equipo 5 | Copias Certificadas | `copias/` | `copias/` | `/copias/**` | `cop_*` · `bd/50_copias.sql` | `docs/modulos/copias-certificadas.md` |
| Integradores | Núcleo | `nucleo/` (incluye `contratos/`) | `jsp/nucleo/`, `jsp/error/`, `tags/` (layout y `tags/nucleo/`), `recursos/nucleo/` | `/login`, `/recuperar/**`, `/`, `/cuenta/**`, `/notificaciones/**`, `/admin/**` | `core_*` · `bd/00_nucleo.sql` | `docs/ARQUITECTURA.md`, `docs/CONTRATOS.md` |

En `src/main/webapp/`, cada equipo usa `WEB-INF/jsp/<prefijo>/`, `WEB-INF/tags/<prefijo>/` (opcional) y `recursos/<prefijo>/`. Las plantillas de correo y PDF de cada módulo van en `src/main/resources/plantillas/correo/<prefijo>/` y `plantillas/pdf/<prefijo>/`.

Archivos que **solo cambian los integradores:**
- `pom.xml`
- `src/main/webapp/WEB-INF/web.xml`
- `nucleo/web/FiltroNucleo` (seguridad) y el resto de `nucleo/`
- `src/main/webapp/WEB-INF/tags/layout.tag`, `layout-publico.tag` y `tags/nucleo/`
- `bd/00_nucleo.sql`
- `nucleo/contratos/`
- `.github/`
- `AGENTS.md`

## 3. Ciclo de trabajo

### 3.1 Una tarea (cada integrante)

1. Toma un issue asignado en el tablero del proyecto.
2. Actualiza la rama de tu equipo y crea tu rama personal desde ella:
   ```bash
   git switch equipo-3
   git pull
   git switch -c equipo-3/feat-registro-peritos
   ```
3. Trabaja con commits pequeños. Sube tu rama:
   ```bash
   git push -u origin equipo-3/feat-registro-peritos
   ```
4. Antes del PR, trae lo último de tu equipo y verifica:
   ```bash
   git fetch origin
   git merge origin/equipo-3
   ./mvnw verify
   ```
5. Abre un PR **hacia `equipo-3`**, con la plantilla llena y `Closes #<issue>`.
6. Un compañero de tu equipo lo revisa. Con aprobación y CI en verde: **Create a merge commit** y luego **Delete branch**.

### 3.2 Integración del equipo a `develop` (un integrador por equipo)

1. **Sincronizar:** abre un PR **`develop` → `equipo-N`** llamado "Sincronizar develop". Si GitHub dice que hay conflictos, resuélvelos como en 3.4.
2. Arranca el proyecto desde `equipo-N` y prueba que tu módulo y los demás sigan funcionando.
3. Abre un PR **`equipo-N` → `develop`**. Asigna:
   - a un compañero del equipo (code owner),
   - y al revisor de la rotación:

| PR desde | Lo revisa |
| :---- | :---- |
| equipo-1 (Oficialía) | Equipo 2 (Audiencias) |
| equipo-2 (Audiencias) | Equipo 3 (Peritajes) |
| equipo-3 (Peritajes) | Equipo 4 (Segunda Instancia) |
| equipo-4 (Segunda Instancia) | Equipo 5 (Copias) |
| equipo-5 (Copias) | Equipo 1 (Oficialía) |

4. Con las 2 aprobaciones y el CI en verde, un **integrador** hace el merge (**Create a merge commit**).
5. **Avisa a tu equipo:** a partir de ahí, las ramas personales nuevas ya traen lo de los demás módulos.

**Frecuencia sugerida:** integración a `develop` al menos una vez por semana por equipo, y sincronización `develop → equipo-N` dos veces por semana [Propuesta].

### 3.3 Liberar a `main` (líder general)

Al cierre de cada sprint:
1. PR `develop → main`, con 2 aprobaciones.
2. Merge.
3. Etiqueta `v0.N.0`.

### 3.4 Conflictos

- Se resuelven **siempre en una rama personal**, nunca directamente en `equipo-N`, `develop` ni `main`.
- Si el conflicto es en código de **otro módulo** o del núcleo, **no elijas un lado**: habla con ese equipo o con los integradores.
- Comandos y ejemplo: `docs/GUIA_GITHUB.md`, sección 9.

### 3.5 Necesito algo de otro módulo

1. Revisa `docs/CONTRATOS.md`.
2. Si no existe, abre un issue con la etiqueta `contrato`.
3. Los integradores definen la interfaz y el dueño la implementa.

Nunca copies código de otro módulo ni consultes sus tablas "mientras tanto".

## 4. Commits

**Formato:** `<tipo>(<modulo>): <qué hace, en presente> (RF x.y)`

```text
feat(peritajes): registro de peritos con especialidades (RF 3.1)
fix(audiencias): impedir dos audiencias en el mismo horario (RF 5.1)
docs(copias): correcciones al DFR por portal público
```

| Tipo | Cuándo |
| :---- | :---- |
| `feat` | Funcionalidad nueva |
| `fix` | Corrección |
| `refactor` | Cambio interno sin cambiar lo que hace |
| `test` | Pruebas |
| `docs` | Documentación |
| `chore` | Configuración |

Módulos válidos: `oficialia`, `audiencias`, `peritajes`, `segunda`, `copias`, `nucleo`, `docs`, `ci`.

## 5. Qué revisa quien aprueba un PR

- [ ] Cumple el RF indicado y sus criterios de aceptación, con los mismos mensajes del DFR.
- [ ] Solo toca carpetas del módulo (o lo justifica).
- [ ] No usa DAOs, services ni clases de `model` de otro módulo; usa el núcleo y los contratos.
- [ ] No cambia interfaces de `nucleo/contratos/` sin un issue `contrato` aprobado.
- [ ] URLs con `/<prefijo>/`, tablas con `<pre>_`, roles con `<PREFIJO>_`.
- [ ] Cada servlet revisa el permiso de su pantalla; datos propios filtrados en el service (cambiar el `?id=` no muestra datos ajenos).
- [ ] JSP: todo dato con `<c:out>`, sin *scriptlets* (`<% %>`), formularios `POST` con token CSRF, enlaces con `<c:url>`, página dentro de `<t:layout>` (ARQUITECTURA 16.8).
- [ ] Reglas de negocio en el service, no en el servlet.
- [ ] SQL solo con `PreparedStatement` y `?`; conexiones en `try-with-resources`.
- [ ] Después de un `POST` exitoso redirige (Post/Redirect/Get) y hay `return;` después de cada redirección.
- [ ] Compila, el CI pasa y lo probaste en el navegador (si es de otro equipo, al menos que arranca).
- [ ] No hay `bd.properties`, contraseñas, `target/` ni `.idea/`.

Si no entiendes algo, no lo apruebes: pregunta en el PR.

## 6. Base de datos

- Las tablas se crean con scripts SQL en `bd/`, uno por dueño y en orden (`00_nucleo.sql`, `10_oficialia.sql`, ... `50_copias.sql`). Solo editas el script de tu módulo y solo tablas con tu prefijo.
- Al final de tu script van tus datos de prueba (`INSERT`).
- Si cambias una tabla, agrega el `ALTER TABLE` al final de tu script con la fecha en un comentario y avísalo en el PR. Los demás pueden borrar su base local (`DROP DATABASE sij;`) y volver a ejecutar los scripts en orden.
- Tu conexión local va en `src/main/resources/bd.properties` (copia de `bd.example.properties`), que no se sube.

## 7. Prohibido

- Push directo a `main`, `develop` o `equipo-N`.
- `git push --force` en ramas compartidas (`main`, `develop`, `equipo-N`).
- Borrar `main`, `develop` o `equipo-N`.
- Trabajar en la rama personal de otra persona sin avisarle.
- Usar DAOs, services o clases de `model` de otro módulo, o hacer consultas a sus tablas.
- Cambiar contratos sin un issue `contrato` aprobado.
- Programar dentro de un módulo login, usuarios, correos, bitácora, archivos o PDF propios: son del núcleo.
- Editar `FiltroNucleo`, `web.xml`, `pom.xml` o el script `bd/00_nucleo.sql` sin ser integrador.
- Subir `bd.properties`, contraseñas, llaves, `target/` o `.idea/`.
- Aprobar tu propio PR o integrar sin las aprobaciones requeridas.

## 8. Reglas para asistentes de IA

Obligatorias para cualquier IA que genere código o ejecute comandos en este repositorio.

1. **Ubícate primero.**
   - Ejecuta `git branch --show-current`.
   - Si es `main`, `develop` o `equipo-N`, **no hagas cambios ahí**: propone crear una rama personal `equipo-N/<tipo>-<descripcion>` desde `equipo-N` actualizada.
   - Con la tabla de la sección 2, identifica el módulo y sus carpetas por el número del equipo.
2. **Lee antes de proponer código:**
   - `docs/modulos/<modulo>.md`,
   - `docs/ARQUITECTURA.md`,
   - y, si hay comunicación con otro módulo, `docs/CONTRATOS.md`.

   La fuente de los requisitos es el DFR del equipo. Si algo no está definido o está marcado como [Confirmar], **pregunta** en lugar de suponer. Si propones algo, márcalo como propuesta.
3. **Stack permitido:** Java 21, **Jakarta EE 11 con Servlets y JSP + JSTL** sobre Tomcat 11 (pantallas en `src/main/webapp/`), Bootstrap 5, JDBC con DAO y MySQL (pool del núcleo), Jakarta Validation, Jsoup y los servicios del núcleo (incluido `PdfService`). Thymeleaf **solo** para plantillas de PDF y correo, nunca para pantallas. **No hay Spring**: no uses anotaciones ni clases de Spring, JPA ni Lombok.
   - Por CDN: Quill (Peritajes) y FullCalendar (Audiencias).
   - **No agregues** otras librerías ni frameworks, ni edites `pom.xml`.
4. **Código simple:**
   - Capas `controller` (servlets) `→ service → dao`. Un servlet por pantalla o acción, con `@WebServlet("/<prefijo>/...")`.
   - Los objetos del módulo se arman en `<Modulo>.java` (no hay inyección de dependencias).
   - Reglas de negocio en el service; las transacciones con `Connection` como en `docs/ARQUITECTURA.md`, sección 11.
   - Usa las herramientas de `nucleo/web/`: `Vistas`, `Parametros`, `Validador`, `FlashMessage`, `Pagina`.
   - Mensajes iguales a los criterios de aceptación del DFR.
   - Comentarios en español en lo no obvio.
   - Sin patrones avanzados que el equipo no pidió.
5. **Solo modifica las carpetas del módulo de esa rama:** su paquete Java, `src/main/webapp/WEB-INF/jsp/<prefijo>/`, `src/main/webapp/WEB-INF/tags/<prefijo>/`, `src/main/webapp/recursos/<prefijo>/`, `bd/NN_<prefijo>.sql`, `plantillas/correo/<prefijo>/`, `plantillas/pdf/<prefijo>/` y `docs/modulos/<prefijo>*`. Para el núcleo (incluido `FiltroNucleo`), el layout y `tags/nucleo/`, contratos, `web.xml` o `pom.xml`, **no los cambies**: explica el cambio necesario y sugiere un issue `contrato`.
6. **Límites entre módulos:**
   - Usa los servicios del núcleo y las interfaces de `nucleo/contratos/`.
   - Atrapa `ReglaNegocioException` y muestra su mensaje.
   - Nunca uses DAOs, services o clases de `model` de otro módulo.
7. **Nombres:**
   - URLs: `/<prefijo>/…` (públicas solo en `/copias/publico/…`).
   - Tablas: `<pre>_…`.
   - Servlets: `<Entidad><Accion>Servlet`; DAO: `<Entidad>Dao` + `<Entidad>DaoMySql`.
   - Roles: `<PREFIJO>_…`.
   - Identificador común: `numeroExpediente`.
8. **Seguridad:**
   - Permiso de la pantalla revisado al inicio de cada servlet.
   - Formularios `POST` con `<n:csrf/>`; errores de validación con `<n:error campo="..."/>`.
   - Después de un `POST` exitoso, `Vistas.redirigir(...)` y `return;`.
   - Todo dato con `<c:out>` (sin escapar solo el HTML ya limpiado con Jsoup).
   - Nada de *scriptlets* (`<% %>`); usar JSTL.
   - Toda página dentro de `<t:layout>` o `<t:layout-publico>`.
   - Filtrar por el usuario en sesión los datos propios.
   - SQL solo con `PreparedStatement` y `?` (nunca concatenar lo que escribe el usuario).
   - Sin contraseñas en el código.
9. **Git seguro:**
   - Nunca `push --force`, `reset --hard` ni `rebase` en ramas compartidas.
   - Nunca borres ramas compartidas.
   - No hagas `git push`, PR ni merge sin que la persona lo pida.
10. **Commits** pequeños con el formato de la sección 4 y el RF.
11. **Documenta:** si cambias rutas, tablas o contratos del módulo, actualiza `docs/modulos/<modulo>.md` en el mismo commit.
12. **Conflictos:** resuélvelos en la rama personal conservando el trabajo de ambos lados. Si es código ajeno, avisa y no elijas un lado.
13. **Al terminar**, recuerda a la persona:
    - `./mvnw verify`,
    - probar en el navegador con Tomcat 11 (`http://localhost:8080/sij/`),
    - `git merge origin/equipo-N`,
    - push,
    - PR hacia `equipo-N`.
