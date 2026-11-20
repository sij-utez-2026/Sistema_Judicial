<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%@ taglib prefix="cop" tagdir="/WEB-INF/tags/copias" %>
<%-- RF 4.1 Pantalla de pago simulado (réplica visual de Stripe Checkout, 100% local sin API ni llaves) --%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Pago · <c:out value="${solicitud.folio}"/> · SGCC</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="<c:url value='/recursos/nucleo/css/estilos.css'/>">
    <cop:estilos/>
</head>
<body class="cop-stripe-body">

<n:mensajes/>

<main class="cop-stripe-layout">
    <%-- Columna izquierda: resumen de la solicitud --%>
    <section class="cop-stripe-col-izq">
        <div class="cop-stripe-resumen-inner">
            <div class="cop-stripe-top">
                <a class="cop-stripe-volver"
                   href="<c:url value='/copias/solicitudes/detalle?id=${solicitud.id}'/>"
                   title="Volver al detalle de la solicitud" aria-label="Volver al detalle">
                    <svg width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                        <path d="M10 13L5 8l5-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </a>
                <span class="cop-stripe-tienda-icono" aria-hidden="true">
                    <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
                        <path d="M2.5 6.5V13.5h11V6.5M1.5 6.5l1.2-3.5h10.6l1.2 3.5a1.8 1.8 0 0 1-3.3 0 1.8 1.8 0 0 1-3.2 0 1.8 1.8 0 0 1-3.2 0 1.8 1.8 0 0 1-3.3 0Z"
                              stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
                    </svg>
                </span>
                <span class="cop-stripe-negocio">Entorno de prueba de SGCC</span>
                <span class="cop-stripe-badge-prueba">
                    <svg width="11" height="11" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                        <path d="M8 1.5l5.5 3.2v6.6L8 14.5l-5.5-3.2V4.7L8 1.5z" stroke="currentColor" stroke-width="1.4"/>
                        <path d="M8 14.5V8m0 0l5.5-3.3M8 8L2.5 4.7" stroke="currentColor" stroke-width="1.3"/>
                    </svg>
                    Entorno de prueba
                </span>
            </div>

            <div class="cop-stripe-producto">
                <div class="cop-stripe-concepto">
                    Copias certificadas · Folio <c:out value="${solicitud.folio}"/>
                </div>
                <div class="cop-stripe-monto">
                    MXN <c:out value="${solicitud.costo}"/>
                </div>
                <div class="cop-stripe-detalle-exp">
                    Expediente <c:out value="${solicitud.numeroExpediente}"/> · <c:out value="${solicitud.juzgado}"/>
                </div>
                <div class="cop-stripe-nota-simulada">
                    Pago simulado — no se realiza ningún cargo · Comprobante para <c:out value="${solicitud.solicitanteNombre}"/>
                </div>
            </div>
        </div>
    </section>

    <%-- Columna derecha: formulario visual estilo Stripe Checkout --%>
    <section class="cop-stripe-col-der">
        <div class="cop-stripe-form-inner">
            <form method="post" action="<c:url value='/copias/pagos/registrar'/>" id="formPagoSimulado">
                <n:csrf/>
                <input type="hidden" name="id" value="<c:out value='${solicitud.id}'/>">

                <%-- Botones rápidos simulados --%>
                <div class="cop-stripe-express">
                    <button type="submit" class="cop-stripe-btn-apple" title="Simular pago rápido con Apple Pay">
                        <svg width="14" height="17" viewBox="0 0 14 17" fill="currentColor" aria-hidden="true">
                            <path d="M11.6 9.1c0-2 1.6-3 1.7-3.1-1-1.4-2.4-1.6-2.9-1.6-1.2-.1-2.4.7-3 .7-.6 0-1.6-.7-2.6-.7-1.3 0-2.6.8-3.3 2-1.4 2.4-.4 6 1 8 .7 1 1.5 2.1 2.5 2 .9 0 1.4-.6 2.5-.6 1.1 0 1.5.6 2.5.6 1.1 0 1.8-1 2.4-2 .8-1.1 1.1-2.2 1.1-2.2-.1 0-1.9-.7-1.9-3.1zM9.6 3.1c.6-.7 1-1.6.9-2.6-.9 0-1.9.6-2.5 1.3-.5.6-1 1.6-.9 2.5 1 .1 2-.5 2.5-1.2z"/>
                        </svg>
                        <span>Pay</span>
                    </button>
                    <button type="submit" class="cop-stripe-btn-link" title="Simular pago rápido con Link">
                        <span class="cop-stripe-link-icono" aria-hidden="true">&gt;</span>
                        <span>link</span>
                    </button>
                </div>

                <div class="cop-stripe-divisor">
                    <span>o</span>
                </div>

                <%-- Información de contacto --%>
                <div class="cop-stripe-seccion">Información de contacto</div>
                <div class="mb-2">
                    <label for="correoRecibo" class="cop-stripe-label">Correo electrónico</label>
                    <input id="correoRecibo" type="email" class="cop-stripe-input"
                           placeholder="tu@ejemplo.com">
                </div>

                <%-- Método de pago --%>
                <div class="cop-stripe-seccion">Método de pago</div>
                <div class="cop-stripe-tarjeta-box mb-3">
                    <div class="cop-stripe-tarjeta-titulo">
                        <svg width="16" height="12" viewBox="0 0 18 14" fill="none" aria-hidden="true">
                            <rect x="0.75" y="0.75" width="16.5" height="12.5" rx="2" fill="#1a1f36"/>
                            <rect x="0.75" y="3.5" width="16.5" height="2.2" fill="#fff"/>
                            <rect x="3" y="8.5" width="4" height="1.5" rx="0.5" fill="#fff"/>
                        </svg>
                        <span>Tarjeta</span>
                    </div>

                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <label for="numeroTarjeta" class="cop-stripe-label mb-0">Datos de la tarjeta</label>
                        <button type="button" class="cop-stripe-llenar-4242" data-tarjeta-prueba>Usar 4242</button>
                    </div>

                    <div class="cop-stripe-card-group mb-2">
                        <div class="cop-stripe-card-row-top">
                            <input id="numeroTarjeta" type="text" class="cop-stripe-card-input"
                                   inputmode="numeric" maxlength="19" autocomplete="off"
                                   placeholder="Número de tarjeta">
                            <div class="cop-stripe-brands" aria-hidden="true">
                                <span class="cop-brand-visa">VISA</span>
                                <span class="cop-brand-mc">
                                    <span class="mc-r"></span><span class="mc-y"></span>
                                </span>
                                <span class="cop-brand-amex">AMEX</span>
                            </div>
                        </div>
                        <div class="cop-stripe-card-row-bottom">
                            <div class="cop-stripe-card-half">
                                <label for="vencimientoTarjeta" class="visually-hidden">MM/AA</label>
                                <input id="vencimientoTarjeta" type="text" class="cop-stripe-card-input"
                                       inputmode="numeric" maxlength="5" autocomplete="off"
                                       placeholder="MM/AA">
                            </div>
                            <div class="cop-stripe-card-half cop-stripe-card-cvc">
                                <label for="cvcTarjeta" class="visually-hidden">Código de seguridad</label>
                                <input id="cvcTarjeta" type="text" class="cop-stripe-card-input"
                                       inputmode="numeric" maxlength="4" autocomplete="off"
                                       placeholder="Código de seguridad">
                                <svg class="cop-stripe-cvc-icon" width="18" height="12" viewBox="0 0 22 15" fill="none" aria-hidden="true">
                                    <rect x="0.75" y="0.75" width="20.5" height="13.5" rx="2" stroke="#8792a2" stroke-width="1.3"/>
                                    <rect x="1" y="3.5" width="20" height="2.5" fill="#8792a2"/>
                                    <rect x="13" y="8.5" width="5.5" height="2" rx="0.5" fill="#8792a2"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div class="mb-2">
                        <label for="titularTarjeta" class="cop-stripe-label">Nombre del titular de la tarjeta</label>
                        <input id="titularTarjeta" type="text" class="cop-stripe-input"
                               placeholder="Nombre y apellido">
                    </div>

                    <div class="mb-2">
                        <label for="paisFacturacion" class="cop-stripe-label">Datos de facturación</label>
                        <select id="paisFacturacion" class="cop-stripe-input cop-stripe-select">
                            <option value="MX" selected>México</option>
                            <option value="US">Estados Unidos</option>
                            <option value="ES">España</option>
                            <option value="CO">Colombia</option>
                        </select>
                    </div>

                    <label class="cop-stripe-guardar mb-2">
                        <input type="checkbox" checked>
                        <span>Guardar mis datos para un proceso de compra más rápido</span>
                    </label>

                    <div class="mb-2">
                        <label for="celularContacto" class="cop-stripe-label">Número de celular</label>
                        <div class="cop-stripe-phone-wrap">
                            <span class="cop-stripe-flag" aria-hidden="true">
                                <span class="f-g"></span><span class="f-w"></span><span class="f-r"></span>
                            </span>
                            <span class="cop-stripe-caret" aria-hidden="true">⌄</span>
                            <input id="celularContacto" type="tel" class="cop-stripe-phone-input"
                                   placeholder="222 123 4567" autocomplete="off">
                        </div>
                    </div>

                    <p class="cop-stripe-legal mb-0">
                        <span class="cop-stripe-link-mini">&gt;</span> <strong>link</strong> · Si proporcionas tu número de teléfono y correo electrónico, aceptas crear una cuenta sujeta a las <span class="text-decoration-underline">Condiciones</span> y la <span class="text-decoration-underline">Política de privacidad</span> de Link.
                    </p>
                </div>

                <button type="submit" class="cop-stripe-btn-pago">Pago</button>
            </form>
        </div>
    </section>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<c:url value='/recursos/nucleo/js/comun.js'/>"></script>
<script src="<c:url value='/recursos/copias/js/copias.js?v=3'/>"></script>
</body>
</html>
