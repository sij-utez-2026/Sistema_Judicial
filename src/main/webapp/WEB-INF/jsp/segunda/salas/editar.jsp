<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- RF 3.3 Edición y RF 3.4 Baja/reactivación de sala --%>
<t:layout titulo="Editar sala" modulo="segunda" seccion="salas">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script src="<c:url value='/recursos/segunda/js/salas.js?v=1'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-salas-editar">
    <n:migas><a href="<c:url value='/segunda/salas'/>">Salas</a> / Editar sala</n:migas>
    <n:encabezado titulo="Editar sala"
                  descripcion="${sala.nombre} · ${sala.distritoNombre}">
        <n:estado valor="${sala.activa ? 'Activa' : 'Inactiva'}"/>
    </n:encabezado>

    <div class="row g-3 mt-1 justify-content-center">
        <div class="col-lg-8">

                <%-- Datos. El botón de guardar está al final (form="formDatos") --%>
            <form id="formDatos" method="post"
                  action="<c:url value='/segunda/salas/editar?id=${sala.id}'/>" novalidate>
                <n:csrf/>
                <n:tarjeta titulo="Datos de la sala">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label for="nombre" class="form-label">Nombre de la sala *</label>
                            <input id="nombre" name="nombre" type="text" maxlength="100"
                                   class="form-control${not empty errores.nombre ? ' is-invalid' : ''}"
                                   value="<c:out value='${salaForm.nombre}'/>">
                            <n:error campo="nombre"/>
                        </div>
                        <div class="col-md-6">
                            <label for="distritoId" class="form-label">Distrito judicial *</label>
                            <select id="distritoId" name="distritoId"
                                    class="form-select${not empty errores.distritoId ? ' is-invalid' : ''}">
                                <option value="">Seleccione un distrito</option>
                                <c:forEach var="d" items="${distritos}">
                                    <option value="<c:out value='${d.clave}'/>"${d.clave eq salaForm.distritoId ? ' selected' : ''}>
                                        <c:out value="${d.nombre}"/>
                                    </option>
                                </c:forEach>
                            </select>
                            <n:error campo="distritoId"/>
                            <div class="form-text">No se puede cambiar si la sala ya tiene tocas asignadas.</div>
                        </div>
                        <div class="col-12">
                            <label for="direccion" class="form-label">Dirección *</label>
                            <input id="direccion" name="direccion" type="text" maxlength="255"
                                   class="form-control${not empty errores.direccion ? ' is-invalid' : ''}"
                                   value="<c:out value='${salaForm.direccion}'/>">
                            <n:error campo="direccion"/>
                        </div>
                    </div>
                </n:tarjeta>
            </form>

                <%-- RF 3.4 Estado (formulario aparte) --%>
            <form method="post" action="<c:url value='/segunda/salas/estado'/>" data-formulario-estado
                  data-estado-actual="<c:out value='${sala.activa}'/>"
                  data-nombre="<c:out value='${sala.nombre}'/>">
                <n:csrf/>
                <input type="hidden" name="id" value="<c:out value='${sala.id}'/>">
                <n:tarjeta titulo="Estado de la sala">
                    <div class="btn-group d-flex mb-3" style="max-width: 220px;" role="group"
                         aria-label="Estado de la sala">
                        <input type="radio" class="btn-check per-segmento" name="activa" id="estadoActiva"
                               value="true"${sala.activa ? ' checked' : ''}>
                        <label class="btn btn-sm px-3" for="estadoActiva">Activa</label>
                        <input type="radio" class="btn-check per-segmento" name="activa" id="estadoInactiva"
                               value="false"${sala.activa ? '' : ' checked'}>
                        <label class="btn btn-sm px-3" for="estadoInactiva">Inactiva</label>
                    </div>

                    <c:if test="${sala.activa}">
                        <div class="sij-aviso sij-aviso-advertencia p-3 mb-2">
                            <div class="fw-semibold">&#9432; Al desactivar la sala</div>
                            Una sala inactiva no aparecerá en las asignaciones de tocas. No se puede
                            desactivar si tiene tocas pendientes de resolución.
                        </div>
                    </c:if>

                    <c:if test="${not empty errorEstado}">
                        <div class="sij-texto-chico mb-2">
                            <span class="text-danger"><c:out value="${errorEstado}"/></span><br>
                            El cambio de estado no se aplicó.
                        </div>
                    </c:if>

                    <button type="submit" class="btn btn-sij-secundario" data-aplicar-estado>
                        Aplicar cambio de estado
                    </button>
                </n:tarjeta>
            </form>

            <div class="d-flex justify-content-end gap-2 sin-form-acciones">
                <a class="btn btn-sij-secundario" href="<c:url value='/segunda/salas'/>">Cancelar</a>
                <button type="submit" form="formDatos" class="btn btn-primary">Guardar datos de la sala</button>
            </div>
        </div>
    </div>

        </div>
    </jsp:body>
</t:layout>
