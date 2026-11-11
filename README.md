# Sistema Judicial Integral (SIJ)

Proyecto integrador de la UTEZ: **un solo sistema judicial** con 5 módulos, uno por equipo, dentro de un solo proyecto web **Jakarta EE 11 (Servlets + JSP, MVC)** que corre en **Tomcat 11**.

| Equipo | Módulo | URL | Estado |
| :---- | :---- | :---- | :---- |
| 1 | Oficialía de Partes | `/oficialia` | Página de inicio y menú de ejemplo |
| 2 | Audiencias (SGCA) | `/audiencias` | Página de inicio y menú de ejemplo |
| 3 | Peritajes (SGPJ) | `/peritajes` | Catálogo de peritos (RF 3.1 – 3.4), con datos en memoria |
| 4 | Segunda Instancia (SSI) | `/segunda` | Página de inicio y menú de ejemplo |
| 5 | Copias Certificadas (SGCC) | `/copias` | Página de inicio y menú de ejemplo |
| Integradores | Núcleo común | `/` | Plantilla, componentes y estilos comunes listos. Falta login, usuarios, base de datos y lo demás del núcleo |

## Correr el proyecto

**Requisitos:** Java 21 y **Tomcat 11** (descarga el `.zip` de tomcat.apache.org y descomprímelo). Por ahora **no hace falta MySQL**: Peritajes guarda en memoria (se reinicia al apagar).

**Desde IntelliJ:** *Run → Edit Configurations → + → Tomcat Server → Local*, elige la carpeta de Tomcat 11 y en *Deployment* agrega el artefacto **`sij:war exploded`** con *Application context* **`/sij`**. (En IntelliJ Community, instala el plugin **Smart Tomcat** y usa la misma carpeta y contexto.) Presiona Run.

**Sin IntelliJ:** `./mvnw verify` genera `target/sij.war`; cópialo a la carpeta `webapps/` de Tomcat y arráncalo con `bin/startup` (`startup.bat` en Windows).

Abre <http://localhost:8080/sij/>. Es la página de inicio con una tarjeta por módulo.

| Pantalla | URL |
| :---- | :---- |
| Inicio del sistema | <http://localhost:8080/sij/> |
| Inicio de cada módulo | `/sij/oficialia`, `/sij/audiencias`, `/sij/peritajes`, `/sij/segunda`, `/sij/copias` |
| Catálogo de peritos | <http://localhost:8080/sij/peritajes/peritos> |
| Registrar perito | <http://localhost:8080/sij/peritajes/peritos/nuevo> |

- **`./mvnw: Permission denied`:** ejecuta `chmod +x mvnw`.
- **Una pantalla sale con el diseño viejo:** ejecuta `./mvnw clean` y vuelve a desplegar.
- **Para ver los cambios sin reiniciar:** en la configuración de Tomcat, *On frame deactivation → Update classes and resources*.

Compilar y correr las pruebas: `./mvnw verify`. Lo mismo corre el CI en cada PR.

## Cómo está organizado

```text
sistema-judicial/
├── src/main/java/mx/edu/utez/sij/
│   ├── nucleo/                    #   INTEGRADORES: FiltroNucleo (seguridad y CSRF), herramientas para servlets,
│   │                              #   conexión a MySQL, inicio (/)
│   └── oficialia/  audiencias/  peritajes/  segunda/  copias/   # un paquete por equipo
│                                  #   controller/ (servlets)  service/  dao/  model/  dto/
├── src/main/webapp/               # PANTALLAS (JSP). Guía: docs/FRONT_END.md
│   ├── WEB-INF/web.xml            #   sesión y páginas de error (solo integradores)
│   ├── WEB-INF/tags/              #   layout.tag (menú lateral), layout-publico.tag y componentes <n:...>
│   ├── WEB-INF/jsp/<prefijo>/     #   pantallas de cada módulo + su menu.jsp
│   └── recursos/<prefijo>/        #   CSS y JS (los comunes en recursos/nucleo/)
├── src/main/resources/
│   └── bd.example.properties      #   ejemplo de conexión a MySQL (copiar como bd.properties, que no se sube)
├── docs/                          # Arquitectura, contratos, flujo de Git y un documento por módulo
├── AGENTS.md                      # Reglas para asistentes de IA (Copilot, Claude, ChatGPT…)
└── pom.xml                        # Empaquetado war (solo integradores)
```

**Tecnologías:** Java 21, Jakarta EE 11 (Servlets 6.1, JSP, JSTL) en Tomcat 11, Bootstrap 5 (CDN), Jakarta Validation, JDBC con DAO y MySQL 8 (pool HikariCP). Sin Spring. ¿Venías de la versión con Spring Boot? Lee [docs/MIGRACION_SPRING_A_SERVLETS.md](docs/MIGRACION_SPRING_A_SERVLETS.md).

## Pantallas: todas con la misma estructura

Todas las pantallas usan la plantilla común `<t:layout>`. Así los cinco módulos comparten el mismo menú lateral, encabezado, mensajes y estilos, y cada equipo solo escribe el contenido de su pantalla:

```jsp
<t:layout titulo="Mesas" modulo="audiencias" seccion="mesas">
    <n:migas>Mesas</n:migas>
    <n:encabezado titulo="Mesas de audiencia" descripcion="Mesas por materia y sus abogados."/>
    <n:tarjeta> ... </n:tarjeta>
</t:layout>
```

Cada equipo agrega las opciones de su menú lateral en `src/main/webapp/WEB-INF/jsp/<prefijo>/menu.jsp`. El paso a paso y la lista de componentes están en **[docs/FRONT_END.md](docs/FRONT_END.md)**.

## Documentación

| Documento | Para qué |
| :---- | :---- |
| [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md) | Arquitectura común: módulos, núcleo, seguridad, base de datos y front-end (sección 16) |
| [docs/CONTRATOS.md](docs/CONTRATOS.md) | Interfaces de Java entre módulos |
| [docs/GUIA_PANTALLAS.md](docs/GUIA_PANTALLAS.md) | **Empieza aquí:** cómo hacer tus pantallas, explicado fácil |
| [docs/FLUJO_DE_TRABAJO.md](docs/FLUJO_DE_TRABAJO.md) | Reglas obligatorias de Git y para asistentes de IA |
| [docs/GUIA_GITHUB.md](docs/GUIA_GITHUB.md) | Tutorial de GitHub paso a paso |
| [docs/modulos/](docs/modulos/) | Un documento por módulo (a partir de su DFR) y la plantilla `_PLANTILLA_MODULO.md` |
| [docs/FRONT_END.md](docs/FRONT_END.md) | Cómo hacer una pantalla nueva |
| [docs/MIGRACION_SPRING_A_SERVLETS.md](docs/MIGRACION_SPRING_A_SERVLETS.md) | Cómo pasar a servlets lo que se hizo con Spring Boot |
| [AGENTS.md](AGENTS.md) | Instrucciones para asistentes de IA |

## Cómo se trabaja

```text
main  ←  develop  ←  equipo-N  ←  equipo-N/<tipo>-<descripcion>
```

1. Crea tu rama desde la de tu equipo: `git switch equipo-3 && git pull && git switch -c equipo-3/feat-mi-tarea`.
2. Modifica **solo** las carpetas de tu módulo: tu paquete Java, `src/main/webapp/WEB-INF/jsp/<prefijo>/` y `src/main/webapp/recursos/<prefijo>/`.
3. `./mvnw verify`, pruébalo en Tomcat 11 y abre un PR hacia `equipo-N`.

Detalle en [docs/FLUJO_DE_TRABAJO.md](docs/FLUJO_DE_TRABAJO.md).

## Pendientes del núcleo (integradores)

- Login con PBKDF2, roles por URL en `FiltroNucleo` y el menú según el rol.
- Scripts SQL en `bd/` y los primeros DAO con MySQL (`ConexionBD` ya está lista).
- Usuarios, notificaciones, bitácora, archivos y PDF.
- Las interfaces de `docs/CONTRATOS.md` en `nucleo/contratos/`.

El avance completo del Sprint 0 está en `docs/ARQUITECTURA.md`, sección 14.
