<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Registro de bitácora" modulo="segunda" seccion="bitacora">
  <n:migas><a href="<c:url value='/segunda/bitacora'/>">Bitácora</a> / Registro #<c:out value="${registro.id}"/></n:migas>
  <n:encabezado titulo="Registro #${registro.id}" descripcion="Detalle leído de bitacora_Seg; no se permite editar ni borrar."/>
  <div class="row g-3 mt-1">
    <div class="col-lg-8"><n:tarjeta titulo="Datos del evento">
      <dl class="row sij-texto-chico mb-0">
        <dt class="col-sm-4">Fecha y hora</dt><dd class="col-sm-8"><c:out value="${registro.fechaTexto}"/></dd>
        <dt class="col-sm-4">Fuente</dt><dd class="col-sm-8"><c:out value="${registro.tipoFuente}"/></dd>
        <dt class="col-sm-4">Tabla</dt><dd class="col-sm-8"><c:out value="${registro.nombreTabla}"/></dd>
        <dt class="col-sm-4">ID registro</dt><dd class="col-sm-8"><c:out value="${registro.registroId}"/></dd>
        <dt class="col-sm-4">Acción</dt><dd class="col-sm-8"><c:out value="${registro.accion}"/></dd>
        <dt class="col-sm-4">Usuario</dt><dd class="col-sm-8"><c:out value="${registro.usuarioNombre}"/> <span class="text-muted">(ID <c:out value="${registro.usuarioId}"/>)</span></dd>
        <dt class="col-sm-4">Detalle</dt><dd class="col-sm-8"><c:out value="${registro.detalle}"/></dd>
        <dt class="col-sm-4">Tono</dt><dd class="col-sm-8"><c:out value="${registro.tono}"/></dd>
      </dl>
    </n:tarjeta>
    <n:tarjeta titulo="Valores anteriores"><pre class="small text-wrap mb-0"><c:out value="${registro.valoresAnteriores}"/></pre></n:tarjeta>
    <n:tarjeta titulo="Valores nuevos"><pre class="small text-wrap mb-0"><c:out value="${registro.valoresNuevos}"/></pre></n:tarjeta></div>
    <div class="col-lg-4"><n:tarjeta titulo="Acciones"><a class="btn btn-sij-secundario w-100" href="<c:url value='/segunda/bitacora'/>">Volver a bitácora</a></n:tarjeta></div>
  </div>
</t:layout>
