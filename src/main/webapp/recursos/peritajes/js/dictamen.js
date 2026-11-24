/*
 * Dictamen del perito (RF 4.3 y 4.4). Usa Quill por CDN (peritajes.md 6.6).
 *
 * - Editor con formato básico: títulos, negritas, cursivas y listas.
 * - Autoguardado cada 30 segundos solo si hay cambios, y al salir de la página.
 *   Mensajes del DFR 4.3: "Guardado automáticamente" con la hora, y si falla
 *   "No se pudo guardar, se intentará de nuevo".
 * - Si otra ventana guardó antes, el servidor responde 409 y se avisa (peritajes.md 6.6).
 * - Los datos se mandan como formulario (id, contenido, version, manual) y el token CSRF en el encabezado;
 *   la respuesta es JSON (DictamenAutoguardadoServlet).
 * - Antes de subir un archivo o entregar, se guarda lo que hay en pantalla para no perderlo.
 * - En la versión entregada, el mismo Quill muestra el contenido en solo lectura.
 *
 * El HTML del dictamen llega escapado en <textarea id="contenidoDictamen"> y Quill lo convierte a sus formatos
 * (lo que no reconoce, como <script>, se descarta).
 */
document.addEventListener('DOMContentLoaded', function () {
    var textoInicial = document.getElementById('contenidoDictamen');
    if (!textoInicial || !window.Quill) {
        return;
    }

    // ---------- Versión entregada: solo lectura ----------
    var lectura = document.getElementById('lectura');
    if (lectura) {
        var visor = new Quill(lectura, {theme: 'snow', readOnly: true, modules: {toolbar: false}});
        visor.setContents(visor.clipboard.convert({html: textoInicial.value}));
        return;
    }

    // ---------- Editor ----------
    var seccion = document.getElementById('dictamen');
    var indicador = seccion.querySelector('[data-indicador-guardado]');
    var mensaje = seccion.querySelector('[data-mensaje-guardado]');
    var url = seccion.dataset.urlGuardar;
    var peritajeId = seccion.dataset.peritajeId;
    var version = parseInt(seccion.dataset.version, 10);
    var hayCambios = false;
    var bloqueado = false;          // true después de un 409: no se vuelve a guardar sobre otra ventana
    var guardando = null;           // promesa del guardado en curso

    var editor = new Quill('#editor', {
        theme: 'snow',
        placeholder: 'Redacte aquí las conclusiones técnicas del dictamen...',
        modules: {
            toolbar: {
                container: [[{header: [false, 2, 3]}], ['bold', 'italic'], [{list: 'bullet'}, {list: 'ordered'}], ['deshacer']],
                handlers: {
                    deshacer: function () {
                        this.quill.history.undo();
                    }
                }
            }
        }
    });
    editor.setContents(editor.clipboard.convert({html: textoInicial.value}), 'silent');

    editor.on('text-change', function (delta, anterior, origen) {
        if (origen === 'user') {
            hayCambios = true;
            indicador.textContent = 'Cambios sin guardar';
            indicador.className = 'sij-estado sij-estado-ambar';
        }
    });

    function mostrar(texto, tono, aviso) {
        indicador.textContent = texto;
        indicador.className = 'sij-estado sij-estado-' + tono;
        mensaje.textContent = aviso;
    }

    /** Campos del formulario que recibe el servidor (URLSearchParams se envía como application/x-www-form-urlencoded). */
    function datosGuardado(contenido, manual) {
        return new URLSearchParams({id: peritajeId, contenido: contenido, version: version, manual: manual});
    }

    /** Guarda el contenido. manual = true para "Guardar borrador" (se registra en la bitácora). */
    function guardar(manual) {
        if (bloqueado) {
            return Promise.resolve(false);
        }
        if (guardando) {
            return guardando;
        }
        var contenido = editor.root.innerHTML;
        hayCambios = false;
        mostrar('Guardando...', 'ambar', 'Guardando los cambios...');

        guardando = fetch(url, {
            method: 'POST',
            headers: SIJ.encabezadosCsrf(),
            body: datosGuardado(contenido, manual)
        }).then(function (respuesta) {
            if (respuesta.ok) {
                return respuesta.json().then(function (datos) {
                    version = datos.version;
                    mostrar('Guardado · ' + datos.guardadoEn + ' h', 'verde',
                        'Guardado automáticamente a las ' + datos.guardadoEn + ' h.');
                    return true;
                });
            }
            return respuesta.json().catch(function () { return {}; }).then(function (datos) {
                if (respuesta.status === 409) {
                    bloqueado = true;
                    mostrar('Sin guardar', 'rojo', (datos.mensaje || 'Hay cambios más recientes en otra ventana')
                        + '. Recargue la página para ver la versión más reciente.');
                } else {
                    hayCambios = true;
                    mostrar('Sin guardar', 'rojo', datos.mensaje || 'No se pudo guardar, se intentará de nuevo');
                }
                return false;
            });
        }).catch(function () {
            // Sin conexión: lo escrito se conserva en pantalla y se reintenta (DFR 3.3.2)
            hayCambios = true;
            mostrar('Sin guardar', 'rojo', 'No se pudo guardar, se intentará de nuevo');
            return false;
        }).finally(function () {
            guardando = null;
        });
        return guardando;
    }

    // Cada 30 segundos, solo si hay cambios (DFR 4.3)
    setInterval(function () {
        if (hayCambios) {
            guardar(false);
        }
    }, 30000);

    // Al salir de la página: último intento (keepalive deja terminar la petición)
    window.addEventListener('pagehide', function () {
        if (hayCambios && !bloqueado) {
            fetch(url, {
                method: 'POST',
                keepalive: true,
                headers: SIJ.encabezadosCsrf(),
                body: datosGuardado(editor.root.innerHTML, false)
            });
        }
    });

    // "Guardar borrador"
    seccion.querySelector('[data-guardar-borrador]').addEventListener('click', function () {
        guardar(true);
    });

    // Subir archivo: primero se guarda el texto, porque la página se recarga
    var formularioArchivo = document.querySelector('[data-subir-archivo]');
    formularioArchivo.addEventListener('submit', function (evento) {
        if (!hayCambios) {
            return;
        }
        evento.preventDefault();
        guardar(false).then(function () {
            formularioArchivo.submit();
        });
    });

    // Entregar: se manda lo que hay en pantalla y su versión; el servidor lo guarda y lo finaliza
    var formularioEntrega = document.querySelector('[data-finalizar]');
    formularioEntrega.addEventListener('submit', function () {
        var campoContenido = formularioEntrega.querySelector('[data-campo-contenido]');
        var campoVersion = formularioEntrega.querySelector('[data-campo-version]');
        campoContenido.value = editor.root.innerHTML;
        campoVersion.value = version;
        campoContenido.disabled = false;
        campoVersion.disabled = false;
        hayCambios = false;   // ya va en el formulario: no hace falta el guardado de salida
    });
});
