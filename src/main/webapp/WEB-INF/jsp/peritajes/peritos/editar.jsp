<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- RF 3.3 Edición y RF 3.4 Baja/reactivación de perito (boceto docs/modulos/peritajes-figma-editar.png) --%>
<t:layout titulo="Editar perito" modulo="peritajes" seccion="peritos">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/peritajes/css/peritajes.css'/>">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script src="<c:url value='/recursos/peritajes/js/peritos.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <n:migas><a href="<c:url value='/peritajes/peritos'/>">Catálogo de peritos</a> / Editar perito</n:migas>
        <n:encabezado titulo="Editar perito"
                      descripcion="${perito.nombreCompleto} · Registro ${perito.numeroRegistro}">
            <n:estado valor="${perito.activo ? 'Activo' : 'Inactivo'}"/>
        </n:encabezado>

        <div class="row g-3 mt-1">
            <div class="col-lg-8">

                <%-- Datos del perito. El botón de guardar está al final de la página (atributo form="formDatos") --%>
                <form id="formDatos" method="post" action="<c:url value='/peritajes/peritos/editar?id=${perito.id}'/>" novalidate>
                    <n:csrf/>
                    <n:tarjeta titulo="Datos del perito">
                        <div class="row g-3">
                            <div class="col-md-4">
                                <label for="nombre" class="form-label">Nombre(s) *</label>
                                <input id="nombre" name="nombre" type="text" maxlength="80" class="form-control${not empty errores.nombre ? ' is-invalid' : ''}"
                                       value="<c:out value='${peritoForm.nombre}'/>">
                                <n:error campo="nombre"/>
                            </div>
                            <div class="col-md-4">
                                <label for="apellidoPaterno" class="form-label">Apellido paterno *</label>
                                <input id="apellidoPaterno" name="apellidoPaterno" type="text" maxlength="60" class="form-control${not empty errores.apellidoPaterno ? ' is-invalid' : ''}"
                                       value="<c:out value='${peritoForm.apellidoPaterno}'/>">
                                <n:error campo="apellidoPaterno"/>
                            </div>
                            <div class="col-md-4">
                                <label for="apellidoMaterno" class="form-label">Apellido materno</label>
                                <input id="apellidoMaterno" name="apellidoMaterno" type="text" maxlength="60" class="form-control${not empty errores.apellidoMaterno ? ' is-invalid' : ''}"
                                       value="<c:out value='${peritoForm.apellidoMaterno}'/>">
                                <n:error campo="apellidoMaterno"/>
                            </div>

                            <%-- Correo y número de registro: fijos (el service no los cambia) --%>
                            <div class="col-md-7">
                                <label for="correo" class="form-label">Correo institucional</label>
                                <input id="correo" name="correo" type="email" readonly class="form-control"
                                       value="<c:out value='${peritoForm.correo}'/>">
                            </div>
                            <div class="col-md-5">
                                <label for="numeroRegistro" class="form-label">Número de registro</label>
                                <input id="numeroRegistro" name="numeroRegistro" type="text" readonly class="form-control"
                                       value="<c:out value='${peritoForm.numeroRegistro}'/>">
                            </div>

                            <div class="col-md-7">
                                <label for="telefono" class="form-label">Teléfono de contacto *</label>
                                <input id="telefono" name="telefono" type="tel" maxlength="14" data-telefono="data-telefono" class="form-control${not empty errores.telefono ? ' is-invalid' : ''}"
                                       value="<c:out value='${peritoForm.telefono}'/>">
                                <n:error campo="telefono"/>
                            </div>

                            <%-- Especialidades en texto libre: una o varias (RF 3.1 y 3.3) --%>
                            <c:set var="errorEspecialidad" value="${not empty errores.especialidadCapturada}"/>
                            <div class="col-12">
                                <label class="form-label" for="especialidad0">Especialidades *</label>
                                <div data-especialidades>
                                    <c:forEach var="esp" items="${peritoForm.especialidades}" varStatus="st">
                                        <div class="input-group mb-2 per-especialidad">
                                            <input type="text" class="form-control${errorEspecialidad ? ' is-invalid' : ''}" maxlength="100"
                                                   id="especialidad${st.index}" name="especialidades[${st.index}]" value="<c:out value='${esp}'/>"
                                                   placeholder="Área o estudios del perito, por ejemplo: Psicología forense">
                                            <button type="button" class="btn btn-sij-secundario" data-quitar-especialidad
                                                    title="Quitar especialidad" aria-label="Quitar especialidad">&times;</button>
                                        </div>
                                    </c:forEach>
                                </div>
                                <n:error campo="especialidadCapturada" clase="text-danger small mb-1"/>
                                <button type="button" class="btn btn-sm btn-sij-secundario" data-agregar-especialidad>+ Agregar otra especialidad</button>
                            </div>

                            <fieldset class="col-12">
                                <legend class="form-label fs-6">Materias *</legend>
                                <div class="d-flex flex-wrap gap-2">
                                    <c:forEach var="m" items="${materias}">
                                        <span class="per-opcion">
                                            <input type="checkbox" name="materiasIds" value="${m.id}" id="materia${m.id}" autocomplete="off"
                                                   class="btn-check per-materia"${peritoForm.materiasIds.contains(m.id) ? ' checked' : ''}>
                                            <label class="btn btn-sm" for="materia${m.id}"><c:out value="${m.nombre}"/></label>
                                        </span>
                                    </c:forEach>
                                </div>
                                <n:error campo="materiasIds" clase="text-danger small mt-1"/>
                                <div class="form-text"><span data-contador-materias></span>
                                    Cambiar las materias no afecta los peritajes ya asignados.</div>
                            </fieldset>
                        </div>
                    </n:tarjeta>
                </form>

                <%-- RF 3.4 Estado en el padrón (formulario aparte). peritos.js arma el texto de confirmación. --%>
                <form method="post" action="<c:url value='/peritajes/peritos/estado'/>" data-formulario-estado
                      data-estado-actual="<c:out value='${perito.activo}'/>" data-nombre="<c:out value='${perito.nombreCompleto}'/>">
                    <n:csrf/>
                    <input type="hidden" name="id" value="<c:out value='${perito.id}'/>">
                    <n:tarjeta titulo="Estado en el padrón">
                        <div class="btn-group d-flex mb-3" style="max-width: 220px;" role="group" aria-label="Estado del perito">
                            <input type="radio" class="btn-check per-segmento" name="activo" id="estadoActivo"
                                   value="true"${perito.activo ? ' checked' : ''}>
                            <label class="btn btn-sm px-3" for="estadoActivo">Activo</label>
                            <input type="radio" class="btn-check per-segmento" name="activo" id="estadoInactivo"
                                   value="false"${perito.activo ? '' : ' checked'}>
                            <label class="btn btn-sm px-3" for="estadoInactivo">Inactivo</label>
                        </div>

                        <%-- Aviso si tiene carga activa: no se puede desactivar --%>
                        <c:if test="${perito.activo and carga.total() gt 0}">
                            <div class="sij-aviso sij-aviso-advertencia p-3 mb-2">
                                <div class="fw-semibold">&#9432; No es posible desactivar al perito</div>
                                Tiene <c:out value="${carga.total()}"/> peritajes con carga activa:
                                <c:out value="${carga.asignados()}"/> Asignado y
                                <c:out value="${carga.enProceso()}"/> En Proceso.
                                Deben concluirse o ser reasignados por un usuario autorizado antes de cambiar a Inactivo.
                            </div>
                        </c:if>

                        <%-- Resultado de un intento fallido de baja --%>
                        <c:if test="${not empty errorEstado}">
                            <div class="sij-texto-chico mb-2">
                                <span class="text-danger"><c:out value="${errorEstado}"/></span><br>
                                El cambio a Inactivo no se aplicó.
                                <c:out value="${perito.nombreCompleto}"/> conserva el estado Activo.
                            </div>
                        </c:if>

                        <%-- peritos.js solo lo activa si se eligió otro estado --%>
                        <button type="submit" class="btn btn-sij-secundario" data-aplicar-estado>Aplicar cambio de estado</button>
                    </n:tarjeta>
                </form>

                <div class="d-flex justify-content-end gap-2">
                    <a class="btn btn-sij-secundario" href="<c:url value='/peritajes/peritos'/>">Cancelar</a>
                    <button type="submit" form="formDatos" class="btn btn-primary">Guardar datos del perito</button>
                </div>
            </div>

            <%-- Carga activa: peritajes Asignados y En Proceso --%>
            <div class="col-lg-4">
                <n:tarjeta titulo="Carga activa: ${carga.total()}">
                    <c:choose>
                        <c:when test="${empty peritajesActivos}">
                            <p class="sij-descripcion">Sin peritajes Asignados ni En Proceso.</p>
                        </c:when>
                        <c:otherwise>
                            <table class="table sij-tabla table-bordered mb-2">
                                <thead><tr><th>Folio</th><th>Estado</th></tr></thead>
                                <tbody>
                                <c:forEach var="pa" items="${peritajesActivos}">
                                    <tr>
                                        <td class="fw-semibold"><c:out value="${pa.folio()}"/></td>
                                        <td><n:estado valor="${pa.estado()}"/></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                    <div class="sij-texto-chico">Los dictámenes Entregados no forman parte de la carga activa.</div>
                </n:tarjeta>
            </div>
        </div>
    </jsp:body>
</t:layout>
