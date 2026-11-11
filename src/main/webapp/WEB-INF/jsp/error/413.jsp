<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Página de error 413: archivo demasiado grande (la elige web.xml). INTEGRADORES. --%>
<t:layout-publico titulo="Archivo demasiado grande">
    <n:tarjeta clase="text-center mx-auto mt-4" >
        <div class="display-6 fw-semibold text-secondary">413</div>
        <h1 class="sij-titulo mt-2">Archivo demasiado grande</h1>
        <p class="sij-descripcion">El archivo que intentó subir pasa del tamaño máximo permitido. Regrese, elija un archivo más pequeño y vuelva a enviar el formulario.</p>
        <a class="btn btn-primary" href="<c:url value='/'/>">Ir al inicio</a>
    </n:tarjeta>
</t:layout-publico>
