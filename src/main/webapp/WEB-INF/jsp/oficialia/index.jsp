<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Redirige al Servlet principal de inicio (el filtro se encargará de llevar a /login si no hay sesión)
    response.sendRedirect(request.getContextPath() + "/inicio");
%>
