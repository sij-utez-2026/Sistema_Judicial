<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Inicio del módulo (/peritajes): dashboard según el rol, en tarjetas (InicioPeritajesServlet).
  - Administrador y administrador general: "Coordinación de peritajes" (peritajes por estado, peritos y accesos).
  - Perito: "Mi espacio" (sus peritajes y accesos).
  - Perito Administrador: las dos secciones.
  - Usuarios de Peritajes: sus avisos (RF 5.1). El administrador general no tiene.
--%>
<t:layout titulo="Inicio" modulo="peritajes" seccion="inicio">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/peritajes/css/peritajes.css'/>">
    </jsp:attribute>
    <jsp:body>
        <n:migas>Inicio</n:migas>
        <n:encabezado titulo="Hola, ${usuarioPeritajes.nombre}"
                      descripcion="${usuarioPeritajes.rol} · ${moduloActual.nombreLargo}"/>

        <%-- ===================== Perito y Perito Administrador ===================== --%>
        <c:if test="${usuarioPeritajes.atiendeCasos}">
            <h2 class="fs-5 mt-3 mb-2">Mi espacio</h2>
            <div class="row g-3 mb-2">
                <div class="col-sm-4">
                    <a class="sij-tarjeta p-4 h-100 d-block" href="<c:url value='/peritajes/mis-casos?estado=ASIGNADO'/>">
                        <div class="sij-texto-chico">Asignados</div>
                        <div class="per-numero"><c:out value="${misAsignados}"/></div>
                        <div class="sij-texto-chico">Pendientes de abrir expediente</div>
                    </a>
                </div>
                <div class="col-sm-4">
                    <a class="sij-tarjeta p-4 h-100 d-block" href="<c:url value='/peritajes/mis-casos?estado=EN_PROCESO'/>">
                        <div class="sij-texto-chico">En Proceso</div>
                        <div class="per-numero"><c:out value="${misEnProceso}"/></div>
                        <div class="sij-texto-chico">Con dictamen en preparación</div>
                    </a>
                </div>
                <div class="col-sm-4">
                    <a class="sij-tarjeta p-4 h-100 d-block" href="<c:url value='/peritajes/mis-casos?estado=DICTAMEN_ENTREGADO'/>">
                        <div class="sij-texto-chico">Dictámenes entregados</div>
                        <div class="per-numero"><c:out value="${misEntregados}"/></div>
                        <div class="sij-texto-chico">Solo lectura</div>
                    </a>
                </div>
            </div>

            <n:tarjeta titulo="Mis peritajes activos">
                <c:choose>
                    <c:when test="${empty misActivos}">
                        <p class="sij-descripcion mb-0">No tienes peritajes asignados</p>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="caso" items="${misActivos}">
                            <div class="per-archivo">
                                <div>
                                    <div class="fw-semibold">Peritaje <c:out value="${caso.folio}"/> · <c:out value="${caso.materia}"/></div>
                                    <div class="sij-texto-chico"><c:out value="${caso.referenciaDemanda}"/> · <n:estado valor="${caso.estado.texto}"/></div>
                                </div>
                                <%-- DFR 3.3: el perito llega a su expediente en máximo 3 clics desde su dashboard --%>
                                <a class="btn btn-sm ${caso.estado eq 'EN_PROCESO' ? 'btn-primary' : 'btn-sij-secundario'}"
                                   href="<c:url value='/peritajes/mis-casos/detalle?id=${caso.id}'/>">
                                    <c:out value="${caso.estado eq 'EN_PROCESO' ? 'Continuar dictamen' : 'Abrir expediente'}"/>
                                </a>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </n:tarjeta>
        </c:if>

        <%-- ===================== Administrador y Perito Administrador ===================== --%>
        <c:if test="${usuarioPeritajes.administra}">
            <h2 class="fs-5 mt-3 mb-2">Coordinación de peritajes</h2>
            <div class="row g-3 mb-3">
                <div class="col-sm-6 col-xl-3">
                    <a class="sij-tarjeta p-4 h-100 d-block" href="<c:url value='/peritajes/casos?estado=PENDIENTE_ASIGNACION'/>">
                        <div class="sij-texto-chico">Pendientes de asignación</div>
                        <div class="per-numero"><c:out value="${porEstado['PENDIENTE_ASIGNACION']}"/></div>
                        <div class="sij-texto-chico">Sin perito activo de la materia</div>
                    </a>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <a class="sij-tarjeta p-4 h-100 d-block" href="<c:url value='/peritajes/casos?estado=ASIGNADO'/>">
                        <div class="sij-texto-chico">Asignados</div>
                        <div class="per-numero"><c:out value="${porEstado['ASIGNADO']}"/></div>
                        <div class="sij-texto-chico">El perito aún no abre el expediente</div>
                    </a>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <a class="sij-tarjeta p-4 h-100 d-block" href="<c:url value='/peritajes/casos?estado=EN_PROCESO'/>">
                        <div class="sij-texto-chico">En Proceso</div>
                        <div class="per-numero"><c:out value="${porEstado['EN_PROCESO']}"/></div>
                        <div class="sij-texto-chico">Dictamen en preparación</div>
                    </a>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <a class="sij-tarjeta p-4 h-100 d-block" href="<c:url value='/peritajes/casos?estado=DICTAMEN_ENTREGADO'/>">
                        <div class="sij-texto-chico">Dictámenes entregados</div>
                        <div class="per-numero"><c:out value="${porEstado['DICTAMEN_ENTREGADO']}"/></div>
                        <div class="sij-texto-chico">Consultables por Audiencias</div>
                    </a>
                </div>
            </div>

            <div class="row g-3 mb-3">
                <div class="col-md-6 col-xl-4">
                    <a class="sij-tarjeta p-4 h-100 d-block" href="<c:url value='/peritajes/casos'/>">
                        <h3 class="sij-tarjeta-titulo mb-1">Solicitudes y peritajes</h3>
                        <p class="sij-descripcion mb-0">Asignación, estado de cada solicitud y reasignación manual.</p>
                    </a>
                </div>
                <div class="col-md-6 col-xl-4">
                    <a class="sij-tarjeta p-4 h-100 d-block" href="<c:url value='/peritajes/peritos'/>">
                        <h3 class="sij-tarjeta-titulo mb-1">Catálogo de peritos</h3>
                        <p class="sij-descripcion mb-0">
                            <c:out value="${peritosActivos}"/> activos · <c:out value="${peritosInactivos}"/> inactivos.
                            Registro, edición, baja y reactivación.
                        </p>
                    </a>
                </div>
                <div class="col-md-6 col-xl-4">
                    <a class="sij-tarjeta p-4 h-100 d-block" href="<c:url value='/peritajes/bitacora'/>">
                        <h3 class="sij-tarjeta-titulo mb-1">Bitácora de auditoría</h3>
                        <p class="sij-descripcion mb-0">Historial de movimientos del módulo, de solo consulta.</p>
                    </a>
                </div>
            </div>
        </c:if>

        <%-- ===================== Usuarios de Peritajes: avisos (RF 5.1) ===================== --%>
        <c:if test="${not usuarioPeritajes.administradorGeneral}">
            <n:tarjeta titulo="Avisos recientes">
                <c:choose>
                    <c:when test="${empty avisos}">
                        <p class="sij-descripcion mb-0">No tiene avisos.</p>
                    </c:when>
                    <c:otherwise>
                        <ul class="list-unstyled mb-0">
                            <c:forEach var="aviso" items="${avisos}">
                                <li class="per-movimiento"><c:out value="${aviso.mensaje}"/></li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </n:tarjeta>
        </c:if>
    </jsp:body>
</t:layout>
