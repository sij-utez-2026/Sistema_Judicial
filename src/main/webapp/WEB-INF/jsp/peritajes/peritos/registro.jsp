<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- RF 3.1 Registro de perito (boceto docs/modulos/peritajes-figma-registro.png) --%>
<t:layout titulo="Registrar perito" modulo="peritajes" seccion="peritos">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/peritajes/css/peritajes.css'/>">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script src="<c:url value='/recursos/peritajes/js/peritos.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <n:migas><a href="<c:url value='/peritajes/peritos'/>">Catálogo de peritos</a> / Registrar perito</n:migas>
        <n:encabezado titulo="Registrar perito"
                      descripcion="Capture los datos del padrón. Los campos con * son obligatorios."/>

        <%-- Los errores de validación llegan en "errores" (campo → mensaje) y se muestran con <n:error> --%>
        <form method="post" action="<c:url value='/peritajes/peritos/nuevo'/>" novalidate class="mt-3">
            <n:csrf/>

            <n:tarjeta titulo="Datos de identificación">
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

                    <div class="col-md-6">
                        <label for="correo" class="form-label">Correo institucional *</label>
                        <input id="correo" name="correo" type="email" maxlength="150" class="form-control${not empty errores.correo ? ' is-invalid' : ''}"
                               value="<c:out value='${peritoForm.correo}'/>">
                        <n:error campo="correo"/>
                        <div class="form-text">No se podrá cambiar después del registro.</div>
                    </div>
                    <div class="col-md-6">
                        <label for="telefono" class="form-label">Teléfono de contacto *</label>
                        <input id="telefono" name="telefono" type="tel" maxlength="14" placeholder="777 000 0000" data-telefono="data-telefono" class="form-control${not empty errores.telefono ? ' is-invalid' : ''}"
                               value="<c:out value='${peritoForm.telefono}'/>">
                        <n:error campo="telefono"/>
                    </div>

                    <div class="col-12">
                        <label for="numeroRegistro" class="form-label">Número de registro *</label>
                        <input id="numeroRegistro" name="numeroRegistro" type="text" maxlength="8" placeholder="PER-0000" data-mayusculas="data-mayusculas" class="form-control${not empty errores.numeroRegistro ? ' is-invalid' : ''}"
                               value="<c:out value='${peritoForm.numeroRegistro}'/>">
                        <n:error campo="numeroRegistro"/>
                        <div class="form-text">Identificador único dentro del padrón de peritos. No se podrá cambiar después del registro.</div>
                    </div>
                </div>
            </n:tarjeta>

            <n:tarjeta titulo="Perfil pericial">
                <%-- Especialidades en texto libre: una o varias (RF 3.1 y 3.3). peritos.js agrega y quita renglones. --%>
                <c:set var="errorEspecialidad" value="${not empty errores.especialidadCapturada}"/>
                <div class="mb-3">
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

                <fieldset class="mb-3">
                    <legend class="form-label fs-6">Materias de intervención *</legend>
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
                    <div class="form-text" data-contador-materias>Seleccione al menos una materia para la asignación.</div>
                </fieldset>

                <div class="d-flex justify-content-between align-items-center border-top pt-3">
                    <span class="form-label mb-0">Estado inicial</span>
                    <n:estado valor="Activo"/>
                </div>
            </n:tarjeta>

            <div class="d-flex justify-content-end gap-2">
                <a class="btn btn-sij-secundario" href="<c:url value='/peritajes/peritos'/>">Cancelar</a>
                <button type="submit" class="btn btn-primary">Guardar perito</button>
            </div>
        </form>
    </jsp:body>
</t:layout>
