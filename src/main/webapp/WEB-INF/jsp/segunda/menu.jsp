<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Menú lateral del módulo de Segunda Instancia (EQUIPO 4).
  - clave: la misma que la pantalla pone en <t:layout seccion="...">.
  - Sin url = pantalla pendiente (aparece desactivada).
  - FiltroNucleo valida la sesión y el rol; SegundaPermisos agrega el permiso fino del módulo.
--%>
<n:menu-item clave="inicio"       url="/segunda/inicio"             texto="Inicio"            icono="inicio"/>
<n:menu-item clave="tocas"        url="/segunda/tocas"              texto="${segundaEsAdmin ? 'Tocas' : 'Mis tocas'}" icono="carpeta"/>
<n:menu-item clave="ponencias"    url="/segunda/ponencias"          texto="Ponencias"       icono="documento"/>
<n:menu-item clave="agenda"       url="/segunda/agenda"             texto="Agenda"            icono="calendario"/>
<n:menu-item clave="resoluciones" url="/segunda/resoluciones"       texto="Resoluciones"      icono="balanza"/>
<c:if test="${segundaEsAdmin}">
<n:menu-item clave="salas"        url="/segunda/salas"              texto="Salas"             icono="edificio"/>
<n:menu-item clave="magistrados"  url="/segunda/magistrados"        texto="Magistrados"       icono="personas"/>
<n:menu-item clave="distritos"    url="/segunda/distritos"          texto="Distritos"         icono="edificio"/>
<n:menu-item clave="reasignacion" url="/segunda/tocas/reasignar"    texto="Reasignación"      icono="documento"/>
</c:if>
<c:if test="${segundaEsAdmin}">
  <n:menu-item clave="bitacora" url="/segunda/bitacora"             texto="Bitácora"          icono="bitacora"/>
</c:if>
<n:menu-item clave="perfil"       url="/segunda/perfil"             texto="Mi perfil"         icono="persona"/>
