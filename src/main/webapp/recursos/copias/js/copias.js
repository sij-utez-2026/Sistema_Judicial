document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-imprimir-comprobante]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            window.print();
        });
    });
});
