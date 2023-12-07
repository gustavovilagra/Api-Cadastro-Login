package org.villagra.webapp.repositories;

import org.villagra.webapp.configuration.DatabaseConfig;
import org.villagra.webapp.model.Usuario;


import java.sql.*;


public class UserRepository {
    public Usuario obtenerUsuarioPorEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        Usuario usuario = null;

        try {
            Connection connection = DatabaseConfig.getInstance().getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Si se encuentra un usuario en la base de datos, crea un objeto Usuario y devuélvelo
                        String nombre = resultSet.getString("nombre");
                        String clave = resultSet.getString("password");
                        String salt = resultSet.getString("salt");
                        String token = resultSet.getString("token");
                        usuario = new Usuario(nombre, email, clave, salt, token);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en la base de datos: " + e.getMessage());
        }

        return usuario; // Devuelve el usuario encontrado o null si no existe
    }
    // Método para registrar un usuario en la base de datos
    public static void registrarUsuario(Usuario usuario) {
        try (Connection connection = DatabaseConfig.getInstance().getConnection()) {
            String sql = "INSERT INTO usuario (nombre, email, password,salt) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, usuario.getNome());
            preparedStatement.setString(2, usuario.getEmail());
            preparedStatement.setString(3, usuario.getPassword());
            preparedStatement.setString(4,usuario.getSalt() );

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("ocurrio un error en registrar ="+e.getMessage());
        }
    }
    public void registrarToken(Usuario usuario) {
        try (Connection connection = DatabaseConfig.getInstance().getConnection();) {
            String sql = "UPDATE usuario SET token = ? WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, usuario.getToken());
            preparedStatement.setString(2, usuario.getEmail());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 1) {
                System.out.println("Token registrado con éxito para el usuario: " + usuario.getEmail());
            } else {
                System.out.println("No se pudo registrar el token para el usuario: " + usuario.getEmail());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ocurrió un error al registrar el token: " + e.getMessage());
        }
    }

    public String findTokenByEmail(String email) {
        String token = null;
        try (Connection connection = DatabaseConfig.getInstance().getConnection();) {
            String sql = "SELECT token FROM usuario WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                token = resultSet.getString("token");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ocurrió un error al buscar el token por email: " + e.getMessage());
        }

        return token;
    }
}
