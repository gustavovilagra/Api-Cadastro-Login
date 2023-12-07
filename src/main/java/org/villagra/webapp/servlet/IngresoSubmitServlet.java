package org.villagra.webapp.servlet;


import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.villagra.webapp.model.Usuario;
import org.villagra.webapp.repositories.UserRepository;
import org.villagra.webapp.security.PasswordHashing;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

@WebServlet("/submitI")
public class IngresoSubmitServlet extends HttpServlet {
    UserRepository userRepository = new UserRepository();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5501");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Email");

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            // Eliminar comillas alrededor del token que el cliente envió
            authorizationHeader = authorizationHeader.replace("\"", "");
            System.out.println("authorizationHeader = " + authorizationHeader);
        }

        String emailHeader = request.getHeader("Email");
        if (emailHeader != null) {
            // Eliminar comillas alrededor del correo electrónico que el cliente envió
            emailHeader = emailHeader.replace("\"", "");
            System.out.println("emailHeader = " + emailHeader);
        }

        Usuario usuarioExistente = userRepository.obtenerUsuarioPorEmail(emailHeader);
        System.out.println("usuarioExistente = " + usuarioExistente.getToken());

        if (authorizationHeader != null && emailHeader != null) {
            String tokenRecibido = authorizationHeader.trim();
            String emailRecibido = emailHeader;
            System.out.println("emailRecibido = " + emailRecibido);
            System.out.println("tokenRecibido = " + tokenRecibido);

            String tokens = userRepository.findTokenByEmail(emailRecibido);

            if (tokenRecibido.equals(tokens)) {
                // El token y el correo electrónico son válidos
                response.setStatus(HttpServletResponse.SC_OK);

                enviarTokenAlSegundoServidor(tokens);

            } else {
                // El token o el correo electrónico no son válidos
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            // No se proporcionaron el token o el correo electrónico en la solicitud
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }


    }

    // Método para enviar el token al segundo servidor
    public static void enviarTokenAlSegundoServidor(String token) {

        String segundoServidorUrl = "http://localhost:8081/webRegistrosApp/destination";

        String admin = "Gust@vo";
        String password = "MiDi@Vo$x1989!!";

        try {
            URL url = new URL(segundoServidorUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // conexión para una solicitud POST.
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            //  credenciales al encabezado de autorización
            String credenciales = admin + ":" + password;
            String credencialesCodificadas = java.util.Base64.getEncoder().encodeToString(credenciales.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + credencialesCodificadas);

            // Agrega el token al cuerpo de la solicitud
            String requestBody = token;
            System.out.println("Token generado por el servidor1 "+requestBody);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {

                System.out.println("Token aceptado por el segundo servidor.");

            } else {
                // El segundo servidor respondió con un código de estado que indica un problema.
                System.out.println("El segundo servidor respondió con un código de estado: " + responseCode);
            }

            //respuesta del segundo servidor
            try (InputStream is = connection.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String response = reader.lines().reduce("", (accumulator, line) -> accumulator + line);
                System.out.println("Respuesta del segundo servidor: " + response);
            }

            connection.disconnect();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    protected  void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Agregar los encabezados necesarios para permitir las solicitudes CORS
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5501"); // Cambia el valor al dominio apropiado
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization ,Email");

        // Indicar el tiempo de vida (en segundos) del pre-vuelo (pre-flight) CORS
        resp.setHeader("Access-Control-Max-Age", "3600"); // Por ejemplo, 1 hora

        // Establecer el estado de la respuesta
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {

            StringBuilder requestBody = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            Gson gson = new Gson();
            Usuario datosLogin = gson.fromJson(requestBody.toString(), Usuario.class);
            String email = datosLogin.getEmail();
            String clave = datosLogin.getPassword();

            System.out.println("email del header = " + email);
            System.out.println("password del header = " + clave);

            PasswordHashing passwordHashing = new PasswordHashing();
            Usuario usuarioExistente = userRepository.obtenerUsuarioPorEmail(email);



            if (usuarioExistente != null) {
                System.out.println("usuarioExistente = " + usuarioExistente.getEmail());
                String storedPassword = usuarioExistente.getPassword();
                String salt = usuarioExistente.getSalt();

                // Verificar contraseña
                boolean isValid = PasswordHashing.verifyPassword(clave, storedPassword, salt);
                System.out.println("valor de isValid = " +isValid);

                if (isValid) {
                    String tokens = userRepository.findTokenByEmail(email);
                    System.out.println("valor de tokens = " + tokens);

                    if (tokens == null) {
                        long expirationMillis = 120000; // Tiempo de expiración en milisegundos (2 minutos)
                        String token = generateToken(email, expirationMillis);

                        usuarioExistente.setToken(token);
                        userRepository.registrarToken(usuarioExistente);

                        // Crear una sesión para el usuario
                        HttpSession session = request.getSession();
                        session.setAttribute("email", email);
                        session.setAttribute("token", token);

                        // Crear cookies
                        Cookie tokenCookie = new Cookie("token", token);
                        Cookie emailCookie = new Cookie("email", email);

                        // Configurar propiedades de las cookies
                        tokenCookie.setMaxAge(120); // Tiempo de vida de la cookie en segundos
                        emailCookie.setMaxAge(120);
                        tokenCookie.setPath("/");
                        emailCookie.setPath("/");

                        // Agregar las cookies a la respuesta
                        response.addCookie(tokenCookie);
                        response.addCookie(emailCookie);

                    }else{
                        // Crear cookies
                        Cookie tokenCookie = new Cookie("token", tokens);
                        Cookie emailCookie = new Cookie("email", email);

                        // Configurar propiedades de las cookies
                        tokenCookie.setMaxAge(120);
                        emailCookie.setMaxAge(120);
                        tokenCookie.setPath("/");
                        emailCookie.setPath("/");

                        // Agregar las cookies a la respuesta
                        response.addCookie(tokenCookie);
                        response.addCookie(emailCookie);
                    }

                    //response.sendRedirect("http://localhost:5501/principal.html");
                    response.getWriter().write("usuario_existe");
                    response.setStatus(HttpServletResponse.SC_OK); // Esto enviará un código de estado 20

                } else {
                    response.getWriter().write("error");
                    response.setStatus(HttpServletResponse.SC_OK); // Esto enviará un código de estado 20
                }
            } else {
                // El usuario no existe
                //response.sendRedirect("/webapp/registro");
                response.getWriter().write("usuario_no_existente");
                response.setStatus(HttpServletResponse.SC_OK);
            }

        } catch (Exception ex) {
            System.out.println("error = " + ex.getMessage());
        }
    }
    public static String generateToken(String subject, long expirationMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        String token = Base64.getEncoder().encodeToString((subject + expiration.getTime()).getBytes());
        // Firmar el token con la clave secreta
        token = token + "." + signToken(token);

        return token;
    }

    private static String signToken(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] signature = md.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al firmar el token.", e);
        }
    }
}
