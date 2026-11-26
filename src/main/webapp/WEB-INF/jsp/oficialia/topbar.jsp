<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="ofi-topbar d-flex justify-content-between align-items-center gap-2">
    <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary d-lg-none" type="button" data-bs-toggle="offcanvas"
                data-bs-target="#sijMenu" aria-controls="sijMenu" aria-label="Abrir menú">&#9776;</button>
        <div>
            <div class="ofi-topbar-tag">Sistema de Oficialía de Partes</div>
            <h1 class="ofi-topbar-title">Panel de Administración</h1>
        </div>
    </div>
    <div class="d-none d-md-flex align-items-center gap-3">
        <span class="sij-texto-chico text-nowrap"><c:out value="${fechaHoraActual}"/></span>
        <span class="badge" style="background-color: var(--sij-primario-claro); color: var(--sij-primario); font-weight: 600; font-size: 0.72rem; padding: 0.35rem 0.65rem; border-radius: 4px;">
            Superadministrador
        </span>
    </div>
</header>
