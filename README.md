# Sistema de Login com Java, Spring Boot e MySQL

Projeto de uma tela de login feita com **HTML**, **Spring Boot** e **MySQL**. O usuário digita email e senha, o Java confere os dados no banco usando senha criptografada com **BCrypt** e, se estiver tudo certo, abre uma segunda tela.

## Funcionalidades

- Tela de login em HTML
- Validação de email e senha no servidor (Java)
- Senhas armazenadas como **hash BCrypt** (nunca em texto puro)
- Conexão com MySQL via **JDBC**, com `PreparedStatement` (protege contra SQL Injection)
- Sessão de usuário: a tela principal só abre para quem fez login
- Botão **Sair** que encerra a sessão
- Mensagem de erro genérica ("Email ou senha inválidos"), sem revelar qual dos dois estava errado

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 25 | Linguagem |
| Spring Boot 4.1.1 | Servidor web e endpoints |
| Maven | Gerenciador de dependências |
| MySQL | Banco de dados |
| JDBC | Conexão Java ↔ MySQL |
| jBCrypt | Criptografia das senhas |
| HTML + JavaScript | Telas de login e principal |

## Estrutura do projeto

```
demo
├── pom.xml
└── src/main
    ├── java/com/example/demo
    │   ├── DemoApplication.java    # Inicia o servidor
    │   ├── LoginController.java    # Endpoints: /login, /usuario, /logout
    │   ├── SenhaUtil.java          # Criptografa e confere senhas (BCrypt)
    │   ├── UsuarioDAO.java         # Acesso ao banco de dados
    │   └── GerarHash.java          # Utilitário para gerar o INSERT de um usuário
    └── resources/static
        ├── criptografia.html       # Tela 1: login
        └── home.html               # Tela 2: página após o login
```

## Como funciona

1. A tela de login envia `email` e `senha` para `POST /login`.
2. O `LoginController` busca no banco o hash salvo para aquele email (`UsuarioDAO`).
3. O `SenhaUtil` compara a senha digitada com o hash usando `BCrypt.checkpw`.
4. Se for válido, o email é guardado na **sessão** e a tela redireciona para `home.html`.
5. A `home.html` chama `GET /usuario` para saber quem está logado. Se não houver login, volta para a tela de login.

### Endpoints

| Método | Endereço | O que faz |
|---|---|---|
| `POST` | `/login` | Recebe `email` e `senha` e valida o login |
| `GET` | `/usuario` | Devolve o email de quem está logado (ou 401) |
| `POST` | `/logout` | Encerra a sessão |

## Como executar

### Pré-requisitos

- JDK 25 (ou ajuste `java.version` no `pom.xml` para a sua versão)
- MySQL em execução na porta 3306
- VS Code com *Extension Pack for Java* e *Spring Boot Extension Pack* (ou IntelliJ)

### 1. Criar o banco e a tabela

Execute no MySQL:

```sql
CREATE DATABASE IF NOT EXISTS login_db;
USE login_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(150) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL
);
```

### 2. Criar um usuário de teste

Como o projeto não tem tela de cadastro, o usuário é inserido direto no banco, com a senha já em hash BCrypt.

**Opção A:** usar um hash pronto (apenas para testes). A senha deste exemplo é `minhaSenha123`:

```sql
INSERT INTO usuarios (email, senha_hash)
VALUES ('teste@exemplo.com', '$2a$10$wye8n97h/GmmV6hHkG8t6uWnAdRKb9sln7Gc8c2wh47NuaEAdquqm');
```

**Opção B:** gerar o seu próprio. Abra o `GerarHash.java`, troque o email e a senha, rode o `main` e execute no MySQL o `INSERT` que aparecer no console.

### 3. Configurar a conexão

No arquivo `UsuarioDAO.java`, ajuste os dados do seu MySQL:

```java
private static final String URL  = "jdbc:mysql://localhost:3306/login_db";
private static final String USER = "root";
private static final String PASS = "sua_senha_do_mysql";
```

> **Importante:** não envie a sua senha real do banco para o GitHub. Use uma variável de ambiente ou um arquivo de configuração ignorado pelo Git.

### 4. Rodar o projeto

1. Abra a pasta do projeto no VS Code e espere o Maven baixar as dependências.
2. Execute o `DemoApplication.java` (botão **Run** acima do `main`).
3. Aguarde a mensagem `Started DemoApplication` no terminal.
4. Acesse no navegador: **http://localhost:8080/criptografia.html**

### 5. Testar

| Teste | Email | Senha | Resultado esperado |
|---|---|---|---|
| Login correto | `teste@exemplo.com` | `minhaSenha123` | Abre a tela de boas-vindas |
| Senha errada | `teste@exemplo.com` | qualquer outra | "Email ou senha inválidos" |
| Email inexistente | `outro@email.com` | qualquer | "Email ou senha inválidos" |

A senha diferencia maiúsculas de minúsculas.

## Problemas comuns

| Erro | Causa e solução |
|---|---|
| `Port 8080 was already in use` | Outro programa (ou uma execução antiga) usa a porta. Pare o outro processo ou adicione `server.port=8081` em `src/main/resources/application.properties`. |
| `Access denied for user 'root'` | Senha do MySQL incorreta no `UsuarioDAO.java`. |
| `Unknown database 'login_db'` | O banco não foi criado (passo 1). |
| `Unable to connect to 127.0.0.1:3306` | O MySQL está desligado. Inicie o serviço. |
| `CLIENT_PLUGIN_AUTH is required` | Servidor MySQL muito antigo para o driver atual. Troque o driver no `pom.xml` por `mysql:mysql-connector-java:5.1.49` e use `?useSSL=false` na URL. |
| Login recusado com a senha certa | Confira se o email existe na tabela e se `senha_hash` tem 60 caracteres. |

## Segurança

- As senhas são guardadas apenas como **hash BCrypt**, que inclui um *salt* aleatório. O mesmo texto gera hashes diferentes a cada vez, por isso a comparação usa `BCrypt.checkpw` e não `equals`.
- Todas as consultas usam `PreparedStatement`.
- Em produção, use **HTTPS**, para que a senha não trafegue em texto puro pela rede.

## Possíveis melhorias

- Tela de cadastro de usuários
- Pool de conexões (HikariCP) e Spring Data JPA
- Spring Security para proteger rotas e gerenciar sessões
- Limite de tentativas de login
- Credenciais do banco em variáveis de ambiente

## Autor

Guilherme Felberque Eucaristico
