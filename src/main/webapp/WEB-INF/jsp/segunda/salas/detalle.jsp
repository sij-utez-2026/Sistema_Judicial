<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Sala ${sala.nombre}" modulo="segunda" seccion="salas">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-salas-detalle">
  <n:migas><a href="<c:url value='/segunda/salas'/>">Salas</a> / <c:out value="${sala.nombre}"/></n:migas>
  <n:encabezado titulo="${sala.nombre}"
                descripcion="${sala.distritoNombre} · ${sala.direccion}">
    <n:estado valor="${sala.activa ? 'Activa' : 'Inactiva'}"/>
    <a class="btn btn-sij-secundario" href="<c:url value='/segunda/salas/editar?id=${sala.id}'/>">Editar</a>
  </n:encabezado>

  <div class="row g-3 mt-1 justify-content-center">
    <div class="col-lg-8">
      <n:tarjeta titulo="Datos de la sala">
        <div class="row g-3 sij-texto-chico sin-datos">
          <div class="col-md-6">
            <div class="text-muted">Magistrado</div>
            <div class="fw-semibold">
              <c:choose>
                <c:when test="${sala.sinMagistrado}">
                  <span class="text-muted">Sin magistrado</span>
                </c:when>
                <c:otherwise><c:out value="${sala.magistradoNombre}"/></c:otherwise>
              </c:choose>
            </div>
          </div>
          <div class="col-md-6">
            <div class="text-muted">Distrito</div>
            <div class="fw-semibold"><c:out value="${sala.distritoNombre}"/></div>
          </div>
          <div class="col-12">
            <div class="text-muted">Dirección</div>
            <div class="fw-semibold"><c:out value="${sala.direccion}"/></div>
          </div>
        </div>
      </n:tarjeta>

      <n:tarjeta titulo="Tocas de esta sala" sinRelleno="true">
        <c:choose>
          <c:when test="${empty tocas}">
            <div class="p-4 text-center sij-descripcion">Sin tocas registradas.</div>
          </c:when>
          <c:otherwise>
            <table class="table sij-tabla align-middle mb-0">
              <thead>
              <tr><th>Folio</th><th>Expediente</th><th>Estado</th><th></th></tr>
              </thead>
              <tbody>
              <c:forEach var="t" items="${tocas}">
                <tr>
                  <td class="fw-semibold"><c:out value="${t.folioToca}"/></td>
                  <td><c:out value="${t.numeroExpediente}"/></td>
                  <td><n:estado valor="${t.estado.etiqueta()}"/></td>
                  <td class="text-end">
                    <a class="btn btn-sm btn-sij-secundario"
                       href="<c:url value='/segunda/tocas/detalle?id=${t.id}'/>">Ver</a>
                  </td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
      </n:tarjeta>
    </div>
  </div>

        </div>
    </jsp:body>
</t:layout>

