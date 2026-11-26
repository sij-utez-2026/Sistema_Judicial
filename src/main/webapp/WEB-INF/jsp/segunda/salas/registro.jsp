<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- RF 3.1 Registro de sala --%>
<t:layout titulo="Registrar sala" modulo="segunda" seccion="salas">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-salas-registro">
  <n:migas><a href="<c:url value='/segunda/salas'/>">Salas</a> / Registrar sala</n:migas>
  <n:encabezado titulo="Registrar sala"
                descripcion="Capture los datos de la sala. Los campos con * son obligatorios."/>

  <form method="post" action="<c:url value='/segunda/salas/nueva'/>" novalidate class="mt-3">
    <n:csrf/>

    <n:tarjeta titulo="Datos de la sala">
      <div class="row g-3">
        <div class="col-md-6">
          <label for="nombre" class="form-label">Nombre de la sala *</label>
          <input id="nombre" name="nombre" type="text" maxlength="100"
                 class="form-control${not empty errores.nombre ? ' is-invalid' : ''}"
                 value="<c:out value='${salaForm.nombre}'/>">
          <n:error campo="nombre"/>
          <div class="form-text">El nombre debe ser único dentro del distrito.</div>
        </div>
        <div class="col-md-6">
          <label for="distritoId" class="form-label">Distrito judicial *</label>
          <select id="distritoId" name="distritoId"
                  class="form-select${not empty errores.distritoId ? ' is-invalid' : ''}">
            <option value="">Seleccione un distrito</option>
            <c:forEach var="d" items="${distritos}">
              <option value="<c:out value='${d.clave}'/>"${d.clave eq salaForm.distritoId ? ' selected' : ''}>
                <c:out value="${d.nombre}"/>
              </option>
            </c:forEach>
          </select>
          <n:error campo="distritoId"/>
        </div>
        <div class="col-12">
          <label for="direccion" class="form-label">Dirección *</label>
          <input id="direccion" name="direccion" type="text" maxlength="255"
                 class="form-control${not empty errores.direccion ? ' is-invalid' : ''}"
                 value="<c:out value='${salaForm.direccion}'/>">
          <n:error campo="direccion"/>
        </div>
      </div>
    </n:tarjeta>

    <div class="d-flex justify-content-end gap-2 sin-form-acciones">
      <a class="btn btn-sij-secundario" href="<c:url value='/segunda/salas'/>">Cancelar</a>
      <button type="submit" class="btn btn-primary">Guardar sala</button>
    </div>
  </form>

        </div>
    </jsp:body>
</t:layout>

