# Sistema de Agendamento para Barbearia

API REST desenvolvida para gerenciar o funcionamento de uma barbearia.

O sistema permite cadastrar usuários, clientes, barbeiros e serviços, além de realizar e controlar agendamentos.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Swagger/OpenAPI
- JUnit
- Mockito
- Maven

## Integrantes e responsabilidades

- **João Victor Pereira** — desenvolvimento do módulo de Barbeiro.
- **Breno Clemente** — desenvolvimento do módulo de Usuário.
- **Moacyr Filipe Pereira** — desenvolvimento do módulo de Cliente.
- **Mateus Alves Costa** — desenvolvimento do módulo de Serviço.
- **Juan Pablo Rocha Hempel** — desenvolvimento do módulo de Agendamento, autenticação, segurança com JWT, tratamento de exceções e configuração do Swagger.

## Funcionalidades principais

- Cadastro e gerenciamento de clientes.
- Cadastro e gerenciamento de barbeiros.
- Cadastro e gerenciamento de serviços.
- Criação e controle de agendamentos.
- Autenticação de usuários.
- Controle de acesso por perfil.
- Proteção dos endpoints com JWT.
- Documentação da API com Swagger.
- Controle do banco de dados com migrations do Flyway.

## Documentação da API

Com a aplicação em execução, a documentação pode ser acessada pelo Swagger:

```text
http://localhost:8080/swagger-ui/index.html