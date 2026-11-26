<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Registrar resolución" modulo="segunda" seccion="tocas">

    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.snow.css">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script src="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.js"></script>
        <script src="<c:url value='/recursos/segunda/js/resolucion.js?v=2'/>"></script>
    </jsp:attribute>

  <jsp:body>
        <div class="sin-pantalla sin-vista-tocas-resolucion">
    <n:migas>
      <a href="<c:url value='/segunda/tocas'/>">Tocas</a> /
      <a href="<c:url value='/segunda/tocas/detalle?id=${toca.id}'/>"><c:out value="${toca.folioToca}"/></a> /
      Resolución
    </n:migas>
    <n:encabezado titulo="Registrar resolución"
                  descripcion="${toca.folioToca} · Expediente ${toca.numeroExpediente}"/>

    <c:if test="${not empty error}">
      <div class="sij-aviso sij-aviso-advertencia p-3 mb-3">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <form id="formResolucion" method="post"
          action="<c:url value='/segunda/tocas/resolucion?id=${toca.id}'/>" class="mt-3" novalidate>
      <n:csrf/>
      <n:tarjeta titulo="Datos de la resolución">
        <div class="row g-3">
          <div class="col-12">
            <label for="sentido" class="form-label">Sentido *</label>
            <select id="sentido" name="sentido"
                    class="form-select${not empty errores.sentido ? ' is-invalid' : ''}" required>
              <option value="">Seleccione el sentido</option>
              <option value="CONFIRMA"${resolucionForm.sentido eq 'CONFIRMA' ? ' selected' : ''}>Confirma</option>
              <option value="REVOCA"${resolucionForm.sentido eq 'REVOCA' ? ' selected' : ''}>Revoca</option>
              <option value="MODIFICA"${resolucionForm.sentido eq 'MODIFICA' ? ' selected' : ''}>Modifica</option>
            </select>
            <n:error campo="sentido"/>
          </div>
          <div class="col-12">
            <label for="editorComentarios" class="form-label">Comentarios / Resolutivo *</label>
            <div id="editorComentarios" class="bg-white${not empty errores.comentarios ? ' is-invalid' : ''}"></div>
            <textarea id="inputComentarios" name="comentarios" hidden><c:out value="${resolucionForm.comentarios}"/></textarea>
            <div id="avisoComentarios" class="invalid-feedback">Escriba el resolutivo de la resolución.</div>
            <div class="form-text">Texto enriquecido, hasta 2,000 caracteres de contenido.</div>
            <n:error campo="comentarios"/>
          </div>
          <div class="col-12">
            <div class="sij-aviso sij-aviso-informacion p-3">
              Al registrar la resolución, el sistema generará automáticamente el PDF a partir de este texto y lo guardará como documento de la toca.
            </div>
          </div>
        </div>
      </n:tarjeta>

      <div class="d-flex justify-content-end gap-2 sin-form-acciones">
        <a class="btn btn-sij-secundario"
           href="<c:url value='/segunda/tocas/detalle?id=${toca.id}'/>">Cancelar</a>
        <button type="submit" class="btn btn-primary">Registrar resolución</button>
      </div>
    </form>
        </div>
    </jsp:body>
</t:layout>
