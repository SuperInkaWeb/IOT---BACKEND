package com.superinka.ecosensor.backend.servicio;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.superinka.ecosensor.backend.dto.DashboardResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ReporteService {

    // Colores de marca EcoSensor
    private static final Color COLOR_PRIMARIO   = new Color(59, 130, 246);   // azul
    private static final Color COLOR_VERDE      = new Color(34, 197, 94);    // verde
    private static final Color COLOR_ROJO       = new Color(248, 113, 113);  // rojo
    private static final Color COLOR_AMARILLO   = new Color(250, 204, 21);   // amarillo
    private static final Color COLOR_OSCURO     = new Color(15, 23, 42);     // fondo oscuro
    private static final Color COLOR_GRIS       = new Color(100, 116, 139);  // gris texto
    private static final Color COLOR_FONDO_CARD = new Color(30, 41, 59);     // fondo card

    public byte[] generarReporteEmpresa(DashboardResponse data, String nombreEmpresa) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 60, 40);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            // ── ENCABEZADO ────────────────────────────────────────────
            agregarEncabezado(doc, writer, nombreEmpresa);

            // ── RESUMEN EJECUTIVO ─────────────────────────────────────
            agregarResumenEjecutivo(doc, data);

            // ── MÉTRICAS DETALLADAS ───────────────────────────────────
            doc.add(Chunk.NEWLINE);
            agregarTituloSeccion(doc, "MÉTRICAS AMBIENTALES DETALLADAS");
            agregarMetricaCard(doc, "🌫  Calidad del Aire — PM2.5",
                    String.format("%.2f µg/m³", data.getPromedioPM25()),
                    getEstadoAire(data.getPromedioPM25()),
                    getColorMetrica(data.getPromedioPM25(), 50, 150));

            agregarMetricaCard(doc, "💨  Dióxido de Carbono — CO2",
                    String.format("%.0f ppm", data.getPromedioCO2()),
                    getEstadoCO2(data.getPromedioCO2()),
                    getColorMetrica(data.getPromedioCO2(), 700, 1000));

            agregarMetricaCard(doc, "💧  Calidad del Agua — pH",
                    String.format("%.2f", data.getPromedioPH()),
                    getEstadoPH(data.getPromedioPH()),
                    getColorPH(data.getPromedioPH()));

            agregarMetricaCard(doc, "⚡  Consumo Energético",
                    String.format("%.2f kWh", data.getConsumoEnergia()),
                    getEstadoEnergia(data.getConsumoEnergia()),
                    getColorMetrica(data.getConsumoEnergia(), 5, 10));

            // ── KPIs OPERACIONALES ────────────────────────────────────
            doc.add(Chunk.NEWLINE);
            agregarTituloSeccion(doc, "KPIs OPERACIONALES");
            agregarKPIs(doc, data);

            // ── ANÁLISIS IA ───────────────────────────────────────────
            doc.add(Chunk.NEWLINE);
            agregarTituloSeccion(doc, "ANÁLISIS INTELIGENTE (IA/ML)");
            agregarAnalisisIA(doc, data);

            // ── RECOMENDACIONES ───────────────────────────────────────
            doc.add(Chunk.NEWLINE);
            agregarTituloSeccion(doc, "CONCLUSIÓN Y RECOMENDACIONES");
            agregarRecomendaciones(doc, data);

            // ── PIE DE PÁGINA ─────────────────────────────────────────
            agregarPiePagina(doc, writer);

            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    // ── MÉTODOS PRIVADOS ──────────────────────────────────────────

    private void agregarEncabezado(Document doc, PdfWriter writer, String nombreEmpresa)
            throws DocumentException {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // barra azul superior
        PdfContentByte canvas = writer.getDirectContentUnder();
        canvas.setColorFill(COLOR_PRIMARIO);
        canvas.rectangle(40, doc.getPageSize().getHeight() - 80, doc.getPageSize().getWidth() - 80, 50);
        canvas.fill();

        Font fontLogo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE);
        Font fontSub  = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.WHITE);

        Paragraph logoParr = new Paragraph();
        logoParr.add(new Chunk("EcoSensor  ", fontLogo));
        logoParr.add(new Chunk("Reporte Ambiental Inteligente", fontSub));
        logoParr.setSpacingBefore(10);
        doc.add(logoParr);

        Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_OSCURO);
        Font fontFecha   = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_GRIS);

        doc.add(new Paragraph("Empresa: " + nombreEmpresa, fontEmpresa));
        doc.add(new Paragraph("Generado: " + LocalDateTime.now().format(fmt), fontFecha));
        doc.add(new LineSeparator(1f, 100f, COLOR_PRIMARIO, Element.ALIGN_CENTER, -2));
        doc.add(Chunk.NEWLINE);
    }

    private void agregarResumenEjecutivo(Document doc, DashboardResponse data)
            throws DocumentException {

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_OSCURO);
        Font valor  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, getColorEstado(data.getEstadoGeneral()));
        Font sub    = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_GRIS);

        doc.add(new Paragraph("RESUMEN EJECUTIVO", titulo));
        doc.add(Chunk.NEWLINE);

        // Eco-Score grande y destacado
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(5);

        // Celda Eco-Score
        PdfPCell celdaScore = new PdfPCell();
        celdaScore.setBorderColor(getColorEstado(data.getEstadoGeneral()));
        celdaScore.setPadding(12);
        celdaScore.setBorderWidth(2);
        Paragraph pScore = new Paragraph();
        pScore.add(new Chunk(String.valueOf(data.getEcoScore() != null ? data.getEcoScore() : 0), valor));
        pScore.add(new Chunk("/100\n", sub));
        pScore.add(new Chunk("Eco-Score", FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_GRIS)));
        celdaScore.addElement(pScore);
        tabla.addCell(celdaScore);

        // Celda Estado
        PdfPCell celdaEstado = new PdfPCell();
        celdaEstado.setPadding(12);
        celdaEstado.setBorderColor(new Color(226, 232, 240));
        Paragraph pEstado = new Paragraph();
        pEstado.add(new Chunk(data.getEstadoGeneral() != null ? data.getEstadoGeneral() : "BUENO",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, getColorEstado(data.getEstadoGeneral()))));
        pEstado.add(new Chunk("\nEstado General", FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_GRIS)));
        celdaEstado.addElement(pEstado);
        tabla.addCell(celdaEstado);

        // Celda Anomalías
        PdfPCell celdaAnomalias = new PdfPCell();
        celdaAnomalias.setPadding(12);
        celdaAnomalias.setBorderColor(new Color(226, 232, 240));
        Paragraph pAno = new Paragraph();
        long anomalias = data.getAnomaliasDetectadas() != null ? data.getAnomaliasDetectadas() : 0;
        pAno.add(new Chunk(String.valueOf(anomalias),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22,
                        anomalias > 0 ? COLOR_ROJO : COLOR_VERDE)));
        pAno.add(new Chunk("\nAnomalías (IA)", FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_GRIS)));
        celdaAnomalias.addElement(pAno);
        tabla.addCell(celdaAnomalias);

        doc.add(tabla);
    }

    private void agregarTituloSeccion(Document doc, String titulo) throws DocumentException {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARIO);
        Paragraph p = new Paragraph(titulo, f);
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        doc.add(p);
        doc.add(new LineSeparator(0.5f, 100f, COLOR_PRIMARIO, Element.ALIGN_CENTER, -1));
        doc.add(Chunk.NEWLINE);
    }

    private void agregarMetricaCard(Document doc, String nombre, String valor,
                                     String estado, Color color) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{3f, 1.5f, 1.5f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(4);
        tabla.setSpacingAfter(4);

        // Nombre
        PdfPCell cNombre = new PdfPCell(new Phrase(nombre,
                FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_OSCURO)));
        cNombre.setPadding(8);
        cNombre.setBorderColor(new Color(226, 232, 240));
        cNombre.setBackgroundColor(new Color(248, 250, 252));
        tabla.addCell(cNombre);

        // Valor
        PdfPCell cValor = new PdfPCell(new Phrase(valor,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, color)));
        cValor.setPadding(8);
        cValor.setHorizontalAlignment(Element.ALIGN_CENTER);
        cValor.setBorderColor(new Color(226, 232, 240));
        tabla.addCell(cValor);

        // Estado
        PdfPCell cEstado = new PdfPCell(new Phrase(estado,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, color)));
        cEstado.setPadding(8);
        cEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
        cEstado.setBorderColor(new Color(226, 232, 240));
        tabla.addCell(cEstado);

        doc.add(tabla);
    }

    private void agregarKPIs(Document doc, DashboardResponse data) throws DocumentException {
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(5);

        agregarCeldaKPI(tabla, "Sensores totales",
                String.valueOf(data.getTotalSensores() != null ? data.getTotalSensores() : 0), COLOR_PRIMARIO);
        agregarCeldaKPI(tabla, "Sensores activos",
                String.valueOf(data.getSensoresActivos() != null ? data.getSensoresActivos() : 0), COLOR_VERDE);
        agregarCeldaKPI(tabla, "Alertas hoy",
                String.valueOf(data.getAlertasHoy() != null ? data.getAlertasHoy() : 0), COLOR_AMARILLO);
        agregarCeldaKPI(tabla, "Alertas críticas",
                String.valueOf(data.getAlertasCriticas() != null ? data.getAlertasCriticas() : 0), COLOR_ROJO);

        doc.add(tabla);
    }

    private void agregarCeldaKPI(PdfPTable tabla, String label, String valor, Color color) {
        PdfPCell celda = new PdfPCell();
        celda.setPadding(10);
        celda.setBorderColor(color);
        celda.setBorderWidth(1.5f);
        Paragraph p = new Paragraph();
        p.add(new Chunk(valor + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, color)));
        p.add(new Chunk(label, FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_GRIS)));
        celda.addElement(p);
        tabla.addCell(celda);
    }

    private void agregarAnalisisIA(Document doc, DashboardResponse data) throws DocumentException {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_OSCURO);
        long anomalias = data.getAnomaliasDetectadas() != null ? data.getAnomaliasDetectadas() : 0;

        String texto = anomalias > 0
                ? String.format("El sistema de IA detectó %d anomalía(s) en el período analizado. " +
                  "Se recomienda revisar los sensores con mayor variabilidad.", anomalias)
                : "El sistema de IA no detectó anomalías en este período. " +
                  "Todos los sensores operan dentro de los rangos esperados.";

        PdfPCell celda = new PdfPCell(new Phrase(texto, f));
        celda.setPadding(12);
        celda.setBorderColor(anomalias > 0 ? COLOR_AMARILLO : COLOR_VERDE);
        celda.setBorderWidth(1.5f);
        celda.setBackgroundColor(anomalias > 0
                ? new Color(254, 252, 232)
                : new Color(240, 253, 244));

        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        tabla.addCell(celda);
        doc.add(tabla);
    }

    private void agregarRecomendaciones(Document doc, DashboardResponse data) throws DocumentException {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_OSCURO);
        Integer score = data.getEcoScore();
        String texto  = generarRecomendacion(score);
        Color color   = score == null ? COLOR_GRIS : score >= 85 ? COLOR_VERDE : score >= 60 ? COLOR_AMARILLO : COLOR_ROJO;
        String icono  = score == null ? "ℹ" : score >= 85 ? "✓" : score >= 60 ? "⚠" : "✗";

        PdfPCell celda = new PdfPCell(new Phrase(icono + "  " + texto, f));
        celda.setPadding(14);
        celda.setBorderColor(color);
        celda.setBorderWidth(2f);

        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        tabla.addCell(celda);
        doc.add(tabla);
    }

    private void agregarPiePagina(Document doc, PdfWriter writer) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.setColorFill(COLOR_PRIMARIO);
        canvas.rectangle(40, 30, doc.getPageSize().getWidth() - 80, 2);
        canvas.fill();

        Font fPie = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_GRIS);
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("EcoSensor · Monitoreo IoT Inteligente · Documento confidencial", fPie),
                doc.getPageSize().getWidth() / 2, 20, 0);
    }

    // ── HELPERS DE ESTADO ─────────────────────────────────────────

    private String getEstadoAire(Double v)    { if (v == null) return "Sin datos"; return v < 50 ? "Buena" : v < 150 ? "Moderada" : "Peligrosa"; }
    private String getEstadoCO2(Double v)     { if (v == null) return "Sin datos"; return v < 700 ? "Normal" : v < 1000 ? "Elevado" : "¡Alto!"; }
    private String getEstadoPH(Double v)      { if (v == null) return "Sin datos"; return (v >= 6.5 && v <= 8.5) ? "Potable" : "No potable"; }
    private String getEstadoEnergia(Double v) { if (v == null) return "Sin datos"; return v < 5 ? "Normal" : v < 10 ? "Alto" : "¡Crítico!"; }

    private Color getColorMetrica(Double v, double umbralMedio, double umbralAlto) {
        if (v == null) return COLOR_GRIS;
        if (v < umbralMedio) return COLOR_VERDE;
        if (v < umbralAlto)  return COLOR_AMARILLO;
        return COLOR_ROJO;
    }

    private Color getColorPH(Double v) {
        if (v == null) return COLOR_GRIS;
        return (v >= 6.5 && v <= 8.5) ? COLOR_VERDE : COLOR_ROJO;
    }

    private Color getColorEstado(String estado) {
        if (estado == null) return COLOR_GRIS;
        return switch (estado) {
            case "CRITICO" -> COLOR_ROJO;
            case "MEDIO"   -> COLOR_AMARILLO;
            default        -> COLOR_VERDE;
        };
    }

    private String generarRecomendacion(Integer score) {
        if (score == null) return "No hay datos suficientes para generar una recomendación.";
        if (score >= 85) return "ESTADO ÓPTIMO: Todos los parámetros ambientales están dentro de los rangos saludables. No se requieren acciones correctivas.";
        if (score >= 60) return "ESTADO PREVENTIVO: Se recomienda revisar los sensores con mayor variabilidad y optimizar el consumo eléctrico en las próximas 48 horas.";
        return "ESTADO CRÍTICO: Se requiere inspección técnica inmediata. Revisar filtros de aire, calidad del agua y sobrecargas eléctricas en las zonas con anomalías detectadas.";
    }
}