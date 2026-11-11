# Migrar de Spring Boot a Servlets (Jakarta EE)

Guía para los equipos que ya tenían código hecho con Spring Boot. El ejemplo completo y probado es **Peritajes** (catálogo de peritos): compara su versión anterior en `develop` con la nueva.

> Se sigue usando **MVC**: los controllers pasan a ser **servlets**, los services se quedan casi igual, los repositorios pasan a ser **DAO** y las JSP casi no cambian. Arquitectura completa: `docs/ARQUITECTURA.md` (v6.0).

---

## 1. Antes de empezar (importante para equipos con avance, como Copias)

Cuando tu rama de equipo traiga lo nuevo de `develop`, **tu módulo dejará de compilar** hasta que lo migres: el `pom.xml` ya no tiene Spring. Por eso se hace todo en una sola rama personal:

```bash
git switch equipo-5
git pull
git switch -c equipo-5/chore-migracion-servlets
git merge origin/develop
```

1. **Carpetas movidas.** `front-end/WEB-INF/` ahora es `src/main/webapp/WEB-INF/` y `front-end/recursos/` es `src/main/webapp/recursos/`. Git **no** mueve los archivos que tu equipo creó: se quedan en `front-end/`. Muévelos tú (ejemplo de Copias, probado):
   ```bash
   git mv front-end/WEB-INF/jsp/copias/* src/main/webapp/WEB-INF/jsp/copias/
   git mv front-end/WEB-INF/tags/copias src/main/webapp/WEB-INF/tags/copias
   git mv front-end/recursos/copias/* src/main/webapp/recursos/copias/
   ```
   Al terminar, borra la carpeta `front-end/` (ya solo quedan carpetas vacías).
2. **Conflictos esperados** (en Copias salen estos cuatro):
   - `inicio.jsp` y `menu.jsp` de tu módulo: conserva **tu** contenido y solo aplica los cambios de la sección 3.
   - `InicioCopiasController.java` ("deleted in ... and modified in HEAD"): ya existe `InicioCopiasServlet`; pasa ahí lo que agregaste al controller y borra el controller (`git rm`).
   - `docs/modulos/copias-certificadas.md`: conserva tu versión y cambia solo las rutas (`front-end/` → `src/main/webapp/`) y la fila de tecnologías.
3. Migra el Java (sección 2), las JSP (sección 3) y las pruebas (sección 4).
4. Revisa que no quede nada de Spring (sección 5), compila con `./mvnw verify`, prueba en Tomcat 11 y abre el PR hacia `equipo-5`.

---

## 2. Java: equivalencias

