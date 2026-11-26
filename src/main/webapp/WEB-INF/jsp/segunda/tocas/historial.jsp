<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Historial inalterable de la toca (RF 6.2) --%>
<t:layout titulo="Historial de ${toca.folioToca}" modulo="segunda" seccion="tocas">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-tocas-historial">
    <n:migas>
        <a href="<c:url value='/segunda/tocas'/>">Tocas</a> /
        <a href="<c:url value='/segunda/tocas/detalle?id=${toca.id}'/>"><c:out value="${toca.folioToca}"/></a> /
        Historial
    </n:migas>
    <n:encabezado titulo="Historial de la toca"
                  descripcion="Registro inalterable de eventos de ${toca.folioToca}"/>

    <n:tarjeta sinRelleno="true">
        <c:choose>
            <c:when test="${empty historial}">
                <div class="p-4 text-center sij-descripcion">
                    Esta toca aún no tiene eventos registrados.
                </div>
            </c:when>
            <c:otherwise>
                <ol class="list-unstyled mb-0 p-3 sin-timeline">
                    <c:forEach var="h" items="${historial}">
                        <li class="border-start border-3 ps-3 pb-3 position-relative">
                            <div class="sij-texto-chico text-muted"><c:out value="${h.fechaTexto}"/></div>
                            <div class="fw-semibold"><c:out value="${h.accion}"/></div>
                            <c:if test="${not empty h.detalle}">
                                <div class="sij-texto-chico"><c:out value="${h.detalle}"/></div>
                            </c:if>
                            <c:if test="${not empty h.usuarioNombre}">
                                <div class="sij-texto-chico text-muted">por <c:out value="${h.usuarioNombre}"/></div>
                            </c:if>
                        </li>
                    </c:forEach>
                </ol>
            </c:otherwise>
        </c:choose>
    </n:tarjeta>

        </div>
    </jsp:body>
</t:layout>


