# Front-end (pantallas JSP)

Aquí están **todas las pantallas** del Sistema Judicial Integral. Están hechas con **JSP + JSTL + Bootstrap 5**, y todas usan la misma plantilla y los mismos componentes. Así los cinco módulos se ven y se comportan igual.

> Si es tu primera vez, empieza por `docs/GUIA_PANTALLAS.md` (explicada fácil). El detalle técnico está en `docs/ARQUITECTURA.md`, sección 16. Esta guía es el paso a paso para hacer una pantalla nueva.

## Qué hay en `src/main/webapp/`

```text
src/main/webapp/
├── WEB-INF/                      # No se abre desde el navegador: solo a través de un servlet
│   ├── web.xml                   # INTEGRADORES · sesión y páginas de error
│   ├── tags/
│   │   ├── layout.tag            # INTEGRADORES · plantilla con menú lateral (pantallas con sesión)
│   │   ├── layout-publico.tag    # INTEGRADORES · plantilla sin menú (portal de Copias y errores)
│   │   └── nucleo/               # INTEGRADORES · componentes <n:...> (tabla de abajo)
│   └── jsp/
│       ├── nucleo/inicio.jsp     # INTEGRADORES · inicio del sistema (/) con una tarjeta por módulo
│       ├── error/                # INTEGRADORES · 403.jsp, 404.jsp, 500.jsp
│       └── <prefijo>/            # CADA EQUIPO · sus pantallas
│           ├── inicio.jsp        #   inicio del módulo (/<prefijo>), sirve de ejemplo para copiar
│           ├── menu.jsp          #   opciones del menú lateral del módulo
│           └── ...               #   el resto de sus pantallas (por ejemplo peritajes/peritos/lista.jsp)
└── recursos/                     # CSS, JS e imágenes (se sirven en /recursos/...)
    ├── nucleo/css/estilos.css    # INTEGRADORES · colores y estilos comunes
    ├── nucleo/js/comun.js        # INTEGRADORES · confirmaciones, filtros, CSRF para fetch
    └── <prefijo>/                # CADA EQUIPO · css/, js/ e img/ propios
```

Prefijos: `oficialia` (Equipo 1), `audiencias` (Equipo 2), `peritajes` (Equipo 3), `segunda` (Equipo 4), `copias` (Equipo 5).

**Ejemplo completo para copiar:** las pantallas de peritos de Peritajes (`jsp/peritajes/peritos/`): lista con filtros y paginación, registro con validaciones y edición con confirmación.

## Hacer una pantalla nueva en 4 pasos

### 1. El servlet manda los datos a la vista

```java
@WebServlet("/audiencias/mesas")
public class MesaListarServlet extends HttpServlet {

    private final MesaService mesaService = Audiencias.mesaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("mesas", mesaService.listar());
        Vistas.mostrar(request, response, "audiencias/mesas/lista");   // → /WEB-INF/jsp/audiencias/mesas/lista.jsp
    }
}
```

### 2. La JSP usa la plantilla

`src/main/webapp/WEB-INF/jsp/audiencias/mesas/lista.jsp`:

```jsp
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>

<t:layout titulo="Mesas" modulo="audiencias" seccion="mesas">
    <n:migas>Mesas</n:migas>
    <n:encabezado titulo="Mesas de audiencia" descripcion="Mesas por materia y sus abogados.">
        <a class="btn btn-primary" href="<c:url value='/audiencias/mesas/nueva'/>">+ Nueva mesa</a>
    </n:encabezado>

    <n:tarjeta sinRelleno="true" clase="mt-3">
        <table class="table sij-tabla align-middle mb-0">
            <thead><tr><th>Nombre</th><th>Estado</th></tr></thead>
            <tbody>
            <c:forEach var="m" items="${mesas}">
                <tr>
                    <td><c:out value="${m.nombre}"/></td>
                    <td><n:estado valor="${m.activa ? 'Activa' : 'Inactiva'}"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </n:tarjeta>
</t:layout>
```

- `modulo="audiencias"` muestra el nombre del módulo y su menú lateral.
- `seccion="mesas"` marca como activa la opción del menú con `clave="mesas"`.
- No copies el menú, el encabezado ni los `<link>` de Bootstrap: la plantilla ya los trae.

### 3. Activa la opción en el menú del módulo

`src/main/webapp/WEB-INF/jsp/audiencias/menu.jsp`. La opción ya existe sin `url` (sale como "Pendiente"); solo agrégale la `url`:

```jsp
<n:menu-item clave="mesas" url="/audiencias/mesas" texto="Mesas" icono="edificio"/>
```

### 4. Si la pantalla necesita CSS o JS propio

Van en `src/main/webapp/recursos/<prefijo>/` y se cargan con los atributos `estilos` y `scripts`. En ese caso el contenido va dentro de `<jsp:body>`:

```jsp
<t:layout titulo="Agenda" modulo="audiencias" seccion="agenda">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/audiencias/css/agenda.css'/>">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script src="<c:url value='/recursos/audiencias/js/agenda.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        ... contenido ...
    </jsp:body>
</t:layout>
```

Las clases CSS propias empiezan con el prefijo corto del módulo: `ofi-`, `aud-`, `per-`, `sin-`, `cop-`.

## Componentes (`<n:...>`)

Cada archivo de `WEB-INF/tags/nucleo/` explica su uso en el comentario de arriba.

