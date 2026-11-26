/*
 * Mejoras de las pantallas de salas (lista, registro y edición).
 * Lo común (filtros con data-autoenvio, quitar la marca roja al corregir, confirmaciones y
 * evitar el doble envío) ya lo hace src/main/webapp/recursos/nucleo/js/comun.js.
 * Todo funciona igual sin JavaScript: el servidor siempre vuelve a validar.
 */
document.addEventListener('DOMContentLoaded', function () {

    // ---------- Cambio de estado en la edición ----------
    var formularioEstado = document.querySelector('[data-formulario-estado]');
    if (formularioEstado) {
        var botonAplicar = formularioEstado.querySelector('[data-aplicar-estado]');
        var estadoActual = formularioEstado.dataset.estadoActual;   // "true" o "false"
        var nombre = formularioEstado.dataset.nombre;

        var revisar = function () {
            var seleccionado = formularioEstado.querySelector('input[name="activa"]:checked').value;
            botonAplicar.disabled = seleccionado === estadoActual;
            // comun.js muestra la ventana de confirmación con este texto al enviar
            formularioEstado.dataset.confirmar = seleccionado === 'false'
                ? '¿Desactivar la sala "' + nombre + '"? No aparecerá en las asignaciones.'
                : '¿Reactivar la sala "' + nombre + '"? Volverá a estar disponible.';
        };
        formularioEstado.querySelectorAll('input[name="activa"]').forEach(function (opcion) {
            opcion.addEventListener('change', revisar);
        });
        revisar();
    }
});


