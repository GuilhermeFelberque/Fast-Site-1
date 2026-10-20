package com.example.demo;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recebe o que vem das telas e responde.
 * Endereços: POST /login, GET /usuario, POST /logout
 */
@RestController // diz ao Spring que esta classe responde requisições das telas
public class LoginController {

    private final UsuarioDAO dao = new UsuarioDAO();

    // A tela de login chama POST /login enviando "email" e "senha"
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email,
                                        @RequestParam String senha,
                                        HttpSession session) {

        String emailLimpo = email.trim().toLowerCase();

        // 1) Busca no banco o hash salvo para esse email
        String hashSalvo = dao.buscarHashPorEmail(emailLimpo);

        // 2) Compara a senha digitada com o hash
        if (SenhaUtil.conferir(senha, hashSalvo)) {
            // Login certo: guarda o email na SESSÃO (é o que "lembra" que a pessoa está logada)
            session.setAttribute("usuarioLogado", emailLimpo);
            return ResponseEntity.ok("Login realizado!");
        }

        // Mensagem genérica: não revela se o erro foi no email ou na senha
        return ResponseEntity.status(401).body("Email ou senha inválidos");
    }

    // A tela principal chama GET /usuario para saber QUEM está logado
    // Se ninguém estiver logado, responde 401 e a tela manda de volta para o login
    @GetMapping("/usuario")
    public ResponseEntity<String> usuarioLogado(HttpSession session) {
        Object email = session.getAttribute("usuarioLogado");

        if (email == null) {
            return ResponseEntity.status(401).body("Não logado");
        }
        return ResponseEntity.ok(email.toString());
    }

    // O botão "Sair" chama POST /logout: apaga a sessão
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Saiu");
    }
}
