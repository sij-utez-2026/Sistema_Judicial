<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Detalle del peritaje, asignación automática (RF 2.2) y reasignación manual (RF 2.3). Administradores.
  Bocetos "Peritaje · Detalle y elegibilidad" y "Peritaje · Reasignación autorizada" (docs/modulos/Nathaly/).
  La base no guarda la lista de candidatos de la asignación automática: el detalle (carga previa y cuántos
  peritos eran elegibles) está en la bitácora.
--%>
<t:layout titulo="Peritaje ${peritaje.folio}" modulo="peritajes" seccion="casos">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/peritajes/css/peritajes.css'/>">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script src="<c:url value='/recursos/peritajes/js/casos.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <n:migas><a href="<c:url value='/peritajes/casos'/>">Peritajes</a> / Peritaje <c:out value="${peritaje.folio}"/></n:migas>

        <n:encabezado titulo="Peritaje ${peritaje.folio}" descripcion="Detalle del peritaje · ${peritaje.referenciaDemanda}">
            <c:if test="${puedeReasignar}">
                <button type="button" class="btn btn-sij-secundario" data-bs-toggle="modal" data-bs-target="#ventanaReasignar">
                    <n:icono nombre="persona" tamano="14"/> Reasignar perito
                </button>
            </c:if>
        </n:encabezado>

        <n:tarjeta titulo="Información del peritaje" clase="mt-3">
            <dl class="row mb-0 per-datos">
                <div class="col-sm-6 col-lg-3"><dt>Folio</dt><dd><c:out value="${peritaje.folio}"/></dd></div>
                <div class="col-sm-6 col-lg-3"><dt>Demanda</dt><dd><c:out value="${peritaje.folioDemanda}"/></dd></div>
                <div class="col-sm-6 col-lg-3"><dt>Expediente</dt><dd><c:out value="${empty peritaje.numeroExpediente ? 'Sin expediente' : peritaje.numeroExpediente}"/></dd></div>
                <div class="col-sm-6 col-lg-3"><dt>Estado</dt><dd><n:estado valor="${peritaje.estado.texto}"/></dd></div>
                <div class="col-sm-6 col-lg-3"><dt>Materia</dt><dd><c:out value="${peritaje.materia}"/></dd></div>
                <div class="col-sm-6 col-lg-3"><dt>Órgano solicitante</dt><dd><c:out value="${empty peritaje.juzgado ? 'Sin dato' : peritaje.juzgado}"/></dd></div>
                <div class="col-sm-6 col-lg-3"><dt>Perito asignado</dt><dd><c:out value="${peritaje.peritoNombreTexto}"/></dd></div>
                <div class="col-sm-6 col-lg-3"><dt>Especialidades del perito</dt><dd><c:out value="${empty peritaje.especialidadesPerito ? '—' : peritaje.especialidadesPerito}"/></dd></div>
            </dl>
        </n:tarjeta>

        <div class="row g-3">
            <div class="col-lg-8">
                <n:tarjeta titulo="Solicitud de intervención">
                    <p class="mb-3"><c:out value="${empty peritaje.descripcionDemanda ? 'Sin descripción de la demanda.' : peritaje.descripcionDemanda}"/></p>
                    <dl class="row mb-0 per-datos">
                        <div class="col-sm-6"><dt>Fecha de solicitud</dt><dd><c:out value="${peritaje.fechaSolicitudTexto}"/></dd></div>
                    </dl>
                </n:tarjeta>

                <c:if test="${peritaje.estado eq 'PENDIENTE_ASIGNACION'}">
                    <div class="sij-aviso sij-aviso-advertencia p-3 mb-3">
                        &#9432; No hay peritos activos con la materia <c:out value="${peritaje.materia}"/>. El sistema lo
                        asignará automáticamente en cuanto haya uno (por ejemplo, al registrar o reactivar un perito).
                    </div>
                </c:if>

                <%-- Asignación automática y reasignaciones (asignacion_Per), de la más antigua a la más reciente --%>
                <c:if test="${not empty historial}">
                    <n:tarjeta titulo="Historial de asignación">
                        <ul class="list-unstyled mb-0">
                            <c:forEach var="mov" items="${historial}">
                                <li class="per-movimiento">
                                    <c:choose>
                                        <c:when test="${mov.automatica}">
                                            <div class="fw-semibold">Asignación automática → <c:out value="${mov.peritoNuevoNombre}"/></div>
                                            <div class="sij-texto-chico"><c:out value="${mov.fechaTexto}"/> · por el sistema</div>
                                            <div>Perito activo con la materia y la menor carga activa (Asignado + En Proceso); si hay empate, el sistema elige al azar.</div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="fw-semibold"><c:out value="${mov.peritoAnteriorNombre}"/> &rarr; <c:out value="${mov.peritoNuevoNombre}"/></div>
                                            <div class="sij-texto-chico"><c:out value="${mov.fechaTexto}"/> · reasignación manual por <c:out value="${mov.realizadoPor}"/></div>
                                            <div>Motivo: <c:out value="${mov.motivo}"/></div>
                                        </c:otherwise>
                                    </c:choose>
                                </li>
                            </c:forEach>
                        </ul>
                    </n:tarjeta>
                </c:if>
            </div>

            <div class="col-lg-4">
                <n:tarjeta titulo="Seguimiento">
                    <div class="mb-3"><n:estado valor="${peritaje.estado.texto}"/></div>
                    <dl class="mb-3 per-datos">
                        <dt>Asignación vigente</dt>
                        <dd><c:out value="${empty peritaje.fechaAsignacion ? 'Pendiente' : peritaje.fechaAsignacionTexto}"/></dd>
                        <dt>Apertura del expediente</dt>
                        <dd><c:out value="${empty peritaje.fechaApertura ? 'El perito aún no abre el expediente' : peritaje.fechaAperturaTexto}"/></dd>
                        <c:if test="${not empty peritaje.fechaEntrega}">
                            <dt>Entrega del dictamen</dt>
                            <dd><c:out value="${peritaje.fechaEntregaTexto}"/></dd>
                        </c:if>
                    </dl>
                    <p class="sij-texto-chico">Registro conservado en la bitácora de auditoría.</p>
                    <a class="btn btn-sm btn-sij-secundario" href="<c:url value='/peritajes/bitacora'><c:param name='q' value='${peritaje.folio}'/></c:url>">Consultar bitácora</a>
                </n:tarjeta>
            </div>
        </div>

        <%-- ===== Ventana de reasignación (RF 2.3) ===== --%>
        <c:if test="${puedeReasignar}">
            <div class="modal fade" id="ventanaReasignar" tabindex="-1" aria-labelledby="tituloReasignar" aria-hidden="true"
                 <c:if test="${abrirReasignacion}">data-abrir-al-cargar</c:if>>
                <div class="modal-dialog modal-dialog-centered modal-lg">
                    <form class="modal-content" method="post" action="<c:url value='/peritajes/casos/reasignar'/>">
                        <n:csrf/>
                        <input type="hidden" name="id" value="<c:out value='${peritaje.id}'/>">
                        <div class="modal-header">
                            <div>
                                <div class="sij-texto-chico text-uppercase"><c:out value="${usuarioPeritajes.rol}"/> · acción autorizada</div>
                                <h2 class="modal-title fs-5" id="tituloReasignar">Reasignar perito</h2>
                            </div>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                        </div>
                        <div class="modal-body">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <span>Peritaje <c:out value="${peritaje.folio}"/> · <c:out value="${peritaje.materia}"/></span>
                                <n:estado valor="${peritaje.estado.texto}"/>
                            </div>
                            <c:choose>
                                <c:when test="${empty candidatos}">
                                    <div class="sij-aviso sij-aviso-advertencia p-3">
                                        No hay otros peritos activos con la materia del peritaje.
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <fieldset>
                                        <legend class="visually-hidden">Perito elegible</legend>
                                        <div class="table-responsive">
                                            <table class="table sij-tabla table-bordered align-middle">
                                                <thead><tr><th>Perito elegible</th><th>Materias</th><th>Carga</th><th>Selección</th></tr></thead>
                                                <tbody>
                                                <c:forEach var="cand" items="${candidatos}">
                                                    <tr>
                                                        <td class="fw-semibold"><label for="perito${cand.peritoId}"><c:out value="${cand.nombre}"/></label></td>
                                                        <td><c:out value="${cand.materias}"/></td>
                                                        <td><c:out value="${cand.carga}"/></td>
                                                        <td><input class="form-check-input" type="radio" name="peritoId" required
                                                                   id="perito${cand.peritoId}" value="<c:out value='${cand.peritoId}'/>"></td>
                                                    </tr>
                                                </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </fieldset>
                                    <p class="sij-texto-chico">La carga de trabajo no es una restricción para la asignación manual. El estado del peritaje no cambia<c:if test="${peritaje.estado eq 'EN_PROCESO'}"> y el perito nuevo continúa el borrador del dictamen</c:if>.</p>
                                </c:otherwise>
                            </c:choose>
                            <label for="motivo" class="form-label">Justificación de la reasignación *</label>
                            <textarea id="motivo" name="motivo" class="form-control" rows="3" maxlength="500" required
                                      placeholder="Describa el motivo de la reasignación manual"><c:out value="${motivoCapturado}"/></textarea>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-sij-secundario" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-primary"${empty candidatos ? ' disabled' : ''}>Confirmar reasignación</button>
                        </div>
                    </form>
                </div>
            </div>
        </c:if>
    </jsp:body>
</t:layout>
