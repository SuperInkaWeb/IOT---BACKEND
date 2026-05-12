package com.superinka.ecosensor.backend.servicio;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.superinka.ecosensor.backend.modelo.Alerta;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
	
	private final JavaMailSender mailSender;

	@Value("${app.mail.from}")
	private String fromEmail;


    @Value("${app.mail.from-name:EcoSensor}")
    private String fromName;

    // ── ENVIAR ALERTA POR EMAIL ───────────────────────────────────

    @Async  // no bloquear el hilo principal
    public void enviarAlertaEmail(Alerta alerta, String emailDestino, String nombreUsuario) {
        if (emailDestino == null || emailDestino.isBlank()) return;

        String emoji = switch (alerta.getNivel()) {
            case "CRITICO" -> "🚨";
            case "ALTO"    -> "⚠️";
            default        -> "ℹ️";
        };

        String asunto = emoji + " Alerta EcoSensor: " + alerta.getMensaje();
        String cuerpo = construirHtmlAlerta(alerta, nombreUsuario, emoji);

        enviarEmail(emailDestino, asunto, cuerpo);
    }

    // ── ENVIAR BIENVENIDA ─────────────────────────────────────────

    @Async
    public void enviarBienvenida(String emailDestino, String nombre, String planNombre) {
        String asunto = "¡Bienvenido a EcoSensor! 🌿";
        String cuerpo = construirHtmlBienvenida(nombre, planNombre);
        enviarEmail(emailDestino, asunto, cuerpo);
    }
    // ── ENVIAR CONFIRMACIÓN DE PAGO ───────────────────────────────

    @Async
    public void enviarConfirmacionPago(String emailDestino, String nombre,
                                        String planNombre, String fechaFin) {
        String asunto = "✅ Suscripción activada — Plan " + planNombre;
        String cuerpo = construirHtmlPago(nombre, planNombre, fechaFin);
        enviarEmail(emailDestino, asunto, cuerpo);
    }

    // ── ENVÍO INTERNO ─────────────────────────────────────────────

    private void enviarEmail(String destino, String asunto, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(htmlContent, true); // 'true' indica que es HTML

            mailSender.send(message);
            log.info("Email enviado exitosamente a {}", destino);

        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", destino, e.getMessage());
        }
    }

    // ── TEMPLATES HTML ────────────────────────────────────────────

    private String construirHtmlAlerta(Alerta alerta, String nombre, String emoji) {
        String colorNivel = switch (alerta.getNivel()) {
            case "CRITICO" -> "#ef4444";
            case "ALTO"    -> "#f97316";
            default        -> "#3b82f6";
        };

        String fecha = alerta.getFecha() != null
                ? alerta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Ahora";

        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f1f5f9;font-family:'Segoe UI',sans-serif;">
              <div style="max-width:560px;margin:32px auto;background:white;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                
                <!-- Header -->
                <div style="background:#0f172a;padding:28px 32px;text-align:center;">
                  <h1 style="color:white;margin:0;font-size:22px;font-weight:700;">🌿 EcoSensor</h1>
                  <p style="color:#64748b;margin:6px 0 0;font-size:13px;">Monitoreo inteligente en tiempo real</p>
                </div>
                
                <!-- Alerta -->
                <div style="padding:28px 32px;">
                  <div style="background:%s;border-radius:12px;padding:20px;text-align:center;margin-bottom:24px;">
                    <div style="font-size:36px;margin-bottom:8px;">%s</div>
                    <h2 style="color:white;margin:0;font-size:18px;">%s</h2>
                    <p style="color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:13px;">Nivel: %s</p>
                  </div>
                  
                  <table style="width:100%%;border-collapse:collapse;">
                    <tr>
                      <td style="padding:10px 0;border-bottom:1px solid #f1f5f9;color:#64748b;font-size:13px;">Tipo de métrica</td>
                      <td style="padding:10px 0;border-bottom:1px solid #f1f5f9;color:#0f172a;font-size:13px;font-weight:600;text-align:right;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:10px 0;border-bottom:1px solid #f1f5f9;color:#64748b;font-size:13px;">Valor detectado</td>
                      <td style="padding:10px 0;border-bottom:1px solid #f1f5f9;color:#0f172a;font-size:13px;font-weight:600;text-align:right;">%.2f</td>
                    </tr>
                    <tr>
                      <td style="padding:10px 0;color:#64748b;font-size:13px;">Fecha y hora</td>
                      <td style="padding:10px 0;color:#0f172a;font-size:13px;font-weight:600;text-align:right;">%s</td>
                    </tr>
                  </table>
                  
                  <div style="margin-top:24px;text-align:center;">
                    <a href="http://localhost:4200/home" style="background:#3b82f6;color:white;padding:12px 28px;border-radius:10px;text-decoration:none;font-size:14px;font-weight:600;">Ver dashboard →</a>
                  </div>
                </div>
                
                <!-- Footer -->
                <div style="background:#f8fafc;padding:16px 32px;text-align:center;border-top:1px solid #f1f5f9;">
                  <p style="color:#94a3b8;font-size:12px;margin:0;">EcoSensor · Para dejar de recibir alertas, ajusta tu configuración en el dashboard.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                colorNivel, emoji, alerta.getMensaje(), alerta.getNivel(),
                alerta.getTipoMetrica(),
                alerta.getValor() != null ? alerta.getValor() : 0.0,
                fecha
        );
    }

    private String construirHtmlBienvenida(String nombre, String planNombre) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f1f5f9;font-family:'Segoe UI',sans-serif;">
              <div style="max-width:560px;margin:32px auto;background:white;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                <div style="background:linear-gradient(135deg,#0f172a,#1e3a5f);padding:40px 32px;text-align:center;">
                  <h1 style="color:white;margin:0;font-size:26px;">🌿 ¡Bienvenido a EcoSensor!</h1>
                  <p style="color:#93c5fd;margin:10px 0 0;">Tu entorno ahora está protegido</p>
                </div>
                <div style="padding:32px;">
                  <p style="color:#0f172a;font-size:16px;">Hola <strong>%s</strong>,</p>
                  <p style="color:#475569;font-size:14px;line-height:1.6;">Tu cuenta ha sido configurada exitosamente con el plan <strong>%s</strong>. Ya puedes agregar tus primeros sensores y comenzar el monitoreo en tiempo real.</p>
                  <div style="margin:24px 0;text-align:center;">
                    <a href="http://localhost:4200/sensores/crear" style="background:#3b82f6;color:white;padding:14px 32px;border-radius:10px;text-decoration:none;font-size:14px;font-weight:600;">Agregar mi primer sensor →</a>
                  </div>
                </div>
                <div style="background:#f8fafc;padding:16px 32px;text-align:center;border-top:1px solid #f1f5f9;">
                  <p style="color:#94a3b8;font-size:12px;margin:0;">EcoSensor — Monitoreo IoT inteligente para hogares y empresas</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(nombre, planNombre);
    }

    private String construirHtmlPago(String nombre, String planNombre, String fechaFin) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f1f5f9;font-family:'Segoe UI',sans-serif;">
              <div style="max-width:560px;margin:32px auto;background:white;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                <div style="background:#16a34a;padding:32px;text-align:center;">
                  <div style="font-size:48px;">✅</div>
                  <h1 style="color:white;margin:8px 0 0;font-size:22px;">¡Pago confirmado!</h1>
                </div>
                <div style="padding:32px;">
                  <p style="color:#0f172a;font-size:15px;">Hola <strong>%s</strong>,</p>
                  <p style="color:#475569;font-size:14px;line-height:1.6;">Tu suscripción al plan <strong>%s</strong> ha sido activada exitosamente.</p>
                  <div style="background:#f0fdf4;border:1px solid #bbf7d0;border-radius:10px;padding:16px;margin:20px 0;">
                    <p style="margin:0;color:#166534;font-size:13px;"><strong>Plan:</strong> %s</p>
                    <p style="margin:6px 0 0;color:#166534;font-size:13px;"><strong>Válido hasta:</strong> %s</p>
                  </div>
                  <div style="text-align:center;">
                    <a href="https://ecosensor-inka.netlify.app/home" style="background:#3b82f6;color:white;padding:12px 28px;border-radius:10px;text-decoration:none;font-size:14px;font-weight:600;">Ir al dashboard →</a>
                  </div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(nombre, planNombre, planNombre, fechaFin);
    }
}