<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Recuperación de contraseña (RecuperarServlet). Diseño "Recuperación de contraseña" (docs/modulos/img.png).
  Si la cuenta existe y está activa, se manda el enlace para restablecer (con el correo apagado, sale en la consola).
--%>
<t:layout-acceso titulo="Recuperar contraseña">
    <a class="sij-acceso-enlace fw-normal mb-3" href="<c:url value='/login'/>">&larr; Volver al inicio de sesión</a>
    <h1 class="sij-acceso-titulo mb-1">Recupere su acceso</h1>
    <p class="sij-acceso-descripcion mb-3">Indique el correo asociado a su cuenta institucional.</p>

    <form method="post" action="<c:url value='/recuperar'/>" novalidate>
        <n:csrf/>
        <div class="mb-3">
            <label for="correo" class="form-label">Correo institucional</label>
            <input id="correo" name="correo" type="email" maxlength="150" required autofocus autocomplete="username"
                   class="form-control" placeholder="nombre@pjmorelos.gob.mx" value="<c:out value='${correo}'/>">
        </div>
        <button type="submit" class="btn btn-primary">Enviar instrucciones</button>
    </form>

    <%-- Mismo aviso exista o no la cuenta (no revela qué correos están registrados) --%>
    <c:if test="${solicitudRecibida}">
        <div class="sij-acceso-aviso mt-4" role="status">
            <div class="fw-semibold">&#9432; Solicitud recibida</div>
            Si existe una cuenta asociada a este correo, recibirá instrucciones para restablecer su contraseña.
        </div>
    </c:if>
</t:layout-acceso>
