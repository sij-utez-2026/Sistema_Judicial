/**
 * JavaScript para Oficialía de Partes - Poder Judicial del Estado de Morelos
 */

document.addEventListener("DOMContentLoaded", function () {
    initModals();
    initLiveSearch();
    initActionButtons();
    initLogoPreview();
});

/* =========================================================
   GESTIÓN DE MODALES
   ========================================================= */
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add("active");
        document.body.style.overflow = "hidden";
        const firstInput = modal.querySelector("input:not([type=hidden]):not([readonly]), textarea, select");
        if (firstInput) {
            setTimeout(() => firstInput.focus(), 80);
        }
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove("active");
        document.body.style.overflow = "";
    }
}

function initModals() {
    document.querySelectorAll("[data-open-modal]").forEach(btn => {
        btn.addEventListener("click", function () {
            openModal(this.dataset.openModal);
        });
    });

    document.querySelectorAll("[data-close-modal], .ofi-modal-close").forEach(btn => {
        btn.addEventListener("click", function () {
            const backdrop = this.closest(".ofi-modal-backdrop");
            if (backdrop) {
                closeModal(backdrop.id);
            }
        });
    });

    document.querySelectorAll(".ofi-modal-backdrop").forEach(backdrop => {
        backdrop.addEventListener("click", function (e) {
            if (e.target === this) {
                closeModal(this.id);
            }
        });
    });

    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
            document.querySelectorAll(".ofi-modal-backdrop.active").forEach(modal => {
                closeModal(modal.id);
            });
        }
    });
}

/* =========================================================
   BÚSQUEDA EN TIEMPO REAL EN TABLAS
   ========================================================= */
function initLiveSearch() {
    const searchInputs = document.querySelectorAll(".search-input");
    searchInputs.forEach(input => {
        input.addEventListener("input", function () {
            const query = this.value.toLowerCase().trim();
            const container = this.closest(".sij-tarjeta");
            if (!container) return;
            const table = container.querySelector(".sij-tabla");
            if (!table) return;

            const rows = table.querySelectorAll("tbody tr[data-fila]");
            let visibleCount = 0;

            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(query)) {
                    row.style.display = "";
                    visibleCount++;
                } else {
                    row.style.display = "none";
                }
            });

            const counterBadge = container.querySelector(".table-records-badge");
            if (counterBadge) {
                counterBadge.textContent = `${visibleCount} registro${visibleCount === 1 ? "" : "s"}`;
            }

            const footerBadge = container.querySelector(".table-footer");
            if (footerBadge) {
                footerBadge.textContent = `Mostrando ${visibleCount} de ${rows.length} registros`;
            }
        });
    });
}

/* =========================================================
   BOTONES DE EDICIÓN, ELIMINACIÓN Y ASIGNACIÓN DE ADMIN
   ========================================================= */
