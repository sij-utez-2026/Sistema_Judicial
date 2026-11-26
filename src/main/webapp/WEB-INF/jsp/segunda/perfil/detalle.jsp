<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Perfil de la cuenta autenticada en Segunda Instancia. --%>
<t:layout titulo="Mi perfil" modulo="segunda" seccion="perfil">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-perfil-detalle">
  <n:migas>Mi perfil</n:migas>
  <n:encabezado titulo="Mi perfil"
                descripcion="Datos de su cuenta y de su adscripción en Segunda Instancia.">
    <a class="btn btn-sij-secundario" href="<c:url value='/cuenta/password'/>">Cambiar contraseña</a>
  </n:encabezado>

  <div class="row g-3 mt-1 justify-content-center">
    <div class="col-lg-8">
      <n:tarjeta titulo="Datos de la cuenta">
        <div class="row g-3 sij-texto-chico sin-datos">
          <div class="col-md-6">
            <div class="text-muted">Nombre</div>
            <div class="fw-semibold"><c:out value="${perfil.nombreCompleto}"/></div>
          </div>
          <div class="col-md-6">
            <div class="text-muted">Correo</div>
            <div class="fw-semibold"><c:out value="${perfil.correo}"/></div>
          </div>
          <div class="col-md-6">
            <div class="text-muted">Roles</div>
            <div class="fw-semibold">
              <c:forEach var="r" items="${perfil.roles}" varStatus="st">
                <n:estado valor="${r}"/><c:if test="${not st.last}"> </c:if>
              </c:forEach>
            </div>
          </div>
          <div class="col-md-6">
            <div class="text-muted">Estado de la cuenta</div>
            <div><n:estado valor="${perfil.activo ? 'Activa' : 'Inactiva'}"/></div>
          </div>
        </div>
      </n:tarjeta>

      <c:if test="${perfil.magistrado}">
        <n:tarjeta titulo="Adscripción">
          <div class="row g-3 sij-texto-chico sin-datos">
            <div class="col-md-6">
              <div class="text-muted">Sala asignada</div>
              <div class="fw-semibold">
                <c:choose>
                  <c:when test="${empty perfil.salaNombre}">
                    <span class="text-muted">Sin sala asignada</span>
                  </c:when>
                  <c:otherwise><c:out value="${perfil.salaNombre}"/></c:otherwise>
                </c:choose>
              </div>
            </div>
            <div class="col-md-6">
              <div class="text-muted">Distrito</div>
              <div class="fw-semibold">
                <c:choose>
                  <c:when test="${empty perfil.distritoNombre}"><span class="text-muted">Sin distrito asignado</span></c:when>
                  <c:otherwise><c:out value="${perfil.distritoNombre}"/></c:otherwise>
                </c:choose>
              </div>
            </div>
          </div>
        </n:tarjeta>
      </c:if>
    </div>
  </div>

        </div>
    </jsp:body>
</t:layout>
