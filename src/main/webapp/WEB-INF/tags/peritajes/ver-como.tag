<%@ tag description="TEMPORAL: con qué usuario de prueba se ve el módulo" pageEncoding="UTF-8" %>
<%--
    <p:ver-como/>   (taglib prefix="p" tagdir="/WEB-INF/tags/peritajes")
  TEMPORAL: mientras no hay inicio de sesión, recuerda con qué usuario y rol se está probando ("Ver como").
  Se borra cuando exista el inicio de sesión del núcleo (el usuario ya sale en el menú lateral).
  Necesita el atributo usuarioPeritajes que ponen los servlets del módulo (PeritajesAuth.exigir...).
--%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty usuarioPeritajes}">
    <div class="per-ver-como sij-texto-chico mb-2">
        Viendo como <strong><c:out value="${usuarioPeritajes.nombre}"/></strong>
        · <c:out value="${usuarioPeritajes.rol}"/>
        · <a href="<c:url value='/peritajes'/>">Cambiar</a>
    </div>
</c:if>
