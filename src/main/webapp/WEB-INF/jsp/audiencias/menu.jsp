<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Menú lateral del módulo de Audiencias (EQUIPO 2). Lo incluye layout.tag cuando la pantalla usa modulo="audiencias".
  - clave: la misma que la pantalla pone en <t:layout seccion="...">, para marcarla activa.
  - Sin url = pantalla pendiente (aparece desactivada). Al terminar una pantalla, agregar su url.
  - TODO: cuando el núcleo tenga login, mostrar cada opción solo a los roles de la tabla de roles del DFR.
--%>
<n:menu-item clave="inicio" url="/audiencias" texto="Inicio" icono="inicio"/>
<n:menu-item clave="mesas" texto="Mesas" icono="edificio"/>
<n:menu-item clave="abogados" texto="Abogados" icono="personas"/>
<n:menu-item clave="expedientes" texto="Mis expedientes" icono="carpeta"/>
<n:menu-item clave="agenda" texto="Agenda" icono="calendario"/>
<n:menu-item clave="bitacora" texto="Bitácora" icono="bitacora"/>