| Spring Boot | Servlets (este proyecto) |
| :---- | :---- |
| `@Controller` + `@RequestMapping` + `@GetMapping` / `@PostMapping` | **Un servlet por ruta**: `@WebServlet("/copias/solicitudes/nueva")` con `doGet` y `doPost` |
| `@PathVariable Long id` (`/copias/solicitudes/{id}`) | Parámetro: `/copias/solicitudes/detalle?id=5` → `Long id = Parametros.numero(request, "id");` |
| `@RequestParam(defaultValue = "0") int page` | `int page = Parametros.entero(request, "page", 0);` |
| `@RequestParam(required = false) String q` | `String q = Parametros.texto(request, "q");` (vacío = `null`) |
| `@RequestParam EstadoSolicitud estado` (un `enum`) | Leerlo como texto y convertirlo con un `try` de `EstadoSolicitud.valueOf(...)` (si no es válido, `null`) |
| `Model model` + `model.addAttribute("x", valor)` | `request.setAttribute("x", valor);` |
| `return "copias/solicitudes/lista";` | `Vistas.mostrar(request, response, "copias/solicitudes/lista");` |
| `return "redirect:/copias/pagos/" + id;` | `Vistas.redirigir(request, response, "/copias/pagos/pago?id=" + id); return;` |
| `RedirectAttributes flash` + `addFlashAttribute("exito", ...)` / `("exitoDetalle", ...)` | `FlashMessage.exito(request, "Solicitud registrada", detalle);` |
| `addFlashAttribute("error", ...)` | `FlashMessage.error(request, mensaje);` |
| `addFlashAttribute("otraCosa", valor)` | `FlashMessage.agregar(request, "otraCosa", valor);` |
| `@Valid @ModelAttribute("form") Form form, BindingResult resultado` | Llenar el form con `Parametros` y `Map<String, String> errores = Validador.validar(form);`. Si no está vacío: `request.setAttribute("errores", errores)` y `request.setAttribute("form", form)` |
| `resultado.rejectValue("campo", "codigo", "Mensaje")` | `errores.put("campo", "Mensaje");` (usa un `LinkedHashMap` propio si lo vas a modificar) |
| `throw new ResponseStatusException(HttpStatus.NOT_FOUND, ...)` | `Vistas.noEncontrado(response); return;` |
| `throw new ResponseStatusException(HttpStatus.FORBIDDEN, ...)` | `response.sendError(HttpServletResponse.SC_FORBIDDEN); return;` |
| `MultipartFile ine` | `@MultipartConfig(maxFileSize = ...)` en el servlet y `Part ine = request.getPart("ine");` (`ine.getSize()`, `ine.getSubmittedFileName()`, `ine.getInputStream()`). El formulario lleva `enctype="multipart/form-data"` |
| `Page<T>` / `PageImpl` | `Pagina<T>` del núcleo (mismos getters: `content`, `number`, `totalElements`...). `Pagina.de(lista, page, size)` en memoria |
| `@Service`, `@Component`, `@Repository` | Se quitan. Los objetos se arman en una clase del módulo (ver abajo) |
| Inyección por constructor en controllers | `private final SolicitudCopiaService solicitudService = Copias.solicitudService();` |
| Interfaces `...Repository` (estilo Spring Data) | Se renombran a `...Dao` en el paquete `dao`; la versión en memoria se queda como `...DaoMemoria` |
| `@Transactional` | Se quita. Con MySQL, las transacciones van con `Connection` (ARQUITECTURA, sección 11) |
| `CommandLineRunner` (`DatosIniciales<Modulo>`) | Clase normal con un método `cargar()` que llama un `@WebListener` (`Arranque<Modulo>`) |
| `@ControllerAdvice` con `@ModelAttribute` (datos para todas las pantallas del módulo) | Un filtro del módulo: `@WebFilter("/copias/*")` que hace `request.setAttribute(...)` y `cadena.doFilter(...)` |
| `@ModelAttribute` en un controller para revisar el rol antes de cada ruta | Llamar la revisión al inicio de `doGet` y `doPost`: `if (!acceso.exigirRol(request, response, RolesCopias.PERSONAL)) return;` |
| `RequestContextHolder` (leer la sesión desde un service) | Pasar el `HttpServletRequest` (o la `HttpSession`) como parámetro: `request.getSession().getAttribute(...)`. Los services **no** deberían depender de la sesión: el servlet lee el dato y se lo pasa |
| `@SpringBootTest`, `@Autowired` en pruebas | Pruebas JUnit normales: `new SolicitudCopiaService(new SolicitudCopiaDaoMemoria(), ...)` |

### 2.1 La clase que arma el módulo

En lugar de que Spring cree e inyecte los objetos, el módulo los crea **una sola vez** en una clase. Ejemplo para Copias:

```java
package mx.edu.utez.sij.copias;

/** Arma los DAO y services de Copias. Los servlets piden aquí lo que necesitan. */
public final class Copias {

    // DAO (hoy en memoria; con MySQL se cambia cada "new ...Memoria()" por "new ...MySql()")
    private static final SolicitudCopiaDao SOLICITUD_DAO = new SolicitudCopiaDaoMemoria();
    private static final PagoDao PAGO_DAO = new PagoDaoMemoria();
    // ...

    // Services: en el orden en que se necesitan
    private static final SesionTemporal SESION = new SesionTemporal();
    private static final HistorialService HISTORIAL = new HistorialService(HISTORIAL_DAO, SESION);
    private static final SolicitudCopiaService SOLICITUDES = new SolicitudCopiaService(SOLICITUD_DAO, HISTORIAL, ...);
    // ...

    private Copias() {
    }

    public static SolicitudCopiaService solicitudService() { return SOLICITUDES; }
    public static HistorialService historialService() { return HISTORIAL; }
    // ... un método por cada service que usen los servlets
}
```

Si dos services se necesitan entre sí (A usa B y B usa A), es señal de que hay que separar algo: pregunta antes de hacer trucos.

### 2.2 Ejemplo: un controller de Copias pasado a servlets

**Antes** (`SolicitudController`, resumido):

```java
@Controller
@RequestMapping("/copias/solicitudes")
public class SolicitudController {
    @ModelAttribute
    public void soloTrabajador() { acceso.exigirRol(RolesCopias.PERSONAL); }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        SolicitudCopia solicitud = solicitudService.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron registros"));
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("historial", historialService.historial(id));
        return "copias/solicitudes/detalle";
    }
}
```

**Después** (un servlet por ruta; `SolicitudListarServlet`, `SolicitudNuevaServlet` y `SolicitudDetalleServlet`):

```java
@WebServlet("/copias/solicitudes/detalle")
public class SolicitudDetalleServlet extends HttpServlet {

    private final SolicitudCopiaService solicitudService = Copias.solicitudService();
    private final HistorialService historialService = Copias.historialService();
    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!acceso.exigirRol(request, response, RolesCopias.PERSONAL)) {
            return;                                     // ya respondió 403
        }
        Long id = Parametros.numero(request, "id");
        SolicitudCopia solicitud = id == null ? null : solicitudService.buscar(id).orElse(null);
        if (solicitud == null) {
            Vistas.noEncontrado(response);
            return;
        }
        request.setAttribute("solicitud", solicitud);
        request.setAttribute("historial", historialService.historial(id));
        Vistas.mostrar(request, response, "copias/solicitudes/detalle");
    }
}
```

`AccesoTemporal` cambia para recibir la petición y responder 403 él mismo:

```java
public boolean exigirRol(HttpServletRequest request, HttpServletResponse response, String rol) throws IOException {
    if (!rol.equals(sesion.rol(request))) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
    return true;
}
```

Y `SesionTemporal` lee la sesión de la petición en lugar de `RequestContextHolder`:

```java
public String rol(HttpServletRequest request) {
    HttpSession sesion = request.getSession(false);
    Object rol = sesion == null ? null : sesion.getAttribute(ATRIBUTO_ROL);
    return rol == null ? RolesCopias.PERSONAL : rol.toString();
}
```

Si un service usaba `sesion.rol()` o `sesion.esAdmin()`, el servlet lo lee y se lo pasa como parámetro.

### 2.3 Ejemplo: `@ControllerAdvice` → filtro del módulo

```java
@WebFilter("/copias/*")
public class FiltroCopias extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws IOException, ServletException {
        request.setAttribute("coloresCopias", Copias.coloresService().actuales());
        request.setAttribute("esAdminCopias", Copias.sesion().esAdmin(request));
        cadena.doFilter(request, response);
    }
}
```

Solo pone datos para las vistas. No revisa permisos ni CSRF: eso ya lo hace `FiltroNucleo`.

### 2.4 Ejemplo: subir un archivo

```java
@WebServlet("/copias/solicitudes/nueva")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)     // 5 MB; si se pasa, FiltroNucleo muestra la página 413
public class SolicitudNuevaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part ine = request.getPart("ine");          // null si no viene
        ...
    }
}
```

El service que antes recibía `MultipartFile` recibe `Part` (o mejor: nombre, tamaño y `InputStream`, para no depender de la web).

> **CSRF con archivos:** en un formulario `multipart/form-data` el token va en el formulario como siempre (`<n:csrf/>`); `FiltroNucleo` lo lee igual. Si el archivo pasa del `maxFileSize`, `FiltroNucleo` responde con la página **413** ("Archivo demasiado grande"). Para avisar antes de enviar, usa `<n:archivo ... maxMb="5"/>`, que revisa el tamaño en el navegador.

---

## 3. JSP: equivalencias

Las JSP solo cambian en los formularios de Spring, en `sec:authorize` y en los enlaces con id en la ruta.

