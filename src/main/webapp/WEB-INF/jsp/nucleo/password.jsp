<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Cambiar contraseña con la sesión iniciada (CuentaPasswordServlet). Cualquier usuario. INTEGRADORES. --%>
<t:layout titulo="Cambiar contraseña" seccion="cuenta">
    <n:migas>Cambiar contraseña</n:migas>
    <n:encabezado titulo="Cambiar contraseña"
                  descripcion="Al menos 8 caracteres, con una mayúscula, una minúscula y un número; distinta de la actual."/>

    <div class="row">
        <div class="col-md-8 col-lg-5">
            <form method="post" action="<c:url value='/cuenta/password'/>" class="mt-3" novalidate>
                <n:csrf/>
                <n:tarjeta>
                    <div class="mb-3">
                        <label for="actual" class="form-label">Contraseña actual *</label>
                        <n:contrasena nombre="actual" autocompletar="current-password"/>
                    </div>
                    <div class="mb-3">
                        <label for="nueva" class="form-label">Contraseña nueva *</label>
                        <n:contrasena nombre="nueva" autocompletar="new-password" minimo="8"/>
                    </div>
                    <div class="mb-3">
                        <label for="confirmacion" class="form-label">Confirme la contraseña nueva *</label>
                        <n:contrasena nombre="confirmacion" autocompletar="new-password" minimo="8"/>
                    </div>
                    <div class="d-flex justify-content-end gap-2">
                        <a class="btn btn-sij-secundario" href="<c:url value='/'/>">Cancelar</a>
                        <button type="submit" class="btn btn-primary">Guardar contraseña</button>
                    </div>
                </n:tarjeta>
            </form>
        </div>
    </div>
</t:layout>