| Componente | Ejemplo | Para qué |
| :---- | :---- | :---- |
| `migas` | `<n:migas><a href="...">Mesas</a> / Nueva mesa</n:migas>` | Ubicación arriba de la pantalla |
| `encabezado` | `<n:encabezado titulo="..." descripcion="...">botones</n:encabezado>` | Título, descripción y botones de acción |
| `tarjeta` | `<n:tarjeta titulo="Datos">...</n:tarjeta>` | Recuadro blanco. `sinRelleno="true"` para tablas |
| `estado` | `<n:estado valor="${a.estado}"/>` | Etiqueta con punto de color (o `color="rojo"`) |
| `paginacion` | `<n:paginacion pagina="${pagina}" url="/audiencias/mesas"/>` | "Mostrando 1–10 de 40" y botones; conserva los filtros |
| `confirmar` | `<n:confirmar accion="/audiencias/mesas/estado?id=3" texto="¿Desactivar la mesa?" boton="Desactivar"/>` | Botón con confirmación que envía un `POST` |
| `archivo` | `<n:archivo nombre="acta" etiqueta="Acta firmada" tipos=".pdf,.jpg,.png" maxMb="5"/>` | Campo para subir archivo |
| `csrf` | `<n:csrf/>` | Token CSRF: va en **todo** `<form method="post">` (sin él, 403) |
| `error` | `<n:error campo="nombre"/>` | Mensaje de validación de un campo |
| `menu-item` | `<n:menu-item clave="..." url="..." texto="..." icono="..."/>` | Opción del menú lateral (en `menu.jsp`) |
| `icono` | `<n:icono nombre="buscar"/>` | Íconos: inicio, personas, persona, carpeta, documento, calendario, balanza, grafica, campana, bitacora, edificio, buscar, editar, mas, salir |
| `mensajes` | ya va en la plantilla | Muestra `exito`, `exitoDetalle` y `error` |

**Mensajes desde el servlet** (antes de redirigir):

```java
FlashMessage.exito(request, "Registro exitoso", "Mesa 3 quedó activa.");   // aviso verde; la segunda línea es opcional (null)
FlashMessage.error(request, e.getMessage());                               // alerta roja
Vistas.redirigir(request, response, "/audiencias/mesas");
return;
```

Si vuelves a mostrar la misma pantalla (sin redirigir), usa `request.setAttribute("error", "...")`.

**Botones y clases comunes:** `btn btn-primary` (principal), `btn btn-sij-secundario` (blanco), `sij-tabla`, `sij-contador`, `sij-texto-chico`, `sij-descripcion`, `sij-aviso sij-aviso-advertencia`.

**JavaScript común (`comun.js`):** `data-autoenvio` en un `<select>` aplica el filtro al cambiar; `data-confirmar="¿...?"` en un formulario pide confirmación; `SIJ.confirmar(texto)` y `SIJ.encabezadosCsrf()` para tu propio JS. Los formularios ya evitan el doble envío.

## Formularios

HTML normal con `<n:csrf/>`. El servlet valida con `Validador.validar(form)` y, si hay errores, deja `errores` (campo → mensaje) y el formulario capturado (`mesaForm`) para no borrar lo que se escribió:

```jsp
<form method="post" action="<c:url value='/audiencias/mesas/nueva'/>" novalidate>
    <n:csrf/>
    <n:tarjeta titulo="Datos de la mesa">
        <label for="nombre" class="form-label">Nombre *</label>
        <input id="nombre" name="nombre" type="text"
               class="form-control${not empty errores.nombre ? ' is-invalid' : ''}"
               value="<c:out value='${mesaForm.nombre}'/>">
        <n:error campo="nombre"/>
    </n:tarjeta>
    <button type="submit" class="btn btn-primary">Guardar</button>
</form>
```

```java
// En el doPost del servlet
MesaForm form = new MesaForm();
form.setNombre(Parametros.texto(request, "nombre"));
Map<String, String> errores = Validador.validar(form);
if (!errores.isEmpty()) {
    request.setAttribute("errores", errores);
    request.setAttribute("mesaForm", form);
    Vistas.mostrar(request, response, "audiencias/mesas/formulario");
    return;
}
```

El ejemplo completo (casillas, listas que se agregan con JavaScript, edición) está en Peritajes: `PeritoRegistrarServlet`, `PeritoFormSupport` y `jsp/peritajes/peritos/registro.jsp`.

## Reglas (las revisa quien aprueba el PR)

- Todo dato con `<c:out value="${...}"/>`. Nunca `${...}` suelto (riesgo de XSS).
- Nada de `<% %>`: solo `<c:if>`, `<c:forEach>`, `<c:choose>`.
- Enlaces y recursos con `<c:url value='/...'/>`.
- Toda pantalla dentro de `<t:layout>` (o `<t:layout-publico>` en el portal de Copias).
- De un `record` solo se leen sus campos: `${m.nombre}`. Sus métodos se llaman con paréntesis: `${carga.total()}`.
- Fechas ya formateadas como texto desde el servlet o el DTO (`fmt:formatDate` no funciona con `LocalDate`).
- No uses `trimDirectiveWhitespaces`: borra los espacios entre etiquetas.
- Solo editas `jsp/<tu-prefijo>/` y `recursos/<tu-prefijo>/`. Si necesitas cambiar la plantilla, un componente o `estilos.css`, abre un issue para los integradores.

## Ver los cambios

Corre el proyecto en **Tomcat 11** desde IntelliJ (artefacto `sij:war exploded`, contexto `/sij`) y abre `http://localhost:8080/sij/`. Con *On frame deactivation → Update classes and resources*, los cambios en JSP, `.tag`, CSS y JS se ven al recargar el navegador. Si una pantalla sale con el diseño viejo, ejecuta `./mvnw clean` y vuelve a desplegar. Configuración paso a paso: `docs/ARQUITECTURA.md`, sección 12.
