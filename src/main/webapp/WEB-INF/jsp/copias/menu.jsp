<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Menú lateral del módulo de Copias Certificadas (EQUIPO 5). Lo incluye layout.tag cuando la pantalla usa modulo="copias".
  - clave: la misma que la pantalla pone en <t:layout seccion="...">, para marcarla activa.
  - Sin url = pantalla pendiente (aparece desactivada). Al terminar una pantalla, agregar su url.
  - TODO: cuando el núcleo tenga login, mostrar cada opción solo a los roles de la tabla de roles del DFR.
--%>
<n:menu-item clave="inicio" url="/copias" texto="Inicio" icono="inicio"/>
<n:menu-item clave="validacion" texto="Validación de solicitudes" icono="documento"/>
<n:menu-item clave="entregas" texto="Entregas" icono="carpeta"/>
<n:menu-item clave="solicitudes" texto="Consulta de solicitudes" icono="buscar"/>
