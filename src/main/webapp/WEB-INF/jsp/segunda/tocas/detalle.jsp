<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Detalle de la toca con expediente completo (RF 6.1) + Ponencias (RF 5.2) + Cancelación (RF 5.3) --%>
<t:layout titulo="Toca ${toca.folioToca}" modulo="segunda" seccion="tocas">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-tocas-detalle">
    <n:migas><a href="<c:url value='/segunda/tocas'/>">Tocas</a> / <c:out value="${toca.folioToca}"/></n:migas>

    <n:encabezado titulo="${toca.folioToca}"
                  descripcion="Expediente ${toca.numeroExpediente} · ${toca.materiaNombre} · ${toca.salaNombre}">
        <n:estado valor="${toca.estado.etiqueta()}"/>
    </n:encabezado>

    <div class="d-flex flex-wrap gap-2 my-3 sin-resumen">
        <a class="btn btn-sij-secundario" href="<c:url value='/segunda/tocas/historial?id=${toca.id}'/>">
            Ver historial
        </a>
    </div>

    <div class="row g-3">
            <%-- Columna izquierda: datos de la toca y expediente --%>
        <div class="col-lg-8">

            <n:tarjeta titulo="Datos de la toca">
                <div class="row g-3 sij-texto-chico sin-datos">
                    <div class="col-md-4">
                        <div class="text-muted">Folio</div>
                        <div class="fw-semibold"><c:out value="${toca.folioToca}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="text-muted">Expediente original</div>
                        <div class="fw-semibold"><c:out value="${toca.numeroExpediente}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="text-muted">Fecha de recepción</div>
                        <div class="fw-semibold"><c:out value="${toca.fechaRecepcionTexto}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="text-muted">Materia</div>
                        <div class="fw-semibold"><c:out value="${toca.materiaNombre}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="text-muted">Juzgado de origen</div>
                        <div class="fw-semibold"><c:out value="${toca.juzgadoNombre}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="text-muted">Distrito</div>
                        <div class="fw-semibold"><c:out value="${toca.distritoNombre}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="text-muted">Sala</div>
                        <div class="fw-semibold"><c:out value="${toca.salaNombre}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="text-muted">Magistrado</div>
                        <div class="fw-semibold"><c:out value="${toca.magistradoNombre}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="text-muted">Estado</div>
                        <div><n:estado valor="${toca.estado.etiqueta()}"/></div>
                    </div>
                    <div class="col-md-8">
                        <div class="text-muted">Parte apelante</div>
                        <div class="fw-semibold"><c:out value="${toca.apelante}"/></div>
                    </div>
                    <c:if test="${apelacionPdfDisponible}">
                        <div class="col-12">
                            <a class="btn btn-sm btn-sij-secundario" href="<c:url value='/segunda/tocas/documento?id=${toca.id}&tipo=apelacion'/>">Abrir escrito de apelación</a>
                        </div>
                    </c:if>
                </div>
            </n:tarjeta>

            <n:tarjeta titulo="Expediente">
                <c:choose>
                    <c:when test="${empty expediente}">
                        <div class="sij-descripcion">Sin datos del expediente.</div>
                    </c:when>
                    <c:otherwise>
                        <div class="row g-3 sij-texto-chico mb-3 sin-datos">
                            <div class="col-md-4">
                                <div class="text-muted">Tipo</div>
                                <div class="fw-semibold"><c:out value="${expediente.tipo}"/></div>
                            </div>
                            <div class="col-md-4">
                                <div class="text-muted">Juzgado</div>
                                <div class="fw-semibold"><c:out value="${expediente.juzgado}"/></div>
                            </div>
                            <div class="col-md-4">
                                <div class="text-muted">Estado</div>
                                <div class="fw-semibold"><c:out value="${expediente.estado}"/></div>
                            </div>
                        </div>
                        <h3 class="h6 mt-3">Partes</h3>
                        <c:choose>
                            <c:when test="${empty expediente.partes}">
                                <div class="sij-descripcion">Sin partes registradas.</div>
                            </c:when>
                            <c:otherwise>
                                <table class="table sij-tabla table-sm mb-0">
                                    <thead>
                                    <tr><th>Nombre</th><th>RFC</th><th>Rol</th></tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="p" items="${expediente.partes}">
                                        <tr>
                                            <td><c:out value="${p.nombre}"/></td>
                                            <td><c:out value="${p.rfc}"/></td>
                                            <td><c:out value="${p.rol}"/></td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </c:otherwise>
                        </c:choose>
                    </c:otherwise>
                </c:choose>
            </n:tarjeta>

            <n:tarjeta titulo="Audiencias de primera instancia">
                <c:choose>
                    <c:when test="${empty audiencias}">
                        <div class="sij-descripcion">Sin audiencias registradas.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="table sij-tabla table-sm mb-0">
                            <thead>
                            <tr><th>Fecha</th><th>Tipo</th><th>Estado</th><th>Resultado</th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="a" items="${audiencias}">
                                <tr>
                                    <td><c:out value="${a.fechaHora}"/></td>
                                    <td><c:out value="${a.tipo}"/></td>
                                    <td><c:out value="${a.estado}"/></td>
                                    <td><c:out value="${a.resultado}"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </n:tarjeta>

            <n:tarjeta titulo="Resolución de primera instancia">
                <c:choose>
                    <c:when test="${empty resolucionPrimera}">
                        <div class="sij-descripcion">Sin resolución registrada.</div>
                    </c:when>
                    <c:otherwise>
                        <div class="sij-texto-chico">
                            <div><span class="text-muted">Sentido:</span>
                                <span class="fw-semibold"><c:out value="${resolucionPrimera.sentido}"/></span>
                            </div>
                            <div><span class="text-muted">Fecha:</span>
                                <c:out value="${resolucionPrimera.fecha}"/>
                            </div>
                        </div>
                        <p class="mt-2"><c:out value="${resolucionPrimera.resolucion}"/></p>
                    </c:otherwise>
                </c:choose>
            </n:tarjeta>
        </div>

            <%-- Columna derecha: dictámenes + ponencias + cancelación --%>
        <div class="col-lg-4">

            <n:tarjeta titulo="Dictámenes periciales">
                <c:choose>
                    <c:when test="${empty dictamenes}">
                        <div class="sij-descripcion">Sin dictámenes entregados.</div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="d" items="${dictamenes}" varStatus="st">
                            <div class="mb-3 ${st.last ? '' : 'border-bottom pb-3'}">
                                <div class="sij-texto-chico">
                                    <div class="fw-semibold"><c:out value="${d.peritoNombre}"/></div>
                                    <div class="text-muted">Folio <c:out value="${d.folioPeritaje}"/></div>
                                    <div class="text-muted">Entregado: <c:out value="${d.fechaEntrega}"/></div>
                                </div>
                                <div class="mt-2 small"><c:out value="${d.contenidoHtml}"/></div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </n:tarjeta>

                <%-- Ponencias (RF 5.2) — título fijo para evitar fn:length en atributo de tag --%>
            <n:tarjeta titulo="Ponencias">
                <c:if test="${not empty ponencias}">
                    <p class="sij-texto-chico text-muted mb-2">
                        <c:out value="${fn:length(ponencias)}"/> registrada(s).
                    </p>
                </c:if>

                <c:choose>
                    <c:when test="${empty ponencias}">
                        <p class="sij-descripcion mb-2">Sin ponencias registradas.</p>
                    </c:when>
                    <c:otherwise>
                        <table class="table sij-tabla table-sm mb-3">
                            <thead>
                            <tr><th>Fecha y hora</th><th>Estado</th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="p" items="${ponencias}">
                                <tr>
                                    <td><c:out value="${p.horarioTexto}"/></td>
                                    <td><n:estado valor="${p.estado.etiqueta()}"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>

                <c:if test="${not toca.estado.esFinal()}">
                    <h3 class="h6 mt-3">Registrar ponencia</h3>
                    <form method="post" action="<c:url value='/segunda/tocas/ponencias'/>" class="row g-2">
                        <n:csrf/>
                        <input type="hidden" name="id" value="<c:out value='${toca.id}'/>">
                        <div class="col-7">
                            <label for="fechaPonencia" class="form-label sij-texto-chico">Fecha *</label>
                            <input id="fechaPonencia" name="fecha" type="date" class="form-control" required>
                        </div>
                        <div class="col-5">
                            <label for="horaPonencia" class="form-label sij-texto-chico">Hora *</label>
                            <input id="horaPonencia" name="hora" type="time" class="form-control" required>
                        </div>
                        <div class="col-12">
                            <button type="submit" class="btn btn-primary btn-sm w-100">
                                Registrar ponencia
                            </button>
                        </div>
                    </form>
                </c:if>
            </n:tarjeta>

                <%-- Resolución (RF 7.1) --%>
            <c:if test="${toca.estado.name() eq 'EN_PONENCIA'}">
                <n:tarjeta titulo="Resolución">
                    <p class="sij-texto-chico mb-2">
                        La toca ya tiene ponencia. Puede registrar la resolución de segunda instancia.
                    </p>
                    <a class="btn btn-primary btn-sm w-100"
                       href="<c:url value='/segunda/tocas/resolucion?id=${toca.id}'/>">
                        Registrar resolución
                    </a>
                </n:tarjeta>
            </c:if>

            <c:if test="${toca.estado.name() eq 'RESUELTA' and not empty resolucionVigente}">
                <n:tarjeta titulo="Resolución emitida">
                    <div class="sij-texto-chico mb-2">
                        <div><span class="text-muted">Sentido:</span>
                            <span class="fw-semibold"><c:out value="${resolucionVigente.sentido.etiqueta()}"/></span>
                        </div>
                        <div><span class="text-muted">Fecha:</span>
                            <c:out value="${resolucionVigente.fechaTexto}"/>
                        </div>
                        <c:if test="${not empty resolucionVigente.comentarios}">
                            <div class="mt-2"><c:out value="${resolucionVigente.comentarios}"/></div>
                        </c:if>
                    </div>
                    <a class="btn btn-sm btn-sij-secundario w-100" href="<c:url value='/segunda/resoluciones/pdf?id=${resolucionVigente.id}'/>" target="_blank" rel="noopener">Abrir PDF de la resolución</a>
                </n:tarjeta>
            </c:if>

                <%-- toca_Seg.estado no admite cancelación por acuerdo en el esquema vigente. --%>
        </div>
    </div>

        </div>
    </jsp:body>
</t:layout>
