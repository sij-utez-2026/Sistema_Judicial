<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Reasignación de tocas" modulo="segunda" seccion="reasignacion">
  <n:migas><a href="<c:url value='/segunda/tocas'/>">Tocas</a> / Reasignación</n:migas>
  <n:encabezado titulo="Reasignación de tocas"
                descripcion="Administración puede cambiar la sala de una toca abierta. El cambio actualiza también al magistrado de la sala destino y registra el motivo."/>

  <n:tarjeta titulo="Ejecutar reasignación">
    <c:choose>
      <c:when test="${empty tocasAbiertas}">
        <p class="mb-0">No hay tocas abiertas disponibles para reasignar.</p>
      </c:when>
      <c:otherwise>
        <div class="alert alert-info" role="status">
          La reasignación se ejecuta al confirmar el formulario; no queda en una cola de aprobación.
          Se registra en <code>reasignacion_seg</code> la toca, la sala de origen, la sala destino y el motivo.
        </div>
        <form method="post" action="<c:url value='/segunda/tocas/reasignar'/>">
          <n:csrf/>
          <div class="row g-3">
            <div class="col-md-6">
              <label for="tocaId" class="form-label">Toca *</label>
              <select id="tocaId" name="tocaId" class="form-select" required>
                <option value="">Seleccione una toca abierta</option>
                <c:forEach var="t" items="${tocasAbiertas}">
                  <option value="<c:out value='${t.id}'/>">
                    <c:out value="${t.folioToca}"/> · <c:out value="${t.numeroExpediente}"/>
                    · Sala actual: <c:out value="${t.salaNombre}"/>
                    · Magistrado: <c:out value="${t.magistradoNombre}"/>
                  </option>
                </c:forEach>
              </select>
            </div>
            <div class="col-md-6">
              <label for="salaId" class="form-label">Sala destino *</label>
              <select id="salaId" name="salaId" class="form-select" required>
                <option value="">Seleccione la sala destino</option>
                <c:forEach var="s" items="${salas}">
                  <option value="<c:out value='${s.id}'/>">
                    <c:out value="${s.nombre}"/> · <c:out value="${s.magistradoNombre}"/>
                  </option>
                </c:forEach>
              </select>
              <div class="form-text">Solo se aceptan salas activas con magistrado activo; no puede ser la sala actual.</div>
            </div>
            <div class="col-12">
              <label for="motivo" class="form-label">Motivo *</label>
              <textarea id="motivo" name="motivo" rows="3" maxlength="300" class="form-control" required></textarea>
              <div class="form-text">Máximo 300 caracteres.</div>
            </div>
          </div>
          <div class="d-flex justify-content-end mt-3">
            <button type="submit" class="btn btn-primary">Confirmar reasignación</button>
          </div>
        </form>
      </c:otherwise>
    </c:choose>
  </n:tarjeta>
</t:layout>
