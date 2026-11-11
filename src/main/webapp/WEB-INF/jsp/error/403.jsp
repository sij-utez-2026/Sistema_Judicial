<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Página de error 403 (la elige web.xml). INTEGRADORES. --%>
<t:layout-publico titulo="No tiene permiso para ver esta página">
    <n:tarjeta clase="text-center mx-auto mt-4" >
        <div class="display-6 fw-semibold text-secondary">403</div>
        <h1 class="sij-titulo mt-2">No tiene permiso para ver esta página</h1>
        <p class="sij-descripcion">Su usuario no tiene el rol necesario. Si cree que es un error, consulte al administrador del módulo.</p>
        <c:set var="direccion" value="${requestScope['jakarta.servlet.error.request_uri']}"/>
        <c:if test="${not empty direccion}">
            <p class="sij-texto-chico">Dirección: <c:out value="${direccion}"/></p>
        </c:if>
        <a class="btn btn-primary" href="<c:url value='/'/>">Ir al inicio</a>
    </n:tarjeta>
</t:layout-publico>
