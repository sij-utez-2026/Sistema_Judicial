<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Menú lateral del módulo de Copias Certificadas (EQUIPO 5). Lo incluye layout.tag cuando la pantalla usa modulo="copias".
  - clave: la misma que la pantalla pone en <t:layout seccion="...">, para marcarla activa.
  - Cada rol ve solo sus opciones (esAdminCopias y usuarioCopias los pone FiltroCopias, a partir de SesionCopias).
  - Quien entra con un rol de Oficialía (admin_op / usuario_op) ve además "Volver a Oficialía".
  - TODO: con el inicio de sesión del núcleo, se borra el bloque "Modo de prueba".
--%>
<c:if test="${usuarioCopias.deOficialia}">
    <n:menu-item clave="volver-oficialia" url="${inicioOficialia}" texto="← Volver a Oficialía" icono="edificio"/>
</c:if>

<c:choose>
    <c:when test="${esAdminCopias}">
        <%-- COPIAS_ADMIN (o admin_op de Oficialía): administración del área --%>
        <n:menu-item clave="admin-inicio" url="/copias/admin" texto="Inicio" icono="inicio"/>
        <n:menu-item clave="personal" url="/copias/admin/personal" texto="Trabajadores" icono="personas"/>
        <n:menu-item clave="colores" url="/copias/admin/colores" texto="Colores" icono="editar"/>
    </c:when>
    <c:otherwise>
        <%-- COPIAS_PERSONAL (o usuario_op de Oficialía): trabajo diario --%>
        <n:menu-item clave="inicio" url="/copias" texto="Inicio" icono="inicio"/>
        <n:menu-item clave="nueva" url="/copias/solicitudes/nueva" texto="Nueva solicitud" icono="mas"/>
        <n:menu-item clave="solicitudes" url="/copias/solicitudes" texto="Registro de solicitudes" icono="buscar"/>
        <n:menu-item clave="validacion" url="/copias/validacion" texto="Validación de privadas" icono="documento"/>
        <n:menu-item clave="entregas" url="/copias/entregas" texto="Entregas" icono="carpeta"/>
    </c:otherwise>
</c:choose>

<%-- TEMPORAL: "Modo de prueba" para entrar como cualquier usuario sin inicio de sesión (ModoPruebaServlet) --%>
<div class="cop-modo-prueba">
    <div class="sij-etiqueta-menu">Modo de prueba</div>
    <c:if test="${not empty usuarioCopias}">
        <div class="cop-modo-actual">
            <c:out value="${usuarioCopias.nombre}"/> ·
            <c:out value="${usuarioCopias.rolTexto}"/><c:if test="${usuarioCopias.deOficialia}"> (Oficialía)</c:if>
        </div>
    </c:if>
    <form method="post" action="<c:url value='/copias/modo-prueba'/>">
        <n:csrf/>
        <label for="copiasUsuarioPrueba" class="visually-hidden">Entrar como</label>
        <select id="copiasUsuarioPrueba" name="usuarioId" class="form-select form-select-sm cop-modo-select" data-autoenvio>
            <c:forEach var="u" items="${usuariosDePrueba}">
                <option value="<c:out value='${u.id}'/>"${u.id eq usuarioCopias.id ? ' selected' : ''}>
                    <c:out value="${u.nombre}"/> — <c:out value="${u.rolTexto}"/><c:if test="${u.deOficialia}"> (Oficialía)</c:if>
                </option>
            </c:forEach>
        </select>
        <noscript><button type="submit" class="btn btn-sm cop-modo-opcion mt-1">Cambiar</button></noscript>
    </form>
</div>
