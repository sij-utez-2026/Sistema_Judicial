<%@ page pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
            <n:menu-item clave="inicio" url="/oficialia" texto="Inicio" icono="inicio" />
            <c:if test="${usuarioSesion.administradorGeneral}">
                <n:menu-item clave="distritos" url="/oficialia/distritos" texto="Distritos" icono="edificio" />
                <n:menu-item clave="juzgados" url="/oficialia/juzgados" texto="Juzgados" icono="balanza" />
                <n:menu-item clave="oficialias" url="/oficialia/oficialias" texto="Oficialías de Partes"
                    icono="carpeta" />
                <n:menu-item clave="materias" url="/oficialia/materias" texto="Materias" icono="documento" />
                <n:menu-item clave="personalizacion" url="/oficialia/personalizacion" texto="Personalización"
                    icono="editar" />
            </c:if>
            <c:if test="${not usuarioSesion.administradorGeneral}">
                <n:menu-item clave="demandas" texto="Demandas" icono="carpeta" />
                <n:menu-item clave="nueva-demanda" texto="Registrar demanda" icono="mas" />
                <n:menu-item clave="usuarios" texto="Usuarios de la oficialía" icono="personas" />
            </c:if>