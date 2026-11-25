<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Inicio de sesión de todo el sistema (LoginServlet). Diseño "Acceso institucional" (docs/modulos/img.png).
  El error ("Credenciales incorrectas", "Cuenta inactiva" o cuenta bloqueada) lo muestra <n:mensajes/>.
--%>
<t:layout-acceso titulo="Iniciar sesión">
    <h1 class="sij-acceso-titulo">Bienvenido al Sistema Judicial Integral</h1>
    <form method="post" action="<c:url value='/login'/>" novalidate>
        <n:csrf/>
        <div class="mb-3">
            <label for="correo" class="form-label">Correo institucional</label>
            <input id="correo" name="correo" type="email" maxlength="150" required autofocus autocomplete="username"
                   class="form-control" placeholder="nombre@pjmorelos.gob.mx" value="<c:out value='${correo}'/>">
        </div>
        <div class="mb-3">
            <label for="contrasena" class="form-label">Contraseña</label>
            <n:contrasena nombre="contrasena" autocompletar="current-password" placeholder="Ingrese su contraseña"/>
        </div>
        <a class="sij-acceso-enlace mb-4" href="<c:url value='/recuperar'/>">¿Olvidó su contraseña?</a>
        <div class="text-center">
            <button type="submit" class="btn btn-primary">&rarr;&nbsp; Iniciar sesión</button>
        </div>
    </form>
</t:layout-acceso>
