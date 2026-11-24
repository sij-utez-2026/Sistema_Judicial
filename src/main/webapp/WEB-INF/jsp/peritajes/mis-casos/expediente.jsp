<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  RF 4.2 Expediente, RF 4.3 dictamen con autoguardado y RF 4.4 entrega. Bocetos "Expediente · Editor de dictamen",
  "Dictamen · Carga y autoguardado", "Dictamen · Confirmación de entrega" y "Dictamen · Entregado y solo lectura".

  El HTML del dictamen NUNCA se escribe sin escapar: va escapado con c:out en un <textarea hidden> y lo carga
  el editor Quill (dictamen.js), que solo acepta sus formatos. Cuando jsoup esté en el pom, DictamenDao lo limpia
  además con Jsoup (peritajes.md 6.6).
--%>
<t:layout titulo="Peritaje ${peritaje.folio}" modulo="peritajes" seccion="mis-casos">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.snow.css">
        <link rel="stylesheet" href="<c:url value='/recursos/peritajes/css/peritajes.css'/>">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script src="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.js"></script>
        <script src="<c:url value='/recursos/peritajes/js/dictamen.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <n:migas><a href="<c:url value='/peritajes/mis-casos'/>">Mis peritajes</a> / Peritaje <c:out value="${peritaje.folio}"/></n:migas>

        <c:choose>
            <c:when test="${soloLectura}">
                <n:encabezado titulo="Peritaje ${peritaje.folio}"
                              descripcion="Dictamen definitivo · ${peritaje.referenciaDemanda} · Materia ${peritaje.materia}">
                    <n:estado valor="${peritaje.estado.texto}"/>
                </n:encabezado>
            </c:when>
            <c:otherwise>
                <n:encabezado titulo="Peritaje ${peritaje.folio}"
                              descripcion="Expediente y elaboración del dictamen · Apertura registrada el ${peritaje.fechaAperturaTexto}"/>
            </c:otherwise>
        </c:choose>

        <%-- Datos del peritaje --%>
        <n:tarjeta clase="mt-3">
            <dl class="row g-3 align-items-center per-datos mb-0">
                <div class="col-sm-6 col-lg-3"><dt>Demanda / materia</dt><dd class="mb-0"><c:out value="${peritaje.referenciaDemanda}"/> · <c:out value="${peritaje.materia}"/></dd></div>
                <div class="col-sm-6 col-lg-3"><dt>Perito responsable</dt><dd class="mb-0"><c:out value="${peritaje.peritoNombreTexto}"/></dd></div>
                <div class="col-sm-6 col-lg-3"><dt>Fecha de solicitud</dt><dd class="mb-0"><c:out value="${peritaje.fechaSolicitudTexto}"/></dd></div>
                <div class="col-sm-6 col-lg-3 text-lg-end"><n:estado valor="${peritaje.estado.texto}"/></div>
            </dl>
        </n:tarjeta>

        <c:choose>
            <%-- ===================== Dictamen entregado: solo lectura ===================== --%>
            <c:when test="${soloLectura}">
                <div class="row g-3">
                    <div class="col-lg-8">
                        <section class="sij-tarjeta mb-3">
                            <div class="per-solo-lectura">&#128274; Versión definitiva · Solo lectura</div>
                            <textarea id="contenidoDictamen" hidden><c:out value="${dictamen.contenido}"/></textarea>
                            <div id="lectura" class="per-lectura" aria-label="Contenido del dictamen"></div>
                        </section>
                    </div>
                    <div class="col-lg-4">
                        <n:tarjeta titulo="Constancia de entrega">
                            <dl class="per-datos mb-3">
                                <dt>Folio</dt><dd><c:out value="${peritaje.folio}"/></dd>
                                <dt>Perito responsable</dt><dd><c:out value="${peritaje.peritoNombreTexto}"/></dd>
                                <dt>Fecha y hora de entrega</dt><dd><c:out value="${peritaje.fechaEntregaTexto}"/></dd>
                                <dt>Órgano solicitante</dt><dd><c:out value="${empty peritaje.juzgado ? 'Sin dato' : peritaje.juzgado}"/></dd>
                            </dl>
                            <n:estado valor="Entrega inmutable" color="ambar"/>
                        </n:tarjeta>
                        <n:tarjeta titulo="Archivos entregados">
                            <c:if test="${empty dictamen.archivos}">
                                <p class="sij-descripcion mb-0">El dictamen se entregó sin archivos anexos.</p>
                            </c:if>
                            <c:forEach var="a" items="${dictamen.archivos}">
                                <div class="per-archivo">
                                    <div>
                                        <div class="fw-semibold"><c:out value="${a.nombreOriginal}"/></div>
                                        <div class="sij-texto-chico"><c:out value="${a.tipoTexto}"/> · <c:out value="${a.tamanoTexto}"/></div>
                                    </div>
                                    <a class="btn btn-sm btn-sij-secundario"
                                       href="<c:url value='/peritajes/mis-casos/dictamen/archivo'><c:param name='id' value='${peritaje.id}'/><c:param name='archivoId' value='${a.id}'/></c:url>">Descargar archivo</a>
                                </div>
                            </c:forEach>
                        </n:tarjeta>
                    </div>
                </div>
            </c:when>

            <%-- ===================== En Proceso: expediente + editor ===================== --%>
            <c:otherwise>
                <div class="row g-3">
                    <%-- Datos de la demanda y anexos (tablas de Oficialía, solo lectura) --%>
                    <div class="col-xl-5">
                        <n:tarjeta titulo="Expediente y anexos">
                            <c:if test="${not empty expediente}">
                                <dl class="per-datos mb-3">
                                    <dt>Demanda</dt><dd><c:out value="${expediente.folioDemanda}"/> · recibida el <c:out value="${expediente.fechaRecepcionTexto}"/></dd>
                                    <dt>Expediente</dt><dd><c:out value="${empty expediente.numeroExpediente ? 'Sin expediente' : expediente.numeroExpediente}"/></dd>
                                    <dt>Juzgado</dt><dd><c:out value="${empty expediente.juzgado ? 'Sin dato' : expediente.juzgado}"/></dd>
                                    <dt>Demandante</dt><dd><c:out value="${empty expediente.demandante ? 'Sin dato' : expediente.demandante}"/></dd>
                                    <dt>Demandado</dt><dd><c:out value="${empty expediente.demandado ? 'Sin dato' : expediente.demandado}"/></dd>
                                    <dt>Descripción</dt><dd><c:out value="${expediente.descripcion}"/></dd>
                                </dl>
                            </c:if>
                            <h3 class="fs-6 fw-semibold">Documentos de la demanda</h3>
                            <c:choose>
                                <c:when test="${empty expediente or empty expediente.documentos}">
                                    <p class="sij-descripcion">La demanda no tiene documentos registrados.</p>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="doc" items="${expediente.documentos}">
                                        <div class="per-archivo">
                                            <div class="fw-semibold">&#128196; <c:out value="${doc.nombre}"/></div>
                                            <%-- [Confirmar con Oficialía] documento_Ofc solo tiene ruta_archivo: no se sabe si es la ruta o el archivo --%>
                                            <button type="button" class="btn btn-sm btn-sij-secundario" disabled
                                                    title="Pendiente de confirmar con Oficialía">Descargar</button>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </n:tarjeta>
                    </div>

                    <%-- Dictamen técnico (RF 4.3) --%>
                    <div class="col-xl-7">
                        <section class="sij-tarjeta p-4 mb-3" id="dictamen"
                                 data-url-guardar="<c:url value='/peritajes/mis-casos/dictamen/autoguardado'/>"
                                 data-peritaje-id="<c:out value='${peritaje.id}'/>"
                                 data-version="<c:out value='${dictamen.version}'/>">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <h2 class="sij-tarjeta-titulo mb-0">Dictamen técnico</h2>
                                <span class="sij-estado sij-estado-verde" data-indicador-guardado>
                                    <c:choose>
                                        <c:when test="${not dictamen.guardado}">Sin guardar</c:when>
                                        <c:otherwise>Guardado · <c:out value="${dictamen.actualizadoEnHora}"/> h</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <textarea id="contenidoDictamen" hidden><c:out value="${dictamen.contenido}"/></textarea>
                            <div id="editor" class="per-editor"></div>
                            <div class="sij-texto-chico mt-2" data-mensaje-guardado aria-live="polite">
                                Autoguardado cada 30 segundos solo si hay modificaciones.
                            </div>

                            <%-- Archivos de soporte (RF 4.4): opcionales; se pueden quitar mientras sea borrador --%>
                            <h3 class="fs-6 fw-semibold mt-4">Archivos de soporte</h3>
                            <c:forEach var="a" items="${dictamen.archivos}">
                                <div class="per-archivo">
                                    <div>
                                        <div class="fw-semibold"><c:out value="${a.nombreOriginal}"/></div>
                                        <div class="sij-texto-chico"><c:out value="${a.tipoTexto}"/> · <c:out value="${a.tamanoTexto}"/> · Cargado</div>
                                    </div>
                                    <div class="d-flex gap-1">
                                        <a class="btn btn-sm btn-sij-secundario"
                                           href="<c:url value='/peritajes/mis-casos/dictamen/archivo'><c:param name='id' value='${peritaje.id}'/><c:param name='archivoId' value='${a.id}'/></c:url>">Descargar</a>
                                        <n:confirmar accion="/peritajes/mis-casos/dictamen/archivos/eliminar"
                                                     texto="¿Quitar el archivo ${a.nombreOriginal} del dictamen?" boton="Quitar"
                                                     clase="btn btn-sm btn-outline-danger">
                                            <input type="hidden" name="id" value="<c:out value='${peritaje.id}'/>">
                                            <input type="hidden" name="archivoId" value="<c:out value='${a.id}'/>">
                                        </n:confirmar>
                                    </div>
                                </div>
                            </c:forEach>
                            <form method="post" enctype="multipart/form-data" class="per-subida mt-2" data-subir-archivo
                                  action="<c:url value='/peritajes/mis-casos/dictamen/archivos'/>">
                                <n:csrf/>
                                <input type="hidden" name="id" value="<c:out value='${peritaje.id}'/>">
                                <n:archivo nombre="archivo" etiqueta="Seleccione un archivo" tipos=".pdf,.doc,.docx,.png,.jpg,.jpeg" maxMb="25"/>
                                <button type="submit" class="btn btn-sm btn-sij-secundario">Subir archivo</button>
                                <p class="sij-texto-chico mb-0 mt-2">El borrador sigue editable hasta la entrega definitiva.</p>
                            </form>

                            <div class="d-flex flex-wrap justify-content-between gap-2 mt-4 pt-3 border-top">
                                <button type="button" class="btn btn-sij-secundario" data-guardar-borrador>Guardar borrador</button>
                                <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#ventanaEntrega">
                                    Entregar dictamen
                                </button>
                            </div>
                        </section>
                    </div>
                </div>

                <%-- ===== Confirmación de entrega (RF 4.4: pide confirmación y avisa que ya no se podrá modificar) ===== --%>
                <div class="modal fade" id="ventanaEntrega" tabindex="-1" aria-labelledby="tituloEntrega" aria-hidden="true">
                    <div class="modal-dialog modal-dialog-centered">
                        <form class="modal-content" method="post" data-finalizar
                              action="<c:url value='/peritajes/mis-casos/dictamen/finalizar'/>">
                            <n:csrf/>
                            <input type="hidden" name="id" value="<c:out value='${peritaje.id}'/>">
                            <input type="hidden" name="contenido" data-campo-contenido disabled>
                            <input type="hidden" name="version" data-campo-version disabled>
                            <div class="modal-header">
                                <div>
                                    <div class="sij-texto-chico text-uppercase">Entrega definitiva</div>
                                    <h2 class="modal-title fs-5" id="tituloEntrega">¿Entregar el dictamen?</h2>
                                </div>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                            </div>
                            <div class="modal-body">
                                <div class="sij-aviso sij-aviso-advertencia p-3 mb-3">
                                    &#9432; Esta acción es irreversible: después ya no podrá modificar ni reemplazar el dictamen.
                                </div>
                                <dl class="per-datos per-resumen p-3 mb-3">
                                    <dt>Peritaje</dt><dd><c:out value="${peritaje.folio}"/> · <c:out value="${peritaje.referenciaDemanda}"/></dd>
                                    <dt>Perito responsable</dt><dd><c:out value="${peritaje.peritoNombreTexto}"/></dd>
                                    <dt>Documentos de soporte · <c:out value="${dictamen.archivos.size()}"/> archivos</dt>
                                    <dd>
                                        <c:if test="${empty dictamen.archivos}">Sin archivos anexos (son opcionales).</c:if>
                                        <c:forEach var="a" items="${dictamen.archivos}">
                                            <div><c:out value="${a.nombreOriginal}"/> · <c:out value="${a.tamanoTexto}"/></div>
                                        </c:forEach>
                                    </dd>
                                </dl>
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="confirmoEntrega" required>
                                    <label class="form-check-label" for="confirmoEntrega">
                                        Confirmo que revisé el dictamen y entiendo que la entrega es definitiva.
                                    </label>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-sij-secundario" data-bs-dismiss="modal">Volver al borrador</button>
                                <button type="submit" class="btn btn-primary">Confirmar entrega</button>
                            </div>
                        </form>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </jsp:body>
</t:layout>
