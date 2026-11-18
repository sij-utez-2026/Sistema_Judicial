<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Comprobante oficial del pago realizado mediante Stripe Checkout en MXN (RF 4.1) --%>
<t:layout-publico titulo="Comprobante de pago" subtitulo="Copias Certificadas · SGCC">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/copias/css/copias.css'/>">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script src="<c:url value='/recursos/copias/js/copias.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <div class="cop-no-imprimir">
            <n:migas>
                <a href="<c:url value='/copias/publico/pago'/>">Pago en línea</a> / Comprobante
            </n:migas>

            <n:encabezado titulo="Comprobante de pago"
                          descripcion="Conserve este comprobante junto con su folio para recoger sus copias en ventanilla.">
                <button type="button" class="btn btn-primary" data-imprimir-comprobante>Imprimir / Guardar PDF</button>
                <a class="btn btn-sij-secundario" href="<c:url value='/copias/publico/pago'/>">Nueva consulta</a>
            </n:encabezado>
        </div>

        <n:tarjeta clase="mt-3 cop-comprobante">
            <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 border-bottom pb-3 mb-4">
                <div>
                    <div class="sij-texto-chico text-uppercase">Sistema Judicial Integral · Copias Certificadas</div>
                    <h2 class="sij-tarjeta-titulo mb-1">Comprobante oficial de pago</h2>
                    <div class="sij-texto-chico">Folio de pago: <strong class="text-dark"><c:out value="${pago.folioPago}"/></strong></div>
                </div>
                <div class="text-md-end">
                    <n:estado valor="Pagado"/>
                    <div class="cop-monto-destacado mt-2"><c:out value="${pago.montoFormateado}"/></div>
                </div>
            </div>

            <div class="row g-3">
                <div class="col-md-6">
                    <div class="sij-texto-chico">Folio de solicitud</div>
                    <div class="fw-semibold"><c:out value="${pago.folioSolicitud}"/></div>
                </div>
                <div class="col-md-6">
                    <div class="sij-texto-chico">Número de expediente</div>
                    <div class="fw-semibold"><c:out value="${pago.numeroExpediente}"/></div>
                </div>
                <c:if test="${not empty solicitud}">
                    <div class="col-md-6">
                        <div class="sij-texto-chico">Juzgado / Materia</div>
                        <div>
                            <c:out value="${solicitud.juzgadoNombre}"/>
                            <c:if test="${not empty solicitud.materiaNombre}">
                                · <c:out value="${solicitud.materiaNombre}"/>
                            </c:if>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="sij-texto-chico">Estado de la solicitud</div>
                        <div><n:estado valor="${solicitud.estado.etiqueta}"/></div>
                    </div>
                </c:if>
                <div class="col-md-6">
                    <div class="sij-texto-chico">Fecha y hora de registro</div>
                    <div><c:out value="${pago.fechaFormateada}"/></div>
                </div>
                <div class="col-md-6">
                    <div class="sij-texto-chico">Moneda y método</div>
                    <div><c:out value="${pago.moneda}"/> · <c:out value="${pago.metodoPago}"/></div>
                </div>
                <div class="col-md-6">
                    <div class="sij-texto-chico">Referencia de sesión Stripe</div>
                    <div class="cop-referencia"><c:out value="${pago.stripeSessionId}"/></div>
                </div>
                <c:if test="${not empty pago.stripePaymentIntentId}">
                    <div class="col-md-6">
                        <div class="sij-texto-chico">Referencia de pago Stripe</div>
                        <div class="cop-referencia"><c:out value="${pago.stripePaymentIntentId}"/></div>
                    </div>
                </c:if>
            </div>

            <div class="sij-aviso p-3 mt-4 mb-0">
                La entrega de las copias certificadas se realiza <strong>únicamente de forma física en ventanilla</strong>
                presentando este comprobante e identificación oficial vigente.
            </div>
        </n:tarjeta>
    </jsp:body>
</t:layout-publico>
