<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Editar magistrado" modulo="segunda" seccion="magistrados">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-magistrados-editar">
            <n:migas><a href="<c:url value='/segunda/magistrados'/>">Magistrados</a> / Detalle</n:migas>
            <n:encabezado titulo="Cuenta vinculada" descripcion="<c:out value='${magistrado.correo}'/> · <c:out value='${magistrado.nombreCompleto}'/>">
                <n:estado valor="${magistrado.activo ? 'Activo' : 'Inactivo'}"/>
            </n:encabezado>

            <div class="row g-3 mt-1 justify-content-center">
                <div class="col-lg-8">
                    <n:tarjeta titulo="Datos de la cuenta">
                        <form method="post" action="<c:url value='/segunda/magistrados/editar?id=${magistrado.id}'/>" novalidate>
                            <n:csrf/>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label for="nombre" class="form-label">Nombre *</label>
                                    <input id="nombre" name="nombre" type="text" maxlength="50" required class="form-control${not empty errores.nombre ? ' is-invalid' : ''}" value="<c:out value='${magistradoForm.nombre}'/>">
                                    <n:error campo="nombre"/>
                                </div>
                                <div class="col-md-6">
                                    <label for="apellidos" class="form-label">Apellidos *</label>
                                    <input id="apellidos" name="apellidos" type="text" maxlength="100" required class="form-control${not empty errores.apellidos ? ' is-invalid' : ''}" value="<c:out value='${magistradoForm.apellidos}'/>">
                                    <n:error campo="apellidos"/>
                                </div>
                                <div class="col-md-8">
                                    <label class="form-label">Correo institucional</label>
                                    <input class="form-control" value="<c:out value='${magistrado.correo}'/>" readonly>
                                </div>
                                <div class="col-md-4 d-flex align-items-end">
                                    <button type="submit" class="btn btn-primary w-100">Guardar datos</button>
                                </div>
                            </div>
                            <p class="form-text mb-0 mt-3">El correo y el rol pertenecen al login central y no se cambian desde esta pantalla.</p>
                        </form>
                    </n:tarjeta>

                    <form method="post" action="<c:url value='/segunda/magistrados/salas'/>">
                        <n:csrf/>
                        <input type="hidden" name="id" value="<c:out value='${magistrado.id}'/>">
                        <n:tarjeta titulo="Sala asignada">
                            <c:choose>
                                <c:when test="${magistrado.sinSala}"><p class="sij-descripcion">Sin sala asignada.</p></c:when>
                                <c:otherwise><p class="mb-2"><span class="fw-semibold"><c:out value="${magistrado.salaNombre}"/></span></p></c:otherwise>
                            </c:choose>
                            <div class="row g-2 align-items-end">
                                <div class="col-md-8">
                                    <label for="salaId" class="form-label">Asignar a otra sala</label>
                                    <select id="salaId" name="salaId" class="form-select">
                                        <option value="">Seleccione una sala</option>
                                        <c:forEach var="s" items="${salas}">
                                            <option value="<c:out value='${s.id}'/>"${s.id eq magistrado.salaId ? ' selected' : ''}>
                                                <c:out value="${s.nombre}"/> · <c:out value="${s.distritoNombre}"/>
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-4"><button type="submit" class="btn btn-sij-secundario w-100">Asignar sala</button></div>
                            </div>
                            <c:if test="${not empty errorSala}"><div class="sij-texto-chico mt-2"><span class="text-danger"><c:out value="${errorSala}"/></span></div></c:if>
                        </n:tarjeta>
                    </form>

                    <form method="post" action="<c:url value='/segunda/magistrados/estado'/>">
                        <n:csrf/>
                        <input type="hidden" name="id" value="<c:out value='${magistrado.id}'/>">
                        <n:tarjeta titulo="Estado del magistrado">
                            <div class="btn-group d-flex mb-3" style="max-width:220px" role="group" aria-label="Estado del magistrado">
                                <input type="radio" class="btn-check per-segmento" name="activo" id="estadoActivo" value="true"${magistrado.activo ? ' checked' : ''}>
                                <label class="btn btn-sm px-3" for="estadoActivo">Activo</label>
                                <input type="radio" class="btn-check per-segmento" name="activo" id="estadoInactivo" value="false"${magistrado.activo ? '' : ' checked'}>
                                <label class="btn btn-sm px-3" for="estadoInactivo">Inactivo</label>
                            </div>
                            <div class="sij-aviso sij-aviso-advertencia p-3 mb-2">
                                <div class="fw-semibold">Al desactivar al magistrado</div>
                                Su sala será liberada y no podrá recibir nuevas tocas. No se puede desactivar si tiene tocas pendientes de resolución.
                            </div>
                            <c:if test="${not empty errorEstado}"><div class="sij-texto-chico mb-2"><span class="text-danger"><c:out value="${errorEstado}"/></span></div></c:if>
                            <button type="submit" class="btn btn-sij-secundario">Aplicar cambio de estado</button>
                        </n:tarjeta>
                    </form>

                    <div class="d-flex justify-content-end sin-form-acciones"><a class="btn btn-sij-secundario" href="<c:url value='/segunda/magistrados'/>">Volver a magistrados</a></div>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>