function initActionButtons() {
    // Distritos
    document.querySelectorAll("[data-editar-distrito]").forEach(btn => {
        btn.addEventListener("click", function () {
            const { clave, nombre, cabecera } = this.dataset;
            document.getElementById("edit-distrito-clave-hidden").value = clave || "";
            document.getElementById("edit-distrito-clave-display").value = clave || "";
            document.getElementById("edit-distrito-nombre").value = nombre || "";
            const cab = document.getElementById("edit-distrito-cabecera");
            if (cab) cab.value = cabecera || "";
            openModal("modalEditarDistrito");
        });
    });

    document.querySelectorAll("[data-eliminar-distrito]").forEach(btn => {
        btn.addEventListener("click", function () {
            const { clave, nombre } = this.dataset;
            document.getElementById("delete-distrito-clave").value = clave || "";
            document.getElementById("delete-distrito-nombre").textContent = `${clave} - ${nombre}`;
            openModal("modalEliminarDistrito");
        });
    });

    // Juzgados
    document.querySelectorAll("[data-editar-juzgado]").forEach(btn => {
        btn.addEventListener("click", function () {
            const { clave, nombre, clavedistrito } = this.dataset;
            document.getElementById("edit-juzgado-clave-hidden").value = clave || "";
            document.getElementById("edit-juzgado-clave-display").value = clave || "";
            document.getElementById("edit-juzgado-nombre").value = nombre || "";
            document.getElementById("edit-juzgado-distrito").value = clavedistrito || "";
            openModal("modalEditarJuzgado");
        });
    });

    document.querySelectorAll("[data-eliminar-juzgado]").forEach(btn => {
        btn.addEventListener("click", function () {
            const { clave, nombre } = this.dataset;
            document.getElementById("delete-juzgado-clave").value = clave || "";
            document.getElementById("delete-juzgado-nombre").textContent = `${clave} - ${nombre}`;
            openModal("modalEliminarJuzgado");
        });
    });

    // Oficialías
    document.querySelectorAll("[data-editar-oficialia]").forEach(btn => {
        btn.addEventListener("click", function () {
            const { clave, nombre, clavejuzgado } = this.dataset;
            document.getElementById("edit-oficialia-clave-hidden").value = clave || "";
            document.getElementById("edit-oficialia-clave-display").value = clave || "";
            document.getElementById("edit-oficialia-nombre").value = nombre || "";
            document.getElementById("edit-oficialia-juzgado").value = clavejuzgado || "";
            openModal("modalEditarOficialia");
        });
    });

    document.querySelectorAll("[data-asignar-admin]").forEach(btn => {
        btn.addEventListener("click", function () {
            const { clave, nombre, admin } = this.dataset;
            document.getElementById("assign-oficialia-clave").value = clave || "";
            document.getElementById("assign-oficialia-nombre").textContent = `${clave} - ${nombre}`;
            const actualEl = document.getElementById("assign-admin-actual");
            if (actualEl) {
                actualEl.textContent = admin ? `Administrador actual: ${admin}` : "Sin administrador asignado";
            }
            openModal("modalAsignarAdmin");
        });
    });

    document.querySelectorAll("[data-eliminar-oficialia]").forEach(btn => {
        btn.addEventListener("click", function () {
            const { clave, nombre } = this.dataset;
            document.getElementById("delete-oficialia-clave").value = clave || "";
            document.getElementById("delete-oficialia-nombre").textContent = `${clave} - ${nombre}`;
            openModal("modalEliminarOficialia");
        });
    });

    // Materias
    document.querySelectorAll("[data-editar-materia]").forEach(btn => {
        btn.addEventListener("click", function () {
            const { clave, nombre, descripcion } = this.dataset;
            document.getElementById("edit-materia-clave-hidden").value = clave || "";
            document.getElementById("edit-materia-clave-display").value = clave || "";
            document.getElementById("edit-materia-nombre").value = nombre || "";
            document.getElementById("edit-materia-desc").value = descripcion || "";
            openModal("modalEditarMateria");
        });
    });

    document.querySelectorAll("[data-eliminar-materia]").forEach(btn => {
        btn.addEventListener("click", function () {
            const { clave, nombre } = this.dataset;
            document.getElementById("delete-materia-clave").value = clave || "";
            document.getElementById("delete-materia-nombre").textContent = `${clave} - ${nombre}`;
            openModal("modalEliminarMateria");
        });
    });
}

/* =========================================================
   VISTA PREVIA DE LOGOTIPO EN PERSONALIZACIÓN
   ========================================================= */
function initLogoPreview() {
    const fileInput = document.getElementById("input-logo-file");
    const nombreEl = document.getElementById("nombre-archivo-logo");
    if (!fileInput || !nombreEl) return;
    fileInput.addEventListener("change", function () {
        if (this.files && this.files.length > 0) {
            nombreEl.textContent = "Archivo seleccionado: " + this.files[0].name;
        }
    });
}
