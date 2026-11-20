<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%@ taglib prefix="cop" tagdir="/WEB-INF/tags/copias" %>
<%-- COPIAS_ADMIN: alta, baja y reactivación de los trabajadores del área (COPIAS_PERSONAL) --%>
<t:layout titulo="Trabajadores" modulo="copias" seccion="personal">
    <jsp:attribute name="estilos"><cop:estilos/></jsp:attribute>
    <jsp:body>
        <n:migas><a href="<c:url value='/copias/admin'/>">Administración</a> / Trabajadores</n:migas>
        <n:encabezado titulo="Trabajadores del área"
                      descripcion="Personal que captura, valida, cobra y entrega las copias certificadas."/>

        <div class="row g-3 mt-1">
            <div class="col-lg-5">
                <form method="post" action="<c:url value='/copias/admin/personal'/>" novalidate>
                    <n:csrf/>
                    <n:tarjeta titulo="Agregar trabajador">
                        <div class="mb-3">
                            <label for="nombre" class="form-label">Nombre completo *</label>
                            <input id="nombre" name="nombre" maxlength="150"
                                   class="form-control${not empty errores.nombre ? ' is-invalid' : ''}"
                                   value="<c:out value='${personalForm.nombre}'/>">
                            <n:error campo="nombre"/>
                        </div>
                        <div class="mb-3">
                            <label for="correo" class="form-label">Correo institucional *</label>
                            <input id="correo" name="correo" type="email" maxlength="150"
                                   class="form-control${not empty errores.correo ? ' is-invalid' : ''}"
                                   value="<c:out value='${personalForm.correo}'/>">
                            <n:error campo="correo"/>
                            <%-- TODO núcleo: el núcleo le enviará una contraseña temporal a este correo --%>
                            <div class="form-text">Con este correo iniciará sesión.</div>
                        </div>
                        <div class="d-flex justify-content-end">
                            <button type="submit" class="btn btn-primary">Agregar trabajador</button>
                        </div>
                    </n:tarjeta>
                </form>
            </div>

            <div class="col-lg-7">
                <n:tarjeta titulo="Trabajadores registrados" sinRelleno="true">
                    <c:choose>
                        <c:when test="${empty trabajadores}">
                            <div class="p-4 text-center sij-descripcion">Aún no hay trabajadores registrados.</div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table sij-tabla align-middle mb-0">
                                    <thead><tr><th>Trabajador</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
                                    <tbody>
                                    <c:forEach var="u" items="${trabajadores}">
                                        <tr>
                                            <td>
                                                <div class="fw-semibold"><c:out value="${u.nombre}"/></div>
                                                <div class="sij-texto-chico"><c:out value="${u.correo}"/></div>
                                            </td>
                                            <td><n:estado valor="${u.activo ? 'Activo' : 'Inactivo'}"/></td>
                                            <td class="text-end">
                                                <n:confirmar accion="/copias/admin/personal/estado"
                                                             texto="${u.activo ? '¿Desactivar a ' : '¿Reactivar a '}${u.nombre}?"
                                                             boton="${u.activo ? 'Desactivar' : 'Reactivar'}"
                                                             clase="btn btn-sm ${u.activo ? 'btn-outline-danger' : 'btn-sij-secundario'}">
                                                    <input type="hidden" name="id" value="<c:out value='${u.id}'/>">
                                                    <input type="hidden" name="activo" value="${u.activo ? 'false' : 'true'}">
                                                </n:confirmar>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </n:tarjeta>
            </div>
        </div>
    </jsp:body>
</t:layout>
