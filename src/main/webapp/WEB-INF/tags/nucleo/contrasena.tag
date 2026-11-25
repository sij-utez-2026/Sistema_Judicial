<%@ tag description="Campo de contraseña con botón de ojo para ver lo escrito" pageEncoding="UTF-8" %>
<%--
    <n:contrasena nombre="contrasena" autocompletar="current-password" placeholder="Ingrese su contraseña"/>
    <n:contrasena nombre="nueva" autocompletar="new-password" minimo="8"/>
  Solo el campo (la etiqueta <label for="..."> la pone la pantalla). El ojo cambia el campo entre oculto y
  visible (comun.js, data-ver-contrasena); sin JavaScript el campo funciona normal y el botón no hace nada.
--%>
<%@ attribute name="nombre" required="true" %>
<%@ attribute name="autocompletar" required="false" %>
<%@ attribute name="placeholder" required="false" %>
<%@ attribute name="minimo" required="false" %>
<%@ attribute name="autofocus" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<div class="sij-campo-contrasena">
    <input id="<c:out value='${nombre}'/>" name="<c:out value='${nombre}'/>" type="password" required class="form-control"
           <c:if test="${not empty autocompletar}">autocomplete="<c:out value='${autocompletar}'/>"</c:if>
           <c:if test="${not empty placeholder}">placeholder="<c:out value='${placeholder}'/>"</c:if>
           <c:if test="${not empty minimo}">minlength="<c:out value='${minimo}'/>"</c:if>
           <c:if test="${autofocus}">autofocus</c:if>>
    <button type="button" class="sij-ver-contrasena" data-ver-contrasena="<c:out value='${nombre}'/>"
            aria-controls="<c:out value='${nombre}'/>" aria-label="Mostrar contraseña" aria-pressed="false">
        <span data-icono-mostrar><n:icono nombre="ojo" tamano="18"/></span>
        <span data-icono-ocultar hidden><n:icono nombre="ojo-tachado" tamano="18"/></span>
    </button>
</div>
