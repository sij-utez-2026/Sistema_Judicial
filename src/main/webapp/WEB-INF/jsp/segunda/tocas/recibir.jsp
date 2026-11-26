<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Recibir toca" modulo="segunda" seccion="tocas">
  <n:migas>
    <a href="<c:url value='/segunda/tocas'/>">Tocas</a> / Recibir toca
  </n:migas>
  <n:encabezado titulo="Recibir toca"
                descripcion="Seleccione una segunda instancia enviada por Oficialía. La sala de origen se toma del registro oficial y no puede editarse."/>

  <c:if test="${not empty error}">
    <div class="sij-aviso sij-aviso-advertencia p-3 mb-3" role="alert"><c:out value="${error}"/></div>
  </c:if>
  <c:if test="${not empty errorCarga}">
    <div class="sij-aviso sij-aviso-advertencia p-3 mb-3" role="alert"><c:out value="${errorCarga}"/></div>
  </c:if>

  <c:choose>
    <c:when test="${empty pendientes}">
      <n:tarjeta titulo="Sin expedientes pendientes">
        <p class="mb-0">No hay expedientes enviados por Oficialía pendientes de recepción.</p>
      </n:tarjeta>
      <div class="d-flex justify-content-end">
        <a class="btn btn-sij-secundario" href="<c:url value='/segunda/tocas'/>">Volver a tocas</a>
      </div>
    </c:when>
    <c:otherwise>
      <form id="formRecibir" method="post"
            action="<c:url value='/segunda/tocas/recibir'/>" class="mt-3">
        <n:csrf/>
        <n:tarjeta titulo="Datos de la recepción">
          <div class="row g-3">
            <div class="col-12">
              <label for="numeroExpediente" class="form-label">Expediente enviado por Oficialía *</label>
              <select id="numeroExpediente" name="numeroExpediente" class="form-select" required>
                <option value="">Seleccione un expediente pendiente</option>
                <c:forEach var="p" items="${pendientes}">
                  <option value="<c:out value='${p.numeroExpediente}'/>"
                          data-materia="<c:out value='${p.materiaNombre}'/>"
                          data-distrito="<c:out value='${p.distritoNombre}'/>"
                          data-juzgado="<c:out value='${p.juzgadoNombre}'/>"
                          data-sala="<c:out value='${p.salaNombre}'/>"
                          <c:if test="${p.numeroExpediente eq expedienteSeleccionado}">selected</c:if>>
                    <c:out value="${p.numeroExpediente}"/> · <c:out value="${p.materiaNombre}"/> · sala <c:out value="${p.salaNombre}"/>
                  </option>
                </c:forEach>
              </select>
              <div class="form-text">Los datos de materia, distrito y juzgado se leen del expediente; la sala inicial es la asociada a segunda_instancia_Ofc.id_sala.</div>
            </div>
            <div class="col-md-6">
              <label for="salaInicial" class="form-label">Sala inicial (solo lectura)</label>
              <input id="salaInicial" class="form-control" value="" readonly aria-readonly="true">
            </div>
            <div class="col-md-6">
              <label for="datosOrigen" class="form-label">Materia · distrito · juzgado</label>
              <input id="datosOrigen" class="form-control" value="" readonly aria-readonly="true">
            </div>
          </div>
        </n:tarjeta>
        <div class="d-flex justify-content-end gap-2">
          <a class="btn btn-sij-secundario" href="<c:url value='/segunda/tocas'/>">Cancelar</a>
          <button type="submit" class="btn btn-primary">Recibir y asignar toca</button>
        </div>
      </form>
      <script>
        (() => {
          const expediente = document.getElementById('numeroExpediente');
          const sala = document.getElementById('salaInicial');
          const origen = document.getElementById('datosOrigen');
          function actualizar() {
            const option = expediente.options[expediente.selectedIndex];
            sala.value = option && option.dataset.sala ? option.dataset.sala : '';
            origen.value = option && option.dataset.materia
              ? [option.dataset.materia, option.dataset.distrito, option.dataset.juzgado].filter(Boolean).join(' · ') : '';
          }
          expediente.addEventListener('change', actualizar);
          actualizar();
        })();
      </script>
    </c:otherwise>
  </c:choose>
</t:layout>
