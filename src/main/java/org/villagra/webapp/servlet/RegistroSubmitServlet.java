package org.villagra.webapp.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.villagra.webapp.model.Usuario;
import org.villagra.webapp.repositories.UserRepository;
import org.villagra.webapp.security.PasswordHashing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/submitR")
public class RegistroSubmitServlet extends HttpServlet {
    private final PasswordHashing passwordHashing = new PasswordHashing();
    UserRepository userRepository = new UserRepository();
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
           // if ("POST".equals(request.getMethod())) {
                // Procesar la solicitud POST para el envío del formulario
                BufferedReader reader = request.getReader();

                // Parsear los datos del formulario
                StringBuilder formData = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    formData.append(line);
                }

                // Parsear los parámetros del formulario
                Map<String, String> parameters = parseFormData(formData.toString());

                // Obtener los valores de los parámetros
                String nombre = parameters.get("nombre");
                String email = parameters.get("email");
                String clave = parameters.get("password");
                String confirmacionClave = parameters.get("confirm_password");
                String date = parameters.get("fecha_nacimiento");
                String direccion = parameters.get("direccion");

                if (nombre != null && email != null && clave != null && confirmacionClave != null && clave.equals(confirmacionClave)) {
                    Usuario usuarioExistente = userRepository.obtenerUsuarioPorEmail(email);

                    if (usuarioExistente != null) {
                        response.setContentType("text/html");
                        PrintWriter out = response.getWriter();

                        out.println("<!DOCTYPE html>");
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<meta charset=\"UTF-8\">");
                        out.println("<title>Registro</title>");
                        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/styles.css\">");
                        out.println( "<script src=\"javaScript/validation.js\"></script>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>¡Registro ya existente!</h1>");
                        out.println("<p>Ingrese a través del link:</p>");
                        out.println("<p><a href=\"/webapp/ingreso\">Iniciar Sesión</a></p>");
                        out.println("</body>");
                        out.println("</html>");
                    } else {
                        // El usuario no existe, registrar al usuario y mostrar un mensaje de éxito
                        String salt = PasswordHashing.generateSalt();
                        String hashedPassword = PasswordHashing.generateHash(clave, salt);
                        Usuario nuevoUsuario = new Usuario(nombre, email, hashedPassword, salt);
                        UserRepository.registrarUsuario(nuevoUsuario);

                        String redirectLocation = "/webapp/ingreso";
                        sendRedirectResponse(response, redirectLocation);
                    }

                } else {
                    // Si alguno de los campos es nulo o las contraseñas no coinciden, mostrar un mensaje de error
                    String errorMessage = "Por favor, completa todos los campos del formulario correctamente.";
                    sendErrorResponse(response, errorMessage);
                }

        } catch (Exception ex) {
            System.out.println("error " + ex.getMessage());
        }
    }

    // Función para analizar los datos del formulario
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> parameters = new HashMap<>();
        String[] pairs = formData.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                // Decodificar los valores URL (puede ser necesario, dependiendo de los datos)
                try {
                    key = URLDecoder.decode(key, StandardCharsets.UTF_8.toString());
                    value = URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                parameters.put(key, value);
            }
        }
        return parameters;
    }

    // Función para enviar una respuesta HTTP de redirección
    private void sendRedirectResponse(HttpServletResponse response, String redirectLocation) {
        response.setStatus(HttpServletResponse.SC_FOUND); // Código de respuesta 302 para redirección
        response.setHeader("Location", redirectLocation);
    }

    // Función para enviar una respuesta HTTP de error
    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Cambiado a 400 para indicar un error de cliente
        response.getWriter().write(errorMessage);
    }
}

