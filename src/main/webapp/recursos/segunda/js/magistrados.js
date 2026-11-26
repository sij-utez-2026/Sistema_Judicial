/*
 * Mejoras de las pantallas de magistrados (lista, registro y edición).
 * Lo común (filtros con data-autoenvio, confirmaciones, evitar doble envío) lo hace comun.js.
 */
document.addEventListener('DOMContentLoaded', function () {

    // ---------- Cambio de estado en la edición ----------
    var formularioEstado = document.querySelector('[data-formulario-estado]');
    if (formularioEstado) {
        var botonAplicar = formularioEstado.querySelector('[data-aplicar-estado]');
        var estadoActual = formularioEstado.dataset.estadoActual;   // "true" o "false"
        var nombre = formularioEstado.dataset.nombre;

        var revisar = function () {
            var seleccionado = formularioEstado.querySelector('input[name="activo"]:checked').value;
            botonAplicar.disabled = seleccionado === estadoActual;
            formularioEstado.dataset.confirmar = seleccionado === 'false'
                ? '¿Dar de baja a ' + nombre + '? Su sala será liberada.'
                : '¿Reactivar a ' + nombre + '? Volverá a poder recibir tocas.';
        };
        formularioEstado.querySelectorAll('input[name="activo"]').forEach(function (opcion) {
            opcion.addEventListener('change', revisar);
        });
        revisar();
    }
});


