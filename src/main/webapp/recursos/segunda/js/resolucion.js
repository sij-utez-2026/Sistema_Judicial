/*
 * Editor enriquecido de la resolución (Segunda Instancia, RF 7.1).
 * Quill 2.0.2 se carga desde layout.tag (jsp:attribute "scripts").
 * El HTML viaja al servlet en el textarea oculto; el servicio aplica una lista segura de etiquetas.
 */
document.addEventListener('DOMContentLoaded', function () {
    var contenedor = document.getElementById('editorComentarios');
    var form = document.getElementById('formResolucion');
    var oculto = document.getElementById('inputComentarios');
    var aviso = document.getElementById('avisoComentarios');
    if (!contenedor || !form || !oculto || !window.Quill) return;

    var quill = new Quill(contenedor, {
        theme: 'snow',
        placeholder: 'Escriba el resolutivo de la resolución…',
        modules: {
            toolbar: [
                ['bold', 'italic', 'underline'],
                [{ 'list': 'ordered' }, { 'list': 'bullet' }],
                ['clean']
            ]
        }
    });
    if (oculto.value.trim()) {
        // Quill convierte el HTML en Delta desde un documento aislado; no insertarlo
        // como innerHTML en el documento activo, ya que puede disparar atributos/eventos.
        quill.setContents(quill.clipboard.convert({ html: oculto.value }));
    }

    form.addEventListener('submit', function (evento) {
        var vacio = quill.getText().trim().length === 0;
        if (vacio) {
            evento.preventDefault();
            if (aviso) aviso.classList.remove('d-none');
            contenedor.classList.add('is-invalid');
            quill.focus();
            return;
        }
        if (aviso) aviso.classList.add('d-none');
        oculto.value = quill.root.innerHTML;
    });
});
