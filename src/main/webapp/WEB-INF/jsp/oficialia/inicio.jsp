<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Inicio" modulo="oficialia" seccion="inicio">
    <n:migas>Inicio</n:migas>
    <n:encabezado titulo="Hola, ${usuarioActual.nombre}"
                  descripcion="${usuarioActual.rol} · ${moduloActual.nombreLargo}"/>

    <n:tarjeta titulo="Bienvenido a Oficialía de Partes" clase="mt-3">
        <p class="sij-descripcion mb-2">
            Has iniciado sesión con el rol <c:out value="${usuarioSesion.rol}"/>.
            Las pantallas operativas de demandas y personal estarán disponibles en las opciones del menú lateral.
        </p>
        <n:estado valor="Sesión activa"/>
    </n:tarjeta>
</t:layout>
