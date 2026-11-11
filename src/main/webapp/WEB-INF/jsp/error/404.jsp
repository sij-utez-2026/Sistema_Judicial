<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Página de error 404 (la elige web.xml). INTEGRADORES. --%>
<t:layout-publico titulo="Página no encontrada">
    <n:tarjeta clase="text-center mx-auto mt-4" >
        <div class="display-6 fw-semibold text-secondary">404</div>
        <h1 class="sij-titulo mt-2">Página no encontrada</h1>
        <p class="sij-descripcion">La dirección no existe o el registro que busca ya no está disponible.</p>
        <c:set var="direccion" value="${requestScope['jakarta.servlet.error.request_uri']}"/>
        <c:if test="${not empty direccion}">
            <p class="sij-texto-chico">Dirección: <c:out value="${direccion}"/></p>
        </c:if>
        <a class="btn btn-primary" href="<c:url value='/'/>">Ir al inicio</a>
    </n:tarjeta>
</t:layout-publico>
