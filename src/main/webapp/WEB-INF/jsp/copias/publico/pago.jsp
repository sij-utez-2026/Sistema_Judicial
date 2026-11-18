<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Portal público de pago de copias certificadas mediante Stripe Checkout en MXN (RF 4.1) --%>
<t:layout-publico titulo="Pago de copias certificadas" subtitulo="Copias Certificadas · SGCC">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/copias/css/copias.css'/>">
    </jsp:attribute>
    <jsp:body>
        <n:migas>
            <a href="<c:url value='/copias'/>">Copias Certificadas</a> / Pago en línea
        </n:migas>

        <n:encabezado titulo="Pago de copias certificadas"
                      descripcion="Consulte su folio de solicitud para cubrir el importe en MXN mediante Stripe Checkout.">
            <a class="btn btn-sij-secundario" href="<c:url value='/copias'/>">Volver al módulo</a>
        </n:encabezado>

        <c:if test="${not stripeConfigurado}">
            <div class="sij-aviso sij-aviso-advertencia p-3 mt-3" role="alert">
                <strong>Stripe sin configurar:</strong> defina la variable de entorno
                <code>STRIPE_SECRET_KEY</code> para habilitar la redirección a <code>checkout.stripe.com</code>.
            </div>
        </c:if>

        <c:if test="${avisoCancelado}">
            <div class="sij-aviso sij-aviso-advertencia p-3 mt-3" role="status">
                El pago fue cancelado en Stripe. No se realizó ningún cargo; puede intentarlo nuevamente cuando lo desee.
            </div>
        </c:if>

        <n:tarjeta titulo="Consultar solicitud por folio" clase="mt-3">
            <form method="get" action="<c:url value='/copias/publico/pago'/>" class="row g-2 align-items-end">
                <div class="col-md-8">
                    <label for="folio" class="form-label">Folio de solicitud *</label>
                    <input id="folio" name="folio" type="text" class="form-control"
                           placeholder="Ejemplo: CC-2026-A7K9M2"
                           value="<c:out value='${folio}'/>" required>
                </div>
                <div class="col-md-4">
                    <button type="submit" class="btn btn-primary w-100">Consultar folio</button>
                </div>
            </form>
        </n:tarjeta>

        <c:if test="${not empty solicitud}">
            <n:tarjeta titulo="Resumen de la solicitud">
                <div class="row g-3">
                    <div class="col-md-4">
                        <div class="sij-texto-chico">Folio de solicitud</div>
                        <div class="fw-semibold"><c:out value="${solicitud.folioSolicitud}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="sij-texto-chico">Número de expediente</div>
                        <div class="fw-semibold"><c:out value="${solicitud.numeroExpediente}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="sij-texto-chico">Estado actual</div>
                        <div><n:estado valor="${solicitud.estado.etiqueta}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="sij-texto-chico">Tipo de demanda</div>
                        <div><c:out value="${solicitud.tipo.etiqueta}"/></div>
                    </div>
                    <div class="col-md-4">
                        <div class="sij-texto-chico">Juzgado / Materia</div>
                        <div>
                            <c:out value="${solicitud.juzgadoNombre}"/>
                            <c:if test="${not empty solicitud.materiaNombre}">
                                · <c:out value="${solicitud.materiaNombre}"/>
                            </c:if>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="sij-texto-chico">Fecha de solicitud</div>
                        <div><c:out value="${solicitud.fechaSolicitudFormateada}"/></div>
                    </div>
                </div>

                <hr class="my-4">

                <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
                    <div>
                        <div class="sij-texto-chico">Importe total a pagar (Pesos mexicanos)</div>
                        <div class="cop-monto-destacado"><c:out value="${solicitud.costoFormateado}"/></div>
                    </div>

                    <c:choose>
                        <c:when test="${not empty pagoExistente and solicitud.estado.name() ne 'ENTREGADO'}">
                            <c:url var="urlComprobante" value="/copias/publico/pago/comprobante">
                                <c:param name="folioPago" value="${pagoExistente.folioPago}"/>
                            </c:url>
                            <a class="btn btn-primary" href="<c:out value='${urlComprobante}'/>">
                                Ver comprobante de pago
                            </a>
                        </c:when>
                        <c:when test="${solicitud.pagable}">
                            <form method="post" action="<c:url value='/copias/publico/pago/stripe'/>" class="m-0">
                                <n:csrf/>
                                <input type="hidden" name="folio" value="<c:out value='${solicitud.folioSolicitud}'/>">
                                <button type="submit" class="btn btn-primary btn-lg">Pagar</button>
                            </form>
                        </c:when>
                    </c:choose>
                </div>

                <c:if test="${not empty motivoNoPagable and (empty pagoExistente or solicitud.estado.name() eq 'ENTREGADO')}">
                    <div class="sij-aviso sij-aviso-advertencia p-3 mt-3">
                        <c:out value="${motivoNoPagable}"/>
                    </div>
                </c:if>

                <c:if test="${solicitud.pagable and empty pagoExistente}">
                    <p class="sij-texto-chico mt-3 mb-0">
                        Al presionar <strong>Pagar</strong> será redirigido a la pantalla oficial segura de Stripe
                        (<code>checkout.stripe.com</code>) para completar el pago en MXN y regresar automáticamente a su comprobante.
                    </p>
                </c:if>
            </n:tarjeta>
        </c:if>

        <c:if test="${not empty solicitudesDemo}">
            <n:tarjeta titulo="Folios de prueba disponibles" sinRelleno="true">
                <div class="table-responsive">
                    <table class="table sij-tabla align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Folio</th>
                            <th>Expediente</th>
                            <th>Tipo</th>
                            <th>Estado</th>
                            <th>Importe</th>
                            <th class="text-end">Acción</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="s" items="${solicitudesDemo}">
                            <tr>
                                <td class="fw-semibold"><c:out value="${s.folioSolicitud}"/></td>
                                <td><c:out value="${s.numeroExpediente}"/></td>
                                <td><c:out value="${s.tipo.etiqueta}"/></td>
                                <td><n:estado valor="${s.estado.etiqueta}"/></td>
                                <td><c:out value="${s.costoFormateado}"/></td>
                                <td class="text-end">
                                    <c:url var="urlSeleccionar" value="/copias/publico/pago">
                                        <c:param name="folio" value="${s.folioSolicitud}"/>
                                    </c:url>
                                    <a class="btn btn-sm btn-sij-secundario" href="<c:out value='${urlSeleccionar}'/>">
                                        Seleccionar
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </n:tarjeta>
        </c:if>
    </jsp:body>
</t:layout-publico>
