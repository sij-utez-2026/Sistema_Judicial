<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Resolución ${resolucion.tocaFolio}" modulo="segunda" seccion="resoluciones">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-resoluciones-detalle">
  <n:migas>
    <a href="<c:url value='/segunda/resoluciones'/>">Resoluciones</a> /
    <c:out value="${resolucion.tocaFolio}"/>
  </n:migas>
  <n:encabezado titulo="Resolución ${resolucion.tocaFolio}"
                descripcion="Expediente ${resolucion.tocaExpediente}">
    <n:estado valor="${resolucion.estado.etiqueta()}"/>
  </n:encabezado>

  <c:if test="${not empty error}">
    <div class="sij-aviso sij-aviso-advertencia p-3 mb-3"><c:out value="${error}"/></div>
  </c:if>

  <div class="row g-3 mt-1">
    <div class="col-lg-8">
      <n:tarjeta titulo="Datos de la resolución">
        <div class="row g-3 sij-texto-chico sin-datos">
          <div class="col-md-6">
            <div class="text-muted">Sentido</div>
            <div><n:estado valor="${resolucion.sentido.etiqueta()}"/></div>
          </div>
          <div class="col-md-6">
            <div class="text-muted">Fecha</div>
            <div class="fw-semibold"><c:out value="${resolucion.fechaTexto}"/></div>
          </div>
          <div class="col-12">
            <div class="text-muted">Resolutivo</div>
            <div class="mt-1 resolucion-cuerpo">${resolucion.comentarios}</div>
          </div>
        </div>
      </n:tarjeta>
    </div>
    <div class="col-lg-4">
      <n:tarjeta titulo="Documento">
        <c:choose>
          <c:when test="${pdfDisponible}">
            <a class="btn btn-primary w-100" href="<c:url value='/segunda/resoluciones/pdf?id=${resolucion.id}'/>" target="_blank" rel="noopener">Abrir PDF guardado</a>
          </c:when>
          <c:otherwise>
            <div class="sij-aviso sij-aviso-advertencia p-3 mb-0">No hay un PDF de resolución asociado en <code>documento_Seg</code>.</div>
          </c:otherwise>
        </c:choose>
      </n:tarjeta>

      <n:tarjeta titulo="Acciones no disponibles">
        <p class="sij-texto-chico mb-0">La base vigente no define versionado ni anulación de resoluciones. Esta pantalla no habilita esas acciones ni modifica la toca después de emitida.</p>
      </n:tarjeta>
    </div>
  </div>

        </div>
    </jsp:body>
</t:layout>
