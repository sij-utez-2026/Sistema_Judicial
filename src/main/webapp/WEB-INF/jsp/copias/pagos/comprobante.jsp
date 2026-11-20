<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%@ taglib prefix="cop" tagdir="/WEB-INF/tags/copias" %>
<%-- RF 4.1 Comprobante imprimible del pago simulado (sin INE ni partes del expediente) --%>
<t:layout titulo="Comprobante ${pago.folioPago}" modulo="copias" seccion="solicitudes">
    <jsp:attribute name="estilos"><cop:estilos/></jsp:attribute>
    <jsp:attribute name="scripts"><script src="<c:url value='/recursos/copias/js/copias.js?v=2'/>"></script></jsp:attribute>
    <jsp:body>
        <div class="cop-no-imprimir">
            <n:migas>
                <a href="<c:url value='/copias/solicitudes'/>">Registro de solicitudes</a> /
                <a href="<c:url value='/copias/solicitudes/detalle?id=${solicitud.id}'/>"><c:out value="${solicitud.folio}"/></a> /
                Comprobante
            </n:migas>

            <n:encabezado titulo="Comprobante de pago"
                          descripcion="Folio de pago ${pago.folioPago} · Conserve este comprobante para la entrega física.">
                <button type="button" class="btn btn-primary" data-imprimir>Imprimir comprobante</button>
                <form method="post" action="<c:url value='/copias/pagos/reenviar'/>" class="d-inline">
                    <n:csrf/>
                    <input type="hidden" name="id" value="<c:out value='${solicitud.id}'/>">
                    <button type="submit" class="btn btn-sij-secundario">Reenviar por correo</button>
                </form>
                <a class="btn btn-sij-secundario" href="<c:url value='/copias/solicitudes/detalle?id=${solicitud.id}'/>">Volver al detalle</a>
            </n:encabezado>
        </div>

        <n:tarjeta clase="cop-comprobante mt-3">
            <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 pb-3 mb-3 cop-comprobante-encabezado">
                <div>
                    <div class="sij-texto-chico text-uppercase">Sistema Judicial Integral · Copias Certificadas</div>
                    <h2 class="sij-tarjeta-titulo mb-1">Comprobante de pago</h2>
                    <div class="sij-texto-chico">
                        Folio del comprobante: <strong class="text-dark"><c:out value="${pago.folioPago}"/></strong>
                    </div>
                </div>
                <div class="text-md-end">
                    <n:estado valor="${solicitud.estado.texto}" color="${solicitud.estado.color}"/>
                    <div class="cop-checkout-total mt-2">
                        <c:out value="${pago.montoTexto}"/> <span>MXN</span>
                    </div>
                </div>
            </div>

            <div class="cop-dato"><span>Folio de la solicitud</span><strong><c:out value="${solicitud.folio}"/></strong></div>
            <div class="cop-dato">
                <span>Expediente</span>
                <strong><c:out value="${solicitud.numeroExpediente}"/> (<c:out value="${solicitud.tipoTexto}"/>)</strong>
            </div>
            <div class="cop-dato"><span>Juzgado</span><strong><c:out value="${solicitud.juzgado}"/></strong></div>
            <div class="cop-dato"><span>Materia</span><strong><c:out value="${solicitud.materia}"/></strong></div>
            <div class="cop-dato"><span>Solicitante</span><strong><c:out value="${solicitud.solicitanteNombre}"/></strong></div>
            <div class="cop-dato"><span>RFC</span><strong><c:out value="${solicitud.solicitanteRfc}"/></strong></div>
            <div class="cop-dato"><span>Correo</span><strong><c:out value="${solicitud.solicitanteCorreo}"/></strong></div>
            <div class="cop-dato"><span>Fecha de pago</span><strong><c:out value="${pago.fechaTexto}"/></strong></div>
            <div class="cop-dato"><span>Importe pagado</span><strong><c:out value="${pago.montoTexto}"/> MXN</strong></div>
            <div class="cop-dato"><span>Estado actual</span><strong><c:out value="${solicitud.estado.texto}"/></strong></div>

            <div class="cop-aviso mt-4 mb-0">
                <strong>Pago simulado — no se realiza ningún cargo.</strong>
                La entrega de las copias certificadas es únicamente física en ventanilla presentando este comprobante y una identificación oficial vigente.
            </div>
        </n:tarjeta>
    </jsp:body>
</t:layout>
