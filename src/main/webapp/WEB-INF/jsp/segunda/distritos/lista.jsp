<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- RF 2.1 Consulta de distritos judiciales (solo lectura, catálogo de Oficialía) --%>
<t:layout titulo="Distritos" modulo="segunda" seccion="distritos">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-distritos-lista">
  <n:migas>Distritos</n:migas>

  <n:encabezado titulo="Distritos judiciales"
                descripcion="Catálogo de distritos que administra Oficialía de Partes. Solo consulta."/>

  <n:tarjeta sinRelleno="true" clase="sin-listado">
    <c:choose>
      <c:when test="${empty distritos}">
        <div class="p-4 text-center sij-descripcion">
          No hay distritos activos en el catálogo.
        </div>
      </c:when>
      <c:otherwise>
        <div class="table-responsive">
          <table class="table sij-tabla align-middle mb-0">
            <thead>
            <tr>
              <th>Distrito</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="d" items="${distritos}">
              <tr>
                <td class="fw-semibold"><c:out value="${d.nombre}"/></td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </n:tarjeta>

  <div class="sij-texto-chico mt-3">
    &#9432; Los distritos se administran en el módulo de Oficialía de Partes.
    Aquí solo se consultan para asignarlos a las salas.
  </div>

        </div>
    </jsp:body>
</t:layout>