| Spring | HTML normal |
| :---- | :---- |
| `<%@ taglib prefix="form" ... %>` y `<%@ taglib prefix="spring" ... %>` | Se borran |
| `<form:form method="post" modelAttribute="solicitudForm" servletRelativeAction="/copias/solicitudes/nueva">` | `<form method="post" action="<c:url value='/copias/solicitudes/nueva'/>" novalidate>` y en la línea siguiente `<n:csrf/>` |
| `</form:form>` | `</form>` |
| `<form:label path="nombre" cssClass="form-label">` | `<label for="nombre" class="form-label">` |
| `<form:input path="nombre" cssClass="form-control" cssErrorClass="form-control is-invalid"/>` | `<input id="nombre" name="nombre" class="form-control${not empty errores.nombre ? ' is-invalid' : ''}" value="<c:out value='${solicitudForm.nombre}'/>">` |
| `<form:errors path="nombre" cssClass="invalid-feedback" element="div"/>` | `<n:error campo="nombre"/>` |
| `<form:hidden path="numeroExpediente"/>` | `<input type="hidden" name="numeroExpediente" value="<c:out value='${solicitudForm.numeroExpediente}'/>">` |
| `<form:textarea path="motivo"/>` | `<textarea id="motivo" name="motivo" class="form-control${...}"><c:out value="${form.motivo}"/></textarea>` |
| `<form:select path="estado"><form:options items="${estados}"/></form:select>` | `<select name="estado" class="form-select"><c:forEach var="e" items="${estados}"><option value="${e}" ${form.estado == e ? 'selected' : ''}><c:out value="${e}"/></option></c:forEach></select>` |
| `<form:checkbox path="materiasIds" value="${m.id}"/>` | `<input type="checkbox" name="materiasIds" value="${m.id}" ${form.materiasIds.contains(m.id) ? 'checked' : ''}>` |
| `<spring:bind path="campo"><c:set var="hayError" value="${status.error}"/></spring:bind>` | `<c:set var="hayError" value="${not empty errores.campo}"/>` |
| `<sec:authorize access="hasRole('COPIAS_ADMIN')">` | `<c:if test="${esAdminCopias}">` (el dato lo pone el servlet o el filtro del módulo) |
| `<form:form ... enctype="multipart/form-data">` | `<form method="post" enctype="multipart/form-data" action="...">` + `<n:csrf/>` |
| `<c:url value='/copias/solicitudes/${s.id}'/>` | `<c:url value='/copias/solicitudes/detalle?id=${s.id}'/>` |
| `<form method="post" action="<c:url value='/copias/validacion/${s.id}/aprobar'/>">` | `action="<c:url value='/copias/validacion/aprobar'/>"` + `<input type="hidden" name="id" value="<c:out value='${s.id}'/>">` |

Ejemplo real ya migrado: `src/main/webapp/WEB-INF/jsp/peritajes/peritos/registro.jsp` y `editar.jsp` (compáralos con su versión en `develop` antes de la migración).

---

## 4. Pruebas

- Las pruebas de **services** casi no cambian: se quitan `@SpringBootTest`, `@Autowired` y `@MockBean`, y los objetos se crean con `new` usando los DAO en memoria.
- Si una prueba usaba `Page`, cambia a `Pagina` (los getters se llaman igual).
- Si una prueba usaba `RequestContextHolder` o una sesión simulada, prueba mejor el service pasándole el dato (rol, usuario) como parámetro.
- `./mvnw verify` corre todas las pruebas; el CI hace lo mismo en cada PR.

---

## 5. Revisión final

Estos comandos no deben encontrar nada en tu módulo:

```bash
grep -rn "springframework\|lombok\|jakarta.persistence" src/main/java/mx/edu/utez/sij/copias src/test/java/mx/edu/utez/sij/copias
grep -rn "<form:\|<spring:\|<sec:\|front-end/" src/main/webapp/WEB-INF/jsp/copias src/main/webapp/WEB-INF/tags/copias
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping\|RedirectAttributes" src/main/java/mx/edu/utez/sij/copias
```

Y en el navegador (`http://localhost:8080/sij/copias`):

- [ ] Todas las pantallas abren (sin 404 ni 500).
- [ ] Los formularios guardan, muestran sus errores debajo de cada campo y no se borran al fallar la validación.
- [ ] Después de guardar, recargar la página **no** vuelve a guardar (Post/Redirect/Get).
- [ ] Los mensajes verdes y rojos aparecen después de redirigir.
- [ ] Un id que no existe (`?id=99999`) muestra la página 404.
- [ ] Las rutas de un rol no se pueden abrir con el otro rol (403).

---

## 6. Si algo sale mal

| Te pasa esto | Causa probable | Solución |
| :---- | :---- | :---- |
| Tomcat no arranca: *"has the same url pattern"* | Dos servlets con la misma ruta | Cada ruta en un solo `@WebServlet` |
| `IllegalStateException: Cannot forward after response has been committed` | Falta `return;` después de `Vistas.redirigir`, `noEncontrado` o `sendError` | Poner `return;` |
| 403 al enviar cualquier formulario | Falta `<n:csrf/>` | Agregarlo dentro del `<form>` |
| Los campos salen vacíos después de un error | El servlet no volvió a poner el form con `request.setAttribute("solicitudForm", form)` | Ponerlo antes de `Vistas.mostrar` |
| `NullPointerException` en un servlet al arrancar | La clase del módulo crea los services en un orden en que uno todavía es `null` | Crear primero los que no dependen de nadie |
| El mensaje verde no aparece | Se usó `request.setAttribute("exito", ...)` antes de redirigir | Antes de redirigir, `FlashMessage.exito(...)` |
| Error al subir archivo: *"Unable to process parts"*, o 403 aunque el formulario lleva `<n:csrf/>` | Falta `@MultipartConfig` en el servlet (sin él no se leen los campos del formulario) | Agregarlo |
