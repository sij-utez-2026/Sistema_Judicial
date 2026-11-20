/*
 * JavaScript propio de Copias Certificadas (EQUIPO 5).
 *   <button data-imprimir>  abre la ventana de impresión del navegador (comprobante)
 *   <input type="color" data-color="#idDelTexto">  mantiene igual el selector de color y su campo de texto
 */
(function () {
    'use strict';

    document.querySelectorAll('[data-imprimir]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            window.print();
        });
    });

    // Selector de color ↔ campo de texto #RRGGBB (administración de colores)
    document.querySelectorAll('input[type="color"][data-color]').forEach(function (selector) {
        var texto = document.querySelector(selector.dataset.color);
        var muestra = document.querySelector(selector.dataset.muestra);
        if (!texto) {
            return;
        }
        function pintar(valor) {
            if (muestra) {
                muestra.style.background = valor;
            }
        }
        selector.addEventListener('input', function () {
            texto.value = selector.value.toUpperCase();
            pintar(selector.value);
        });
        texto.addEventListener('input', function () {
            if (/^#[0-9A-Fa-f]{6}$/.test(texto.value)) {
                selector.value = texto.value;
                pintar(texto.value);
            }
        });
    });

    // Formato visual de tarjeta simulada (estilo Stripe Checkout, sin cobro real)
    var numTarjeta = document.getElementById('numeroTarjeta');
    var vencTarjeta = document.getElementById('vencimientoTarjeta');
    var cvcTarjeta = document.getElementById('cvcTarjeta');
    if (numTarjeta) {
        numTarjeta.addEventListener('input', function () {
            var digitos = numTarjeta.value.replace(/\D/g, '').slice(0, 16);
            numTarjeta.value = digitos.replace(/(.{4})/g, '$1 ').trim();
        });
    }
    if (vencTarjeta) {
        vencTarjeta.addEventListener('input', function () {
            var digitos = vencTarjeta.value.replace(/\D/g, '').slice(0, 4);
            vencTarjeta.value = digitos.length > 2 ? digitos.slice(0, 2) + '/' + digitos.slice(2) : digitos;
        });
    }
    if (cvcTarjeta) {
        cvcTarjeta.addEventListener('input', function () {
            cvcTarjeta.value = cvcTarjeta.value.replace(/\D/g, '').slice(0, 4);
        });
    }
    document.querySelectorAll('[data-tarjeta-prueba]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            if (numTarjeta) numTarjeta.value = '4242 4242 4242 4242';
            if (vencTarjeta) vencTarjeta.value = '12/28';
            if (cvcTarjeta) cvcTarjeta.value = '424';
        });
    });
})();
