/*
 * Detalle del peritaje (administradores).
 * Si la reasignación falló, CasoReasignarServlet marca la ventana con data-abrir-al-cargar para volver a mostrarla
 * con la justificación que se había escrito.
 */
document.addEventListener('DOMContentLoaded', function () {
    var ventana = document.querySelector('#ventanaReasignar[data-abrir-al-cargar]');
    if (ventana && window.bootstrap) {
        bootstrap.Modal.getOrCreateInstance(ventana).show();
    }
});
