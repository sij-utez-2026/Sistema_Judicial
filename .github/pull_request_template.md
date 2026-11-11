## Qué es este PR
- [ ] Tarea personal → `equipo-N`
- [ ] Sincronización `develop` → `equipo-N`
- [ ] Integración `equipo-N` → `develop`
- [ ] Versión `develop` → `main`

## Equipo, módulo y requerimientos
- Equipo / módulo: <!-- equipo-3 / peritajes -->
- RF del DFR: <!-- 3.1, 3.2 -->
- Issues: <!-- "Relacionado con #12" en PR a equipo-N · "Closes #12" en PR a develop -->

## Qué cambia

## Cómo probarlo
<!-- usuario de prueba, URL y pasos -->

## Checklist
- [ ] Traje lo último de la rama destino y no hay conflictos
- [ ] `./mvnw verify` pasa y lo probé en el navegador
- [ ] Solo modifiqué archivos de mi módulo (si no, lo explico abajo)
- [ ] No uso DAOs, services ni clases de `model` de otro módulo
- [ ] URLs con `/<mi-prefijo>/`, tablas con `<pre>_`, permiso revisado en cada servlet, SQL con `PreparedStatement`
- [ ] Mis JSP están en `src/main/webapp/WEB-INF/jsp/<mi-prefijo>/`, usan `<t:layout modulo="...">` y los componentes `<n:...>`, muestran los datos con `<c:out>` y los `POST` llevan `<n:csrf/>`
- [ ] Si cambié tablas, agregué el `ALTER TABLE` a mi script de `bd/` y lo aviso aquí
- [ ] Actualicé `docs/modulos/<mi-modulo>.md` si cambié rutas, tablas o contratos
- [ ] No subí `bd.properties`, contraseñas, `target/` ni `.idea/`

## Archivos fuera de mi módulo (si aplica)