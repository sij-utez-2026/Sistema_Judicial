<%@ tag description="Mensaje de error de un campo del formulario" pageEncoding="UTF-8" %>
<%--
  Muestra el error de validación de un campo, si lo hay. El servlet deja los errores en "errores"
  (Map campo → mensaje, ver Validador):

    <input name="nombre" class="form-control${not empty errores.nombre ? ' is-invalid' : ''}" ...>
    <n:error campo="nombre"/>

  clase: clases del texto (por omisión "invalid-feedback", que Bootstrap muestra debajo de un campo is-invalid).
         Para errores que no van debajo de un input, usar clase="text-danger small".
--%>
<%@ attribute name="campo" required="true" %>
<%@ attribute name="clase" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty errores[campo]}">
    <div class="<c:out value='${empty clase ? "invalid-feedback" : clase}'/>"><c:out value="${errores[campo]}"/></div>
</c:if>
