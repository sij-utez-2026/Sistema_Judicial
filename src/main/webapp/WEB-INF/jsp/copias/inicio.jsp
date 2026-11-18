<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Inicio del módulo (/copias). EQUIPO 5.
  Mantiene la pantalla institucional intacta y enlaza al portal público de pago con Stripe Checkout.
--%>
<t:layout titulo="Inicio" modulo="copias" seccion="inicio">
    <n:migas>Inicio</n:migas>
    <n:encabezado titulo="${moduloActual.nombre}" descripcion="${moduloActual.descripcion}">
        <a class="btn btn-primary" href="<c:url value='/copias/publico/pago'/>">Ir al pago de copias</a>
    </n:encabezado>

    <n:tarjeta titulo="Pantalla de inicio de ejemplo" clase="mt-3">
        <p class="sij-descripcion">
            Equipo 5: reemplacen este contenido por el inicio según el rol que pide su DFR
            (<code>docs/modulos/copias-certificadas.md</code>, sección 8). Las opciones del menú lateral están en
            <code>src/main/webapp/WEB-INF/jsp/copias/menu.jsp</code>.
        </p>
        <n:estado valor="Pendiente"/>
    </n:tarjeta>

    <n:tarjeta titulo="Pago en línea (Stripe Checkout · MXN)" clase="mt-3">
        <p class="sij-descripcion mb-3">
            Consulte un folio de solicitud autorizado y realice el cobro en moneda MXN mediante la pantalla oficial de Stripe Checkout.
        </p>
        <a class="btn btn-primary" href="<c:url value='/copias/publico/pago'/>">Abrir portal de pago</a>
    </n:tarjeta>
</t:layout>
