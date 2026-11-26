<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Registrar magistrado" modulo="segunda" seccion="magistrados">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-magistrados-registro">
            <n:migas><a href="<c:url value='/segunda/magistrados'/>">Magistrados</a> / Nueva cuenta</n:migas>
            <n:encabezado titulo="Registrar magistrado"
                          descripcion="Cree una cuenta institucional nueva con rol SEGUNDA_MAGISTRADO y asígnela opcionalmente a una sala."/>

            <form method="post" action="<c:url value='/segunda/magistrados/nuevo'/>" novalidate class="mt-3">
                <n:csrf/>
                <n:tarjeta titulo="Datos de la nueva cuenta y adscripción">
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
                            <label for="correo" class="form-label">Correo institucional *</label>
                            <input id="correo" name="correo" type="email" maxlength="150" required autocomplete="email"
                                   class="form-control${not empty errores.correo ? ' is-invalid' : ''}"
                                   value="<c:out value='${magistradoForm.correo}'/>">
                            <n:error campo="correo"/>
                            <div class="form-text">Se creará una cuenta nueva en <code>user_Seg</code> y <code>Login</code>, con contraseña temporal.</div>
                        </div>
                        <div class="col-md-4">
                            <label for="salaId" class="form-label">Sala (opcional)</label>
                            <select id="salaId" name="salaId" class="form-select${not empty errores.salaId ? ' is-invalid' : ''}">
                                <option value="">Seleccione una sala</option>
                                <c:forEach var="s" items="${salas}">
                                    <option value="<c:out value='${s.id}'/>"${s.id eq magistradoForm.salaId ? ' selected' : ''}>
                                        <c:out value="${s.nombre}"/>
                                    </option>
                                </c:forEach>
                            </select>
                            <n:error campo="salaId"/>
                            <div class="form-text">La sala puede asignarse ahora o después desde la administración.</div>
                        </div>
                    </div>
                </n:tarjeta>
                <div class="d-flex justify-content-end gap-2 sin-form-acciones">
                    <a class="btn btn-sij-secundario" href="<c:url value='/segunda/magistrados'/>">Cancelar</a>
                    <button type="submit" class="btn btn-primary">Crear cuenta</button>
                </div>
            </form>
        </div>
    </jsp:body>
</t:layout>
