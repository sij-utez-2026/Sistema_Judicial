<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Agenda de ponencias (solo lectura, vista semanal y mensual) --%>


<t:layout titulo="Agenda" modulo="segunda" seccion="agenda">
  <jsp:attribute name="estilos">
    <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/agenda.css'/>">
          <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
  <jsp:body>
        <div class="sin-pantalla sin-vista-agenda-lista">
  <n:migas>Agenda</n:migas>

  <n:encabezado
          titulo="${segundaEsMagistrado ? 'Mi agenda' : 'Agenda de ponencias'}"
          descripcion="${segundaEsMagistrado
                ? 'Ponencias programadas de sus tocas en segunda instancia.'
                : 'Ponencias programadas de todas las salas en segunda instancia.'}"/>

  <%-- Barra de controles: navegación, vista y filtros (admin) --%>
  <form class="row g-2 my-3 sin-filtros sin-agenda-controles" method="get" action="<c:url value='/segunda/agenda'/>">
    <input type="hidden" name="vista" value="<c:out value='${vista}'/>">
    <input type="hidden" name="fecha" value="<c:out value='${fecha}'/>">

    <div class="col-lg-4 col-md-6 d-flex align-items-center gap-2 sin-agenda-navegacion">
      <a class="btn btn-sij-secundario"
         href="<c:url value='/segunda/agenda?vista=${vista}&fecha=${fechaAnterior}'/>"
         aria-label="Rango anterior">&larr;</a>
      <span class="sij-texto-chico text-nowrap fw-semibold sin-agenda-rango">
                <c:choose>
                  <c:when test="${vista eq 'mensual'}">
                    Mes <c:out value="${mesActual}"/> / <c:out value="${anioActual}"/>
                  </c:when>
                  <c:otherwise>
                    <c:out value="${desde}"/> &ndash; <c:out value="${hasta}"/>
                  </c:otherwise>
                </c:choose>
            </span>
      <a class="btn btn-sij-secundario"
         href="<c:url value='/segunda/agenda?vista=${vista}&fecha=${fechaSiguiente}'/>"
         aria-label="Rango siguiente">&rarr;</a>
    </div>

    <div class="col-lg-3 col-md-6">
      <div class="btn-group w-100" role="group" aria-label="Vista de la agenda">
        <a class="btn btn-sm ${vista eq 'semanal' ? 'btn-primary' : 'btn-sij-secundario'}"
           href="<c:url value='/segunda/agenda?vista=semanal&fecha=${fecha}'/>">Semanal</a>
        <a class="btn btn-sm ${vista eq 'mensual' ? 'btn-primary' : 'btn-sij-secundario'}"
           href="<c:url value='/segunda/agenda?vista=mensual&fecha=${fecha}'/>">Mensual</a>
      </div>
    </div>

    <c:if test="${segundaEsAdmin}">
      <div class="col-lg-3 col-md-6">
        <select class="form-select" name="salaId" aria-label="Sala" data-autoenvio>
          <option value="">Sala: Todas</option>
          <c:forEach var="s" items="${salas}">
            <option value="<c:out value='${s.id}'/>"${s.id eq salaId ? ' selected' : ''}>
              <c:out value="${s.nombre}"/>
            </option>
          </c:forEach>
        </select>
      </div>
      <div class="col-lg-2 col-md-6">
        <select class="form-select" name="magistradoId" aria-label="Magistrado" data-autoenvio>
          <option value="">Magistrado: Todos</option>
          <c:forEach var="m" items="${magistrados}">
            <option value="<c:out value='${m.usuarioId}'/>"${m.usuarioId eq magistradoId ? ' selected' : ''}>
              <c:out value="${m.nombreCompleto}"/>
            </option>
          </c:forEach>
        </select>
      </div>
    </c:if>
  </form>

  <div class="d-flex flex-wrap gap-2 mb-3">
    <span class="sij-contador">&bull; <c:out value="${totalEventos}"/> Ponencias en el rango</span>
  </div>

  <%-- Calendario --%>
  <n:tarjeta sinRelleno="true" clase="sin-agenda-card">
    <div class="p-3">
      <div class="sin-calendario-cabecera">
        <div>Lun</div><div>Mar</div><div>Mié</div>
        <div>Jue</div><div>Vie</div><div>Sáb</div><div>Dom</div>
      </div>

      <div class="sin-calendario sin-calendario-${vista}">
        <c:forEach var="dia" items="${porDia}">
          <div class="sin-calendario-dia">
            <div class="sin-calendario-numero">
              <c:out value="${dia.key.dayOfMonth}"/>
            </div>
            <div class="sin-calendario-eventos">
              <c:forEach var="e" items="${dia.value}">
                <a class="sin-calendario-evento ${e.estadoClase}"
                   href="<c:url value='/segunda/tocas/detalle?id=${e.tocaId}'/>"
                   title="<c:out value='${e.descripcion}'/> · <c:out value='${e.salaNombre}'/>">
                  <span class="sin-calendario-hora"><c:out value="${e.horaTexto}"/></span>
                  <span class="sin-calendario-folio"><c:out value="${e.folioToca}"/></span>
                </a>
              </c:forEach>
            </div>
          </div>
        </c:forEach>
      </div>
    </div>
  </n:tarjeta>

  <div class="sij-texto-chico mt-3">
    &#9432; La agenda es de solo consulta. Para registrar o cancelar una ponencia,
    abra el detalle de la toca correspondiente.
  </div>
  
        </div>
    </jsp:body>
</t:layout>

