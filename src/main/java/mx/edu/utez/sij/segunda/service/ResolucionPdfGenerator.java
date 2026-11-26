package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.segunda.model.Resolucion;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Generador PDF autocontenido para el módulo de Segunda Instancia.
 * No agrega librerías ni modifica el POM; produce un PDF 1.4 con texto UTF-8
 * convertido a WinAnsi y conserva el contenido visible del HTML enriquecido.
 */
public final class ResolucionPdfGenerator {
    private static final Charset PDF_CHARSET = StandardCharsets.ISO_8859_1;
    private ResolucionPdfGenerator() { }

    public static byte[] generar(Resolucion resolucion) {
        List<String> lineas = new ArrayList<>();
        lineas.add("TRIBUNAL SUPERIOR DE JUSTICIA");
        lineas.add("Resolucion de Segunda Instancia");
        lineas.add("");
        lineas.add("Toca: " + visible(resolucion.getTocaFolio()));
        lineas.add("Expediente: " + visible(resolucion.getTocaExpediente()));
        lineas.add("Sentido: " + (resolucion.getSentido() == null ? "" : resolucion.getSentido().getEtiqueta()));
        lineas.add("Fecha: " + visible(resolucion.getFechaTexto()));
        lineas.add("");
        lineas.add("RESOLUTIVO");
        lineas.addAll(envolver(htmlATexto(resolucion.getComentarios()), 92));
        return construirPdf(lineas);
    }

    private static List<String> envolver(String texto, int ancho) {
        List<String> salida = new ArrayList<>();
        for (String parrafo : texto.replace("\r", "").split("\n", -1)) {
            String restante = parrafo.trim();
            if (restante.isEmpty()) { salida.add(""); continue; }
            while (restante.length() > ancho) {
                int corte = restante.lastIndexOf(' ', ancho);
                if (corte < 1) corte = ancho;
                salida.add(restante.substring(0, corte).trim());
                restante = restante.substring(corte).trim();
            }
            salida.add(restante);
        }
        return salida;
    }

    private static String htmlATexto(String html) {
        if (html == null) return "";
        String texto = html.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</(p|li|ol|ul)\\s*>", "\n")
                .replaceAll("(?i)<li\\s*>", "- ")
                .replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ").replace("&amp;", "&")
                .replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"");
        return texto.replaceAll("[\\t ]+", " ").trim();
    }

    private static byte[] construirPdf(List<String> lineas) {
        List<List<String>> paginas = new ArrayList<>();
        List<String> pagina = new ArrayList<>();
        for (String linea : lineas) {
            if (pagina.size() == 45) { paginas.add(pagina); pagina = new ArrayList<>(); }
            pagina.add(linea);
        }
        if (pagina.isEmpty()) pagina.add("");
        paginas.add(pagina);

        List<byte[]> objetos = new ArrayList<>();
        objetos.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        int paginasObjeto = 2;
        int fuenteObjeto = 3;
        int siguiente = 4;
        List<Integer> pageIds = new ArrayList<>();
        List<Integer> contenidoIds = new ArrayList<>();
        for (List<String> ignored : paginas) {
            pageIds.add(siguiente++);
            contenidoIds.add(siguiente++);
        }
        StringBuilder kids = new StringBuilder();
        for (int id : pageIds) kids.append(id).append(" 0 R ");
        objetos.add(bytes("<< /Type /Pages /Kids [" + kids + "] /Count " + pageIds.size() + " >>"));
        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));
        for (int i = 0; i < paginas.size(); i++) {
            objetos.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 3 0 R >> >> /Contents " + contenidoIds.get(i) + " 0 R >>"));
            String contenido = contenidoPagina(paginas.get(i));
            byte[] contenidoBytes = bytes(contenido);
            objetos.add(bytes("<< /Length " + contenidoBytes.length + " >>\nstream\n" + contenido + "endstream"));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        escribir(out, "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");
        List<Integer> offsets = new ArrayList<>(); offsets.add(0);
        for (int i = 0; i < objetos.size(); i++) {
            offsets.add(out.size());
            escribir(out, (i + 1) + " 0 obj\n"); out.writeBytes(objetos.get(i)); escribir(out, "\nendobj\n");
        }
        int xref = out.size();
        escribir(out, "xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n");
        for (int i = 1; i < offsets.size(); i++) escribir(out, String.format("%010d 00000 n \n", offsets.get(i)));
        escribir(out, "trailer\n<< /Size " + (objetos.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        return out.toByteArray();
    }

    private static String contenidoPagina(List<String> lineas) {
        StringBuilder s = new StringBuilder("BT\n/F1 11 Tf\n72 730 Td\n");
        for (String linea : lineas) { s.append("(").append(pdfEscape(linea)).append(") Tj\n0 -15 Td\n"); }
        return s.append("ET\n").toString();
    }

    private static String pdfEscape(String texto) {
        String normalizado = texto.replaceAll("[^\\x20-\\x7E\\u00A0-\\u00FF]", "?");
        return normalizado.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static String visible(String texto) { return texto == null ? "" : texto; }
    private static byte[] bytes(String texto) { return texto.getBytes(PDF_CHARSET); }
    private static void escribir(ByteArrayOutputStream out, String texto) { out.writeBytes(bytes(texto)); }
}
