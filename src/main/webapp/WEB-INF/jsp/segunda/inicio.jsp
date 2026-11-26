<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Panel de trabajo de Segunda Instancia. Las tarjetas administrativas solo aparecen para SEGUNDA_ADMIN. --%>
<t:layout titulo="Inicio" modulo="segunda" seccion="inicio">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-inicio">
            <n:migas>Inicio</n:migas>
            <n:encabezado titulo="${moduloActual.nombreLargo}" descripcion="${moduloActual.descripcion}"/>

            <div class="row g-3 sin-inicio-grid">
                <div class="col-md-6 col-xl-4">
                    <a class="sij-tarjeta sin-inicio-card p-4 h-100" href="<c:url value='/segunda/tocas'/>">
                        <div class="sin-inicio-cabecera">
                            <span class="sin-inicio-icono"><n:icono nombre="carpeta" tamano="20"/></span>
                            <h2 class="sij-tarjeta-titulo sin-inicio-titulo">Mis tocas</h2>
                        </div>
                        <p class="sij-descripcion mb-0">Tocas asignadas a su sala en segunda instancia.</p>
                    </a>
                </div>
                <c:if test="${segundaEsAdmin}">
                <div class="col-md-6 col-xl-4">
                    <a class="sij-tarjeta sin-inicio-card p-4 h-100" href="<c:url value='/segunda/salas'/>">
                        <div class="sin-inicio-cabecera">
                            <span class="sin-inicio-icono"><n:icono nombre="edificio" tamano="20"/></span>
                            <h2 class="sij-tarjeta-titulo sin-inicio-titulo">Salas</h2>
                        </div>
                        <p class="sij-descripcion mb-0">Salas de segunda instancia por distrito y su magistrado.</p>
                    </a>
                </div>
                <div class="col-md-6 col-xl-4">
                    <a class="sij-tarjeta sin-inicio-card p-4 h-100" href="<c:url value='/segunda/magistrados'/>">
                        <div class="sin-inicio-cabecera">
                            <span class="sin-inicio-icono"><n:icono nombre="personas" tamano="20"/></span>
                            <h2 class="sij-tarjeta-titulo sin-inicio-titulo">Magistrados</h2>
                        </div>
                        <p class="sij-descripcion mb-0">Magistrados de segunda instancia y su sala asignada.</p>
                    </a>
                </div>
                <div class="col-md-6 col-xl-4">
                    <a class="sij-tarjeta sin-inicio-card p-4 h-100" href="<c:url value='/segunda/distritos'/>">
                        <div class="sin-inicio-cabecera">
                            <span class="sin-inicio-icono"><n:icono nombre="edificio" tamano="20"/></span>
                            <h2 class="sij-tarjeta-titulo sin-inicio-titulo">Distritos</h2>
                        </div>
                        <p class="sij-descripcion mb-0">Catálogo de distritos judiciales (solo consulta).</p>
                    </a>
                </div>
                </c:if>
                <div class="col-md-6 col-xl-4">
                    <a class="sij-tarjeta sin-inicio-card p-4 h-100" href="<c:url value='/segunda/agenda'/>">
                        <div class="sin-inicio-cabecera">
                            <span class="sin-inicio-icono"><n:icono nombre="calendario" tamano="20"/></span>
                            <h2 class="sij-tarjeta-titulo sin-inicio-titulo">Agenda</h2>
                        </div>
                        <p class="sij-descripcion mb-0">Agenda semanal de ponencias del magistrado.</p>
                    </a>
                </div>
                <div class="col-md-6 col-xl-4">
                    <a class="sij-tarjeta sin-inicio-card p-4 h-100" href="<c:url value='/segunda/resoluciones'/>">
                        <div class="sin-inicio-cabecera">
                            <span class="sin-inicio-icono"><n:icono nombre="balanza" tamano="20"/></span>
                            <h2 class="sij-tarjeta-titulo sin-inicio-titulo">Resoluciones</h2>
                        </div>
                        <p class="sij-descripcion mb-0">Resoluciones emitidas y descarga de PDF.</p>
                    </a>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>
