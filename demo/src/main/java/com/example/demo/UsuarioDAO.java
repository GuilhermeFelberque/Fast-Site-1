package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Classe responsável SOMENTE por conversar com o banco de dados.
 */
public class UsuarioDAO {

    // Endereço do banco: jdbc:mysql://servidor:porta/nome_do_banco
    private static final String URL = "jdbc:mysql://localhost:3306/login_db?useSSL=false";

    // Usuário e senha DO MYSQL (troque pela senha real do seu MySQL!)
    private static final String USER = "root";
    private static final String PASS = "";

    /**
     * Busca o hash da senha de um usuário pelo email.
     *
     * @return o hash salvo, ou null se o email não existir (ou se der erro)
     */
    public String buscarHashPorEmail(String email) {
        // O "?" é preenchido depois com o email. Isso evita SQL Injection.
        String sql = "SELECT senha_hash FROM usuarios WHERE email = ?";

        // try-with-resources: fecha conexão e statement automaticamente no final
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email); // preenche o "?"

            try (ResultSet rs = ps.executeQuery()) {
                // rs.next() é true se encontrou o email
                if (rs.next()) {
                    return rs.getString("senha_hash");
                }
            }

        } catch (SQLException e) {
            // Mostra o erro no console (ex.: senha do MySQL errada, banco desligado)
            e.printStackTrace();
        }

        return null; // email não encontrado ou erro
    }
}
