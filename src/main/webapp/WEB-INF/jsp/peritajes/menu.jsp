<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Menú lateral del módulo de Peritajes (EQUIPO 3). Lo incluye layout.tag cuando la pantalla usa modulo="peritajes".
  - clave: la misma que la pantalla pone en <t:layout seccion="...">, para marcarla activa.
  - Sin url = pantalla pendiente (aparece desactivada).
  - Cada opción se muestra solo a sus roles (peritajes.md, sección 3). usuarioPeritajes lo pone PeritajesAuth.
    Ocultar la opción NO es el permiso: cada servlet vuelve a revisar el rol.
  - "Cambiar contraseña" y "Cerrar sesión" son del núcleo (están en el layout).
--%>
<c:if test="${not empty usuarioPeritajes}">
    <n:menu-item clave="inicio" url="/peritajes" texto="Inicio" icono="inicio"/>

    <%-- Perito y Perito Administrador --%>
    <c:if test="${usuarioPeritajes.atiendeCasos}">
        <n:menu-item clave="mis-casos" url="/peritajes/mis-casos" texto="Mis peritajes" icono="documento"/>
    </c:if>

    <%-- Administrador, Perito Administrador y administrador general --%>
    <c:if test="${usuarioPeritajes.administra}">
        <n:menu-item clave="casos" url="/peritajes/casos" texto="Solicitudes y peritajes" icono="carpeta"/>
        <n:menu-item clave="peritos" url="/peritajes/peritos" texto="Catálogo de peritos" icono="personas"/>
        <n:menu-item clave="bitacora" url="/peritajes/bitacora" texto="Bitácora de auditoría" icono="bitacora"/>
        <n:menu-item clave="panel" texto="Panel de carga de trabajo" icono="grafica"/>
    </c:if>
</c:if>
