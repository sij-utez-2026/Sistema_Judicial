<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Bitácora" modulo="segunda" seccion="bitacora">
  <jsp:attribute name="estilos"><link rel="stylesheet" href="<c:url value='/recursos/segunda/css/bitacora.css'/>"><link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>"></jsp:attribute>
  <jsp:body>
    <n:migas>Bitácora</n:migas>
    <n:encabezado titulo="Bitácora de Segunda Instancia" descripcion="Consulta de registros persistidos en bitacora_Seg. Solo lectura."/>
    <c:if test="${not empty error}"><div class="sij-aviso sij-aviso-advertencia p-3 mb-3"><c:out value="${error}"/></div></c:if>
    <form class="row g-2 my-3" method="get" action="<c:url value='/segunda/bitacora'/>">
      <div class="col-lg-4 col-md-6"><input type="search" class="form-control" name="q" value="<c:out value='${q}'/>" placeholder="Buscar por acción, tabla, usuario o detalle"></div>
      <div class="col-lg-3 col-md-6"><select class="form-select" name="accion"><option value="">Acción: todas</option><c:forEach var="a" items="${acciones}"><option value="<c:out value='${a}'/>"${a eq accion ? ' selected' : ''}><c:out value="${a}"/></option></c:forEach></select></div>
      <div class="col-lg-2 col-md-6"><label class="visually-hidden" for="desde">Desde</label><input id="desde" type="date" class="form-control" name="desde" value="<c:out value='${desde}'/>"></div>
      <div class="col-lg-2 col-md-6"><label class="visually-hidden" for="hasta">Hasta</label><input id="hasta" type="date" class="form-control" name="hasta" value="<c:out value='${hasta}'/>"></div>
      <div class="col-lg-1"><button class="btn btn-sij-secundario w-100" type="submit">Filtrar</button></div>
      <input type="hidden" name="size" value="<c:out value='${size}'/>">
    </form>
    <n:tarjeta sinRelleno="true">
      <c:choose><c:when test="${empty pagina.content}"><div class="p-4 text-center sij-descripcion">No hay registros en bitacora_Seg con esos filtros.</div></c:when>
      <c:otherwise><div class="table-responsive"><table class="table sij-tabla align-middle mb-0">
        <thead><tr><th>Fecha y hora</th><th>Usuario</th><th>Acción</th><th>Fuente / tabla</th><th>Registro</th><th>Detalle</th><th></th></tr></thead>
        <tbody><c:forEach var="b" items="${pagina.content}"><tr>
          <td class="sij-texto-chico text-nowrap"><c:out value="${b.fechaTexto}"/></td>
          <td><c:out value="${b.usuarioNombre}"/><c:if test="${empty b.usuarioNombre}"><span class="text-muted">Sistema/externo</span></c:if></td>
          <td><c:out value="${b.accion}"/></td>
          <td class="sij-texto-chico"><c:out value="${b.tipoFuente}"/><c:if test="${not empty b.nombreTabla}"> · <c:out value="${b.nombreTabla}"/></c:if></td>
          <td><c:out value="${b.registroId}"/></td>
          <td class="sij-texto-chico"><c:out value="${b.detalle}"/></td>
          <td><a class="btn btn-sm btn-sij-secundario" href="<c:url value='/segunda/bitacora/detalle?id=${b.id}'/>">Ver</a></td>
        </tr></c:forEach></tbody></table></div></c:otherwise></c:choose>
    </n:tarjeta>
    <c:if test="${not empty pagina.content}"><div class="d-flex flex-wrap align-items-center gap-3 mt-3">
      <form class="d-flex align-items-center gap-2" method="get" action="<c:url value='/segunda/bitacora'/>">
        <input type="hidden" name="q" value="<c:out value='${q}'/>"><input type="hidden" name="accion" value="<c:out value='${accion}'/>"><input type="hidden" name="desde" value="<c:out value='${desde}'/>"><input type="hidden" name="hasta" value="<c:out value='${hasta}'/>">
        <label for="size" class="sij-texto-chico">Registros por página</label><select id="size" name="size" class="form-select form-select-sm w-auto"><c:forEach var="tam" items="${tamanosPagina}"><option value="<c:out value='${tam}'/>"${tam eq size ? ' selected' : ''}><c:out value="${tam}"/></option></c:forEach></select>
      </form><div class="flex-grow-1"><n:paginacion pagina="${pagina}" url="/segunda/bitacora"/></div>
    </div></c:if>
    <p class="sij-texto-chico mt-3">La pantalla es de solo lectura. El esquema no incluye IP ni sesión de usuario en bitacora_Seg.</p>
  </jsp:body>
</t:layout>
