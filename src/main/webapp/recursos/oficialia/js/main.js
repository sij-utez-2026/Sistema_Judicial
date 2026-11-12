/**
 * JavaScript para Oficialía de Partes - Poder Judicial de Morelos
 * Diseños actualizados - Figma Noviembre 2026
 */

document.addEventListener("DOMContentLoaded", function () {
    initModals();
    initLiveSearch();
    initPasswordToggle();
});

/* =========================================================
   GESTIÓN DE MODALES
   ========================================================= */
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add("active");
        document.body.style.overflow = "hidden";
        // Autoenfoque en primer input
        const firstInput = modal.querySelector("input:not([type=hidden]), textarea, select");
        if (firstInput) {
            setTimeout(() => firstInput.focus(), 100);
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
    // Cerrar con botones de cerrar modal
    document.querySelectorAll(".modal-close, [data-close-modal], .btn-close, .ofi-modal-close").forEach(btn => {
        btn.addEventListener("click", function () {
            const backdrop = this.closest(".modal-backdrop, .ofi-modal-backdrop");
            if (backdrop) {
                closeModal(backdrop.id);
            }
        });
    });

    // Cerrar al hacer click fuera de la tarjeta modal
    document.querySelectorAll(".modal-backdrop, .ofi-modal-backdrop").forEach(backdrop => {
        backdrop.addEventListener("click", function (e) {
            if (e.target === this) {
                closeModal(this.id);
            }
        });
    });

    // Cerrar con Escape
    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
            document.querySelectorAll(".modal-backdrop.active, .ofi-modal-backdrop.active").forEach(modal => {
                closeModal(modal.id);
            });
        }
    });
}

/* =========================================================
   BÚSQUEDA Y FILTRADO EN TIEMPO REAL EN TABLAS
   ========================================================= */
function initLiveSearch() {
    const searchInputs = document.querySelectorAll(".search-input");
    searchInputs.forEach(input => {
        input.addEventListener("input", function () {
            const query = this.value.toLowerCase().trim();
            const container = this.closest(".table-card, .sij-tarjeta");
            if (!container) return;
            const table = container.querySelector(".custom-table, .sij-tabla");
            if (!table) return;

            const rows = table.querySelectorAll("tbody tr");
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

            // Actualizar badges de conteo
            const counterBadge = container.querySelector(".table-records-badge, .sij-contador");
            if (counterBadge) {
                counterBadge.textContent = `${visibleCount} registro${visibleCount === 1 ? '' : 's'}`;
            }

            const footerBadge = container.querySelector(".table-footer");
            if (footerBadge) {
                footerBadge.textContent = `Mostrando ${visibleCount} de ${rows.length} registros`;
            }
        });
    });
}

/* =========================================================
   TOGGLE DE VER CONTRASEÑA EN LOGIN
   ========================================================= */
function initPasswordToggle() {
    const toggleBtns = document.querySelectorAll(".password-toggle-btn");
    toggleBtns.forEach(btn => {
        btn.addEventListener("click", function (e) {
            e.preventDefault();
            const parent = this.closest(".ofi-input-group, .input-group");
            const input = parent ? parent.querySelector("input") : this.previousElementSibling;
            if (input && (input.type === "password" || input.type === "text")) {
                if (input.type === "password") {
                    input.type = "text";
                    this.setAttribute("title", "Ocultar contraseña");
                } else {
                    input.type = "password";
                    this.setAttribute("title", "Mostrar contraseña");
                }
            }
        });
    });
}

/* =========================================================
   HELPERS DE EDICIÓN Y ELIMINACIÓN DE REGISTROS
   ========================================================= */
function prepararEditarDistrito(clave, nombre) {
    const hidden = document.getElementById("edit-distrito-clave-hidden");
    const display = document.getElementById("edit-distrito-clave-display");
    const nameInput = document.getElementById("edit-distrito-nombre");
    if (hidden) hidden.value = clave;
    if (display) display.value = clave;
    if (nameInput) nameInput.value = nombre;
    openModal("modalEditarDistrito");
}

function prepararEliminarDistrito(clave, nombre) {
    const claveInput = document.getElementById("delete-distrito-clave");
    const nameBox = document.getElementById("delete-distrito-nombre");
    if (claveInput) claveInput.value = clave;
    if (nameBox) nameBox.textContent = `${clave} - ${nombre}`;
    openModal("modalEliminarDistrito");
}

function prepararEditarJuzgado(clave, nombre, distrito, estado) {
    const hidden = document.getElementById("edit-juzgado-clave-hidden");
    const display = document.getElementById("edit-juzgado-clave-display");
    const nameInput = document.getElementById("edit-juzgado-nombre");
    const distSelect = document.getElementById("edit-juzgado-distrito");
    const stInput = document.getElementById("edit-juzgado-estado");
    if (hidden) hidden.value = clave;
    if (display) display.value = clave;
    if (nameInput) nameInput.value = nombre;
    if (distSelect) distSelect.value = distrito;
    if (stInput && estado) stInput.value = estado;
    openModal("modalEditarJuzgado");
}

function prepararEliminarJuzgado(clave, nombre) {
    const claveInput = document.getElementById("delete-juzgado-clave");
    const nameBox = document.getElementById("delete-juzgado-nombre");
    if (claveInput) claveInput.value = clave;
    if (nameBox) nameBox.textContent = `${clave} - ${nombre}`;
    openModal("modalEliminarJuzgado");
}

function prepararEditarOficialia(clave, nombre, juzgado, estado) {
    const hidden = document.getElementById("edit-oficialia-clave-hidden");
    const display = document.getElementById("edit-oficialia-clave-display");
    const nameInput = document.getElementById("edit-oficialia-nombre");
    const juzSelect = document.getElementById("edit-oficialia-juzgado");
    const stInput = document.getElementById("edit-oficialia-estado");
    if (hidden) hidden.value = clave;
    if (display) display.value = clave;
    if (nameInput) nameInput.value = nombre;
    if (juzSelect) juzSelect.value = juzgado;
    if (stInput && estado) stInput.value = estado;
    openModal("modalEditarOficialia");
}

function prepararEliminarOficialia(clave, nombre) {
    const claveInput = document.getElementById("delete-oficialia-clave");
    const nameBox = document.getElementById("delete-oficialia-nombre");
    if (claveInput) claveInput.value = clave;
    if (nameBox) nameBox.textContent = `${clave} - ${nombre}`;
    openModal("modalEliminarOficialia");
}

function prepararEditarMateria(clave, nombre, descripcion, estado) {
    const hidden = document.getElementById("edit-materia-clave-hidden");
    const display = document.getElementById("edit-materia-clave-display");
    const nameInput = document.getElementById("edit-materia-nombre");
    const descInput = document.getElementById("edit-materia-desc");
    const stInput = document.getElementById("edit-materia-estado");
    if (hidden) hidden.value = clave;
    if (display) display.value = clave;
    if (nameInput) nameInput.value = nombre;
    if (descInput) descInput.value = descripcion;
    if (stInput && estado) stInput.value = estado;
    openModal("modalEditarMateria");
}

function prepararEliminarMateria(clave, nombre) {
    const claveInput = document.getElementById("delete-materia-clave");
    const nameBox = document.getElementById("delete-materia-nombre");
    if (claveInput) claveInput.value = clave;
    if (nameBox) nameBox.textContent = `${clave} - ${nombre}`;
    openModal("modalEliminarMateria");
}
