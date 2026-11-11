<%@ tag description="Campo oculto con el token CSRF" pageEncoding="UTF-8" %>
<%--
  Va dentro de TODO <form method="post">:
    <form method="post" action="..."> <n:csrf/> ... </form>
  FiltroNucleo deja el token en "_csrf" y rechaza con 403 los POST que no lo traen (ver CsrfToken).
--%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty _csrf}">
    <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>">
</c:if>
