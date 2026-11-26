<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Ponencias" modulo="segunda" seccion="ponencias">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-ponencias-lista">
  <n:migas>Ponencias</n:migas>
  <n:encabezado titulo="Ponencias"
                descripcion="${segundaEsMagistrado
                      ? 'Ponencias de sus tocas.'
                      : 'Ponencias de todas las salas.'}"/>

  <form class="row g-2 my-3 sin-filtros" method="get" action="<c:url value='/segunda/ponencias'/>">
    <input type="hidden" name="size" value="<c:out value='${size}'/>">
    <div class="col-lg-5 col-md-6">
      <div class="input-group">
        <button type="submit" class="btn btn-sij-secundario" aria-label="Buscar">
          <n:icono nombre="buscar" tamano="14"/>
        </button>
        <input type="search" class="form-control" name="q" value="<c:out value='${q}'/>"
               placeholder="Buscar por folio o expediente" aria-label="Buscar">
      </div>
    </div>
    <div class="col-lg-3 col-md-6">
      <select class="form-select" name="estado" aria-label="Estado" data-autoenvio>
        <option value="">Estado: Todos</option>
        <c:forEach var="e" items="${estados}">
          <option value="<c:out value='${e.name()}'/>"${e.name() eq estado ? ' selected' : ''}>
            <c:out value="${e.etiqueta()}"/>
          </option>
        </c:forEach>
      </select>
    </div>
  </form>

  <n:tarjeta sinRelleno="true" clase="sin-listado">
    <c:choose>
      <c:when test="${empty pagina.content}">
        <div class="p-4 text-center sij-descripcion">No se encontraron ponencias.</div>
      </c:when>
      <c:otherwise>
        <div class="table-responsive">
          <table class="table sij-tabla align-middle mb-0">
            <thead>
            <tr><th>Toca</th><th>Expediente</th><th>Fecha y hora</th><th>Estado</th><th></th></tr>
            </thead>
            <tbody>
            <c:forEach var="p" items="${pagina.content}">
              <tr>
                <td class="fw-semibold"><c:out value="${p.folioToca}"/></td>
                <td><c:out value="${p.numeroExpediente}"/></td>
                <td class="sij-texto-chico"><c:out value="${p.horarioTexto}"/></td>
                <td><n:estado valor="${p.estado.etiqueta()}"/></td>
                <td class="text-end">
                  <a class="btn btn-sm btn-sij-secundario"
                     href="<c:url value='/segunda/tocas/detalle?id=${p.tocaId}'/>">Ver toca</a>
                </td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </n:tarjeta>

  <c:if test="${not empty pagina.content}">
    <div class="d-flex flex-wrap align-items-center gap-3 sin-paginacion">
      <form class="d-flex align-items-center gap-2" method="get" action="<c:url value='/segunda/ponencias'/>">
        <input type="hidden" name="q" value="<c:out value='${q}'/>">
        <input type="hidden" name="estado" value="<c:out value='${estado}'/>">
        <label for="size" class="sij-texto-chico text-nowrap">Registros por página</label>
        <select id="size" name="size" class="form-select form-select-sm w-auto" data-autoenvio>
          <c:forEach var="tam" items="${tamanosPagina}">
            <option value="<c:out value='${tam}'/>"${tam eq size ? ' selected' : ''}>
              <c:out value="${tam}"/>
            </option>
          </c:forEach>
        </select>
      </form>
      <div class="flex-grow-1">
        <n:paginacion pagina="${pagina}" url="/segunda/ponencias"/>
      </div>
    </div>
  </c:if>

        </div>
    </jsp:body>
</t:layout>

