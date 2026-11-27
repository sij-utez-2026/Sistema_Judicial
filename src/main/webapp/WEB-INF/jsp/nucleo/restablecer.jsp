<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Contraseña nueva desde el enlace del correo de recuperación (RestablecerServlet). INTEGRADORES.
  enlaceValido = false: el enlace no existe, ya venció (15 minutos) o ya se usó.
  El error de la contraseña (regla o confirmación) lo muestra <n:mensajes/> del layout.
--%>
<t:layout-acceso titulo="Restablecer contraseña">
    <a class="sij-acceso-enlace fw-normal mb-3" href="<c:url value='/login'/>">&larr; Volver al inicio de sesión</a>
    <h1 class="sij-acceso-titulo mb-1">Cree su contraseña nueva</h1>

    <c:choose>
        <c:when test="${enlaceValido}">
            <p class="sij-acceso-descripcion mb-3">
                Cuenta: <strong><c:out value="${correo}"/></strong>.
                Al menos 8 caracteres, con una mayúscula, una minúscula y un número.
            </p>
            <form method="post" action="<c:url value='/recuperar/restablecer'/>" novalidate>
                <n:csrf/>
                <input type="hidden" name="token" value="<c:out value='${token}'/>">
                <div class="mb-3">
                    <label for="nueva" class="form-label">Contraseña nueva</label>
                    <n:contrasena nombre="nueva" autocompletar="new-password" minimo="8" autofocus="true"/>
                </div>
                <div class="mb-3">
                    <label for="confirmacion" class="form-label">Confirme la contraseña nueva</label>
                    <n:contrasena nombre="confirmacion" autocompletar="new-password" minimo="8"/>
                </div>
                <button type="submit" class="btn btn-primary">Guardar contraseña</button>
            </form>
        </c:when>
        <c:otherwise>
            <div class="sij-acceso-aviso mt-3" role="status">
                <div class="fw-semibold">&#9432; El enlace ya no es válido</div>
                Venció (dura 15 minutos) o ya se usó. Pida uno nuevo.
            </div>
            <a class="btn btn-primary mt-3" href="<c:url value='/recuperar'/>">Pedir otro enlace</a>
        </c:otherwise>
    </c:choose>
</t:layout-acceso>
