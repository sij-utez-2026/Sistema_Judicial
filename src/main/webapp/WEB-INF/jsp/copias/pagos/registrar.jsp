<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%@ taglib prefix="cop" tagdir="/WEB-INF/tags/copias" %>
<%-- RF 4.1 Pago simulado estilo Stripe Checkout (sin cargos reales ni llaves externas) --%>
<t:layout titulo="Pago ${solicitud.folio}" modulo="copias" seccion="solicitudes">
    <jsp:attribute name="estilos"><cop:estilos/></jsp:attribute>
    <jsp:attribute name="scripts"><script src="<c:url value='/recursos/copias/js/copias.js?v=2'/>"></script></jsp:attribute>
    <jsp:body>
        <n:migas>
            <a href="<c:url value='/copias/solicitudes'/>">Registro de solicitudes</a> /
            <a href="<c:url value='/copias/solicitudes/detalle?id=${solicitud.id}'/>"><c:out value="${solicitud.folio}"/></a> /
            Pago
        </n:migas>

        <n:encabezado titulo="Registrar pago"
                      descripcion="Pasarela simulada para la solicitud ${solicitud.folio} (MXN).">
            <a class="btn btn-sij-secundario" href="<c:url value='/copias/solicitudes/detalle?id=${solicitud.id}'/>">Volver al detalle</a>
        </n:encabezado>

        <div class="cop-checkout mt-3">
            <div class="row g-0">
                <%-- Columna izquierda: resumen estilo Stripe Checkout --%>
                <div class="col-lg-5 cop-checkout-resumen p-4 p-md-5">
                    <div class="cop-checkout-marca mb-3">
                        <span class="cop-checkout-punto"></span>
                        <span>SGCC · Cobro simulado</span>
                        <span class="cop-badge-prueba">MODO PRUEBA</span>
                    </div>

                    <div class="sij-texto-chico">Total a cubrir (MXN)</div>
                    <div class="cop-checkout-total mb-4">
                        <c:out value="${solicitud.costoTexto}"/> <span>MXN</span>
                    </div>

                    <div class="cop-checkout-concepto p-3 mb-4">
                        <div class="d-flex justify-content-between align-items-start gap-2">
                            <div>
                                <div class="fw-semibold">Copias certificadas</div>
                                <div class="sij-texto-chico">
                                    Folio <c:out value="${solicitud.folio}"/> · Expediente <c:out value="${solicitud.numeroExpediente}"/>
                                </div>
                            </div>
                            <strong class="text-nowrap"><c:out value="${solicitud.costoTexto}"/></strong>
                        </div>
                    </div>

                    <div class="cop-dato"><span>Juzgado</span><strong><c:out value="${solicitud.juzgado}"/></strong></div>
                    <div class="cop-dato"><span>Materia</span><strong><c:out value="${solicitud.materia}"/></strong></div>
                    <div class="cop-dato"><span>Solicitante</span><strong><c:out value="${solicitud.solicitanteNombre}"/></strong></div>
                    <div class="cop-dato"><span>Correo</span><strong><c:out value="${solicitud.solicitanteCorreo}"/></strong></div>

                    <div class="cop-aviso mt-4 mb-0">
                        <strong>Pago simulado — no se realiza ningún cargo.</strong>
                    </div>
                </div>

                <%-- Columna derecha: formulario visual de tarjeta estilo Stripe --%>
                <div class="col-lg-7 cop-checkout-panel p-4 p-md-5">
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                        <h2 class="sij-tarjeta-titulo mb-0">Pagar con tarjeta</h2>
                        <div class="d-flex align-items-center gap-1">
                            <span class="cop-red-tarjeta">VISA</span>
                            <span class="cop-red-tarjeta">MC</span>
                            <span class="cop-red-tarjeta">AMEX</span>
                        </div>
                    </div>

                    <form method="post" action="<c:url value='/copias/pagos/registrar'/>" id="formPagoSimulado">
                        <n:csrf/>
                        <input type="hidden" name="id" value="<c:out value='${solicitud.id}'/>">

                        <div class="mb-3">
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <label for="correoRecibo" class="form-label mb-0">Correo para el comprobante</label>
                            </div>
                            <input id="correoRecibo" type="email" class="form-control cop-input-checkout"
                                   value="<c:out value='${solicitud.solicitanteCorreo}'/>" readonly>
                        </div>

                        <div class="cop-grupo-tarjeta mb-3">
                            <div class="d-flex justify-content-between align-items-center px-3 pt-2 pb-1">
                                <span class="sij-texto-chico fw-semibold">Información de la tarjeta (simulada)</span>
                                <button type="button" class="btn btn-link btn-sm p-0 text-decoration-none" data-tarjeta-prueba>
                                    Usar tarjeta 4242
                                </button>
                            </div>
                            <div class="cop-campo-tarjeta">
                                <label for="numeroTarjeta" class="visually-hidden">Número de tarjeta</label>
                                <input id="numeroTarjeta" type="text" class="form-control border-0 shadow-none"
                                       inputmode="numeric" maxlength="19" autocomplete="off"
                                       value="4242 4242 4242 4242" placeholder="1234 1234 1234 1234" required>
                            </div>
                            <div class="row g-0 border-top">
                                <div class="col-6 border-end">
                                    <label for="vencimientoTarjeta" class="visually-hidden">MM / AA</label>
                                    <input id="vencimientoTarjeta" type="text" class="form-control border-0 shadow-none"
                                           inputmode="numeric" maxlength="5" autocomplete="off"
                                           value="12/28" placeholder="MM/AA" required>
                                </div>
                                <div class="col-6">
                                    <label for="cvcTarjeta" class="visually-hidden">CVC</label>
                                    <input id="cvcTarjeta" type="text" class="form-control border-0 shadow-none"
                                           inputmode="numeric" maxlength="4" autocomplete="off"
                                           value="424" placeholder="CVC" required>
                                </div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="titularTarjeta" class="form-label">Nombre en la tarjeta</label>
                            <input id="titularTarjeta" type="text" class="form-control cop-input-checkout"
                                   value="<c:out value='${solicitud.solicitanteNombre}'/>" required>
                        </div>

                        <button type="submit" class="btn btn-lg w-100 cop-btn-stripe">
                            Pagar <c:out value="${solicitud.costoTexto}"/> MXN
                        </button>

                        <div class="d-flex justify-content-between align-items-center mt-3">
                            <span class="sij-texto-chico">Simulación local · sin conexión ni cobros reales</span>
                            <a class="sij-texto-chico" href="<c:url value='/copias/solicitudes/detalle?id=${solicitud.id}'/>">Cancelar</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>
