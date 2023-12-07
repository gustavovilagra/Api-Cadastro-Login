package org.villagra.webapp.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;

@WebServlet("/ingreso")
public class IngresoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Obtén el archivo HTML de ingreso como recurso del proyecto
        String filePath = "Ingreso.html";


        try {
            InputStream inputStream = getServletContext().getResourceAsStream(filePath);
            if (inputStream != null) {
                byte[] fileBytes = inputStream.readAllBytes();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().write(fileBytes);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IOException e) {
            // Maneja los errores aquí
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("Error de E/S: " + e.getMessage());
        }
    }
}
