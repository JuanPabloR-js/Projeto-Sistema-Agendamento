# Barbearia API — Sistema de Agendamento

API REST desenvolvida em Java com Spring Boot para apoiar o gerenciamento de uma barbearia. A aplicação reúne operações de cadastro e consulta de clientes, barbeiros e serviços, além do fluxo de criação, acompanhamento e atualização de agendamentos.

O projeto foi desenvolvido como atividade acadêmica em equipe e utiliza autenticação baseada em JWT, controle de acesso por perfil, persistência em PostgreSQL e versionamento do banco de dados com Flyway.

## Sumário

- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Arquitetura e organização](#arquitetura-e-organização)
- [Perfis e permissões](#perfis-e-permissões)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do ambiente](#configuração-do-ambiente)
- [Execução](#execução)
- [Documentação Swagger](#documentação-swagger)
- [Autenticação](#autenticação)
- [Endpoints](#endpoints)
- [Regras de agendamento](#regras-de-agendamento)
- [Banco de dados e migrations](#banco-de-dados-e-migrations)
- [Testes](#testes)
- [Equipe](#equipe)
- [Observações](#observações)

## Funcionalidades

### Usuários e autenticação
- Cadastro público de contas com perfil `CLIENTE`.
- Login com e-mail e senha.
- Geração e validação de token JWT.
- Senhas armazenadas com hash BCrypt.
- Consulta e atualização dos próprios dados.
- Alteração da própria senha.

### Clientes
- Cadastro e consulta de clientes.
- Consulta de cliente pelo identificador do usuário.
- Atualização de dados do cliente.

### Barbeiros
- Listagem e consulta de barbeiros.
- Cadastro de perfil de barbeiro associado a um usuário existente.
- Atualização de especialidade.
- Ativação e desativação de barbeiros.

### Serviços
- Cadastro, consulta e atualização de serviços.
- Pesquisa por nome e situação (ativo/inativo).
- Listagem paginada.
- Ativação e desativação de serviços.

### Agendamentos
- Criação de agendamento por cliente autenticado.
- Consulta de um agendamento e listagem dos próprios agendamentos.
- Listagem administrativa com filtros por data, status e barbeiro.
- Consulta dos horários disponíveis de um barbeiro para determinado serviço.
- Cancelamento, confirmação e conclusão de agendamentos.
- Verificação de conflitos de horários para evitar sobreposição de atendimentos.

## Tecnologias

As versões abaixo são as declaradas no projeto ou exigidas pela configuração:

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem e plataforma |
| Spring Boot 4.1.1 | Estrutura principal da aplicação |
| Spring Web MVC | Criação dos endpoints REST |
| Spring Data JPA / Hibernate | Persistência e mapeamento objeto-relacional |
| Spring Security | Autenticação e autorização |
| JWT (JJWT 0.13.0) | Emissão e validação de tokens |
| PostgreSQL | Banco de dados relacional |
| Flyway | Versionamento e execução das migrations |
| Springdoc OpenAPI 3.1.0 | Documentação Swagger |
| Jakarta Bean Validation | Validação dos dados de entrada |
| Maven | Gerenciamento de dependências e build |
| JUnit / Mockito | Testes automatizados |
| Lombok | Redução de código repetitivo em partes do projeto |

## Arquitetura e organização

O código está organizado por funcionalidades (*package by feature*). Cada módulo concentra suas classes de controller, service, repository, DTO e entidade/modelo, conforme a necessidade.

```text
src/
├── main/
│   ├── java/br/unisales/sistema_agendamento/
│   │   ├── agendamento/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── enumeration/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   ├── auth/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   └── service/
│   │   ├── barbeiro/
│   │   ├── cliente/
│   │   ├── config/
│   │   ├── exception/
│   │   ├── security/
│   │   ├── servico/
│   │   └── usuario/
│   └── resources/
│       ├── db/migration/
│       └── application.exemplo.properties
└── test/
    └── java/br/unisales/sistema_agendamento/
```

### Responsabilidade das camadas

- **Controller:** recebe requisições HTTP, valida a entrada e devolve as respostas.
- **Service:** concentra as regras de negócio e coordena as operações.
- **Repository:** realiza o acesso aos dados por meio do Spring Data JPA.
- **Entity/Model:** representa os dados persistidos.
- **DTO:** define os dados aceitos nas requisições e devolvidos nas respostas.
- **Config/Security:** reúne configurações da aplicação, documentação OpenAPI e segurança.
- **Exception:** contém exceções específicas e o tratamento global de erros.

## Perfis e permissões

A aplicação trabalha com três perfis:

| Perfil | Acesso principal |
|---|---|
| `CLIENTE` | Criar agendamentos, consultar os próprios agendamentos e cancelar os próprios agendamentos conforme as regras do sistema |
| `BARBEIRO` | Consultar os próprios agendamentos, confirmar os agendamentos sob sua responsabilidade e concluir atendimentos |
| `ADMIN` | Gerenciar cadastros administrativos, consultar todos os agendamentos com filtros e executar operações administrativas |

As permissões também são verificadas nas regras de negócio dos agendamentos. Por exemplo, o barbeiro que confirma um atendimento deve ser o responsável por ele.

> O cadastro público em `/auth/cadastro` cria somente usuários com perfil `CLIENTE`. O código não disponibiliza cadastro público de administradores ou barbeiros. Esses perfis precisam ser provisionados por um procedimento administrativo controlado antes de utilizar as operações correspondentes.

## Pré-requisitos

Antes de executar a aplicação, tenha instalado:

- JDK 21.
- PostgreSQL.
- Git (para clonar o repositório).
- Maven — opcional, pois o projeto inclui Maven Wrapper (`mvnw` e `mvnw.cmd`).

## Configuração do ambiente

### 1. Clone o repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd Projeto-Sistema-Agendamento
```

Substitua `<URL_DO_REPOSITORIO>` pelo endereço real do repositório Git.

### 2. Crie o banco de dados

No PostgreSQL, crie o banco utilizado pela aplicação:

```sql
CREATE DATABASE sistema_agendamento;
```

O usuário informado na configuração precisa ter permissão para conectar ao banco e criar/alterar as tabelas utilizadas pelas migrations.

### 3. Configure as propriedades

O repositório contém o arquivo de exemplo:

```text
src/main/resources/application.exemplo.properties
```

Crie uma cópia com o nome `application.properties`, no mesmo diretório:

```text
src/main/resources/application.properties
```

Edite os valores de acordo com seu ambiente:

```properties
spring.application.name=sistema_agendamento

spring.datasource.url=jdbc:postgresql://localhost:5432/sistema_agendamento
spring.datasource.username=SEU_USUARIO_POSTGRES
spring.datasource.password=SUA_SENHA
spring.datasource.driver-class-name=org.postgresql.Driver

spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate

# Gere uma chave Base64 forte e mantenha-a em segredo.
jwt.secret=SUA_CHAVE_BASE64
# 3600000 milissegundos = 1 hora.
jwt.expiration=3600000

agendamento.abertura=09:00
agendamento.inicio-intervalo=12:00
agendamento.fim-intervalo=14:00
agendamento.fechamento=18:00
agendamento.passo-minutos=10
agendamento.dias=MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY
```

**Atenção à chave JWT:** o código decodifica `jwt.secret` como Base64 e utiliza o resultado como chave de assinatura HMAC. Portanto, não use uma frase comum nem copie uma chave de exemplo para produção. Gere uma chave aleatória forte, codifique-a em Base64 e mantenha-a fora do controle de versão.

Exemplo para gerar uma chave Base64 em ambientes com OpenSSL:

```bash
openssl rand -base64 32
```

Copie o resultado para `jwt.secret`.

> Não envie seu `application.properties`, senha do banco ou chave JWT para o Git. O arquivo `application.exemplo.properties` deve conter apenas valores ilustrativos.

## Execução

Com o PostgreSQL ativo e o arquivo `application.properties` configurado, execute a aplicação na raiz do projeto.

### Windows (PowerShell ou CMD)

```bat
mvnw.cmd spring-boot:run
```

### Linux ou macOS

```bash
./mvnw spring-boot:run
```

Também é possível executar com Maven instalado:

```bash
mvn spring-boot:run
```

Na primeira inicialização, o Flyway executa as migrations disponíveis em `src/main/resources/db/migration`. O Hibernate está configurado com `ddl-auto=validate`, ou seja, valida o mapeamento das entidades em relação ao schema em vez de criar as tabelas automaticamente.

Por padrão, a API utiliza:

```text
http://localhost:8080
```

## Documentação Swagger

Com a aplicação em execução, acesse:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

A documentação permite visualizar os endpoints, consultar parâmetros e testar requisições. Para rotas protegidas, utilize o botão **Authorize** e informe o token JWT.

## Autenticação

### 1. Cadastro de cliente

**POST** `/auth/cadastro`

Exemplo de corpo:

```json
{
  "nome": "João Silva",
  "email": "joao@email.com",
  "senha": "123456",
  "telefone": "27999999999"
}
```

O cadastro público cria a conta com perfil `CLIENTE` e seu respectivo perfil de cliente.

### 2. Login

**POST** `/auth/login`

Exemplo de corpo:

```json
{
  "email": "joao@email.com",
  "senha": "123456"
}
```

A resposta contém o token JWT e os dados básicos do usuário. Utilize o valor do token nas próximas requisições protegidas.

### 3. Envio do token

Inclua o cabeçalho abaixo nas chamadas que exigem autenticação:

```http
Authorization: Bearer SEU_TOKEN_JWT
```

O token tem sua validade definida pela propriedade `jwt.expiration`. No arquivo de exemplo, o valor é `3600000` milissegundos, equivalente a **1 hora**.

## Endpoints

Todos os caminhos abaixo são relativos à URL base `http://localhost:8080`.

### Autenticação

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/auth/cadastro` | Cadastra uma conta de cliente | Público |
| `POST` | `/auth/login` | Autentica e retorna JWT | Público |

### Usuários

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| `GET` | `/usuarios/me` | Consulta os dados do usuário autenticado | Autenticado |
| `PUT` | `/usuarios/me` | Atualiza os dados do próprio usuário | Autenticado |
| `PUT` | `/usuarios/me/senha` | Altera a própria senha | Autenticado |

### Clientes

As rotas de clientes estão protegidas para administradores na configuração atual de segurança.

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/clientes` | Cadastra um perfil de cliente | `ADMIN` |
| `GET` | `/clientes/{id}` | Busca cliente pelo ID | `ADMIN` |
| `GET` | `/clientes/usuario/{usuarioId}` | Busca cliente pelo ID do usuário | `ADMIN` |
| `PUT` | `/clientes/{id}` | Atualiza um perfil de cliente | `ADMIN` |

### Barbeiros

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| `GET` | `/barbeiros` | Lista barbeiros | Público |
| `GET` | `/barbeiros/{id}` | Consulta barbeiro pelo ID | Público |
| `GET` | `/barbeiros/{id}/horarios-disponiveis` | Consulta horários livres | Público |
| `POST` | `/barbeiros` | Cadastra perfil de barbeiro | `ADMIN` |
| `PUT` | `/barbeiros/{id}` | Atualiza barbeiro | `ADMIN` |
| `PATCH` | `/barbeiros/{id}/ativar` | Ativa barbeiro | `ADMIN` |
| `PATCH` | `/barbeiros/{id}/desativar` | Desativa barbeiro | `ADMIN` |

A consulta de horários disponíveis exige os parâmetros `data` e `servicoId`. Exemplo:

```http
GET /barbeiros/1/horarios-disponiveis?data=2026-12-10&servicoId=2
```

Formato da data: `AAAA-MM-DD`.

### Serviços

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/servicos` | Cadastra serviço | `ADMIN` |
| `GET` | `/servicos/{id}` | Consulta serviço pelo ID | Público |
| `GET` | `/servicos` | Lista serviços com paginação | Público |
| `GET` | `/servicos/buscar` | Pesquisa por nome e/ou situação | Público |
| `PUT` | `/servicos/{id}` | Atualiza serviço | `ADMIN` |
| `PATCH` | `/servicos/{id}/{valor}` | Ativa ou desativa serviço (`true`/`false`) | `ADMIN` |

A listagem utiliza paginação do Spring Data. Entre os parâmetros disponíveis estão `page`, `size` e `sort`. Exemplo:

```http
GET /servicos?page=0&size=10&sort=nome,asc
```

Exemplo de pesquisa:

```http
GET /servicos/buscar?nome=corte&ativo=true&page=0&size=10
```

### Agendamentos

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/agendamentos` | Cria agendamento | `CLIENTE` |
| `GET` | `/agendamentos/{id}` | Consulta agendamento acessível ao usuário | `CLIENTE`, `BARBEIRO` ou `ADMIN`, conforme vínculo/regra |
| `GET` | `/agendamentos/meus` | Lista agendamentos do usuário autenticado | `CLIENTE` ou `BARBEIRO` |
| `GET` | `/agendamentos` | Lista agendamentos com filtros | `ADMIN` |
| `PATCH` | `/agendamentos/{id}/cancelar` | Cancela agendamento | `CLIENTE` ou `ADMIN`, respeitadas as regras |
| `PATCH` | `/agendamentos/{id}/confirmar` | Confirma agendamento | `BARBEIRO` responsável |
| `PATCH` | `/agendamentos/{id}/concluir` | Conclui atendimento | `BARBEIRO` responsável ou `ADMIN` |

#### Criar agendamento

**POST** `/agendamentos`

Exemplo de corpo:

```json
{
  "barbeiroId": 1,
  "servicoId": 2,
  "dataHoraInicio": "2026-12-10T10:00:00",
  "observacao": "Preferência por corte tradicional"
}
```

O cliente é identificado a partir do usuário autenticado; não é enviado um `clienteId` no corpo da requisição.

#### Filtrar agendamentos (administrador)

**GET** `/agendamentos`

Parâmetros opcionais:

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `data` | `AAAA-MM-DD` | Filtra pela data |
| `status` | Texto | `AGENDADO`, `CONFIRMADO`, `CANCELADO` ou `CONCLUIDO` |
| `barbeiroId` | Número | Filtra pelo identificador do barbeiro |

Exemplo:

```http
GET /agendamentos?data=2026-12-10&status=AGENDADO&barbeiroId=1
```

## Regras de agendamento

O módulo de agendamento aplica regras para manter a consistência dos horários:

- O agendamento deve ocorrer no futuro.
- O barbeiro e o serviço precisam existir e estar ativos.
- A duração do atendimento é obtida a partir do serviço escolhido.
- O horário final é calculado considerando a duração do serviço.
- O atendimento completo precisa caber dentro de um dos períodos de expediente.
- Horários que se sobrepõem a outros agendamentos não cancelados são considerados ocupados.
- A consulta de disponibilidade considera a data, o barbeiro, o serviço e a duração do atendimento.
- O cliente pode cancelar o próprio agendamento com pelo menos duas horas de antecedência. O administrador também pode cancelar.
- A confirmação é feita pelo barbeiro responsável e deve ocorrer antes do início do atendimento.
- A conclusão só pode ocorrer quando o horário final do atendimento já tiver sido alcançado.
- Os estados possíveis são `AGENDADO`, `CONFIRMADO`, `CANCELADO` e `CONCLUIDO`.

### Expediente configurável

Os horários são configurados por propriedades:

| Propriedade | Valor padrão |
|---|---|
| `agendamento.abertura` | `09:00` |
| `agendamento.inicio-intervalo` | `12:00` |
| `agendamento.fim-intervalo` | `14:00` |
| `agendamento.fechamento` | `18:00` |
| `agendamento.passo-minutos` | `10` |
| `agendamento.dias` | Segunda a sábado |

A configuração padrão divide o expediente em dois períodos: antes e depois do intervalo. Os horários candidatos são gerados em intervalos de 10 minutos, e o atendimento precisa terminar até o limite do período correspondente.

## Banco de dados e migrations

O PostgreSQL é utilizado como banco relacional. O Flyway controla a evolução do schema por arquivos SQL versionados.

Migrations presentes no projeto:

| Migration | Descrição |
|---|---|
| `V1__create_servico_schema.sql` | Criação da tabela de serviços |
| `V2__create_tb_usuario.sql` | Criação da tabela de usuários |
| `V3__create_tabela_barbeiros.sql` | Criação da tabela de barbeiros |
| `V4__create_tabela_cliente.sql` | Criação da tabela de clientes |
| `V5__create_tb_agendamento.sql` | Criação da tabela de agendamentos e índices |
| `V6__alter_servico_preco_precisao.sql` | Ajuste da precisão da coluna de preço |

O Hibernate utiliza `spring.jpa.hibernate.ddl-auto=validate`. Assim, as migrations do Flyway são responsáveis pela criação e alteração das tabelas, enquanto o Hibernate verifica se o mapeamento das entidades corresponde ao banco.

## Testes

O projeto contém testes automatizados para a aplicação e para módulos como:

- Agendamentos.
- Autenticação e cadastro.
- Serviço e validações.
- Cliente.
- Barbeiro.
- Usuário.
- Serviço de JWT.

Para executar todos os testes:

### Windows

```bat
mvnw.cmd test
```

### Linux ou macOS

```bash
./mvnw test
```

## Equipe

Projeto acadêmico desenvolvido em equipe.

| Integrante | Responsabilidade principal |
|---|---|
| João Victor Pereira | Módulo de Barbeiro |
| Breno Clemente | Módulo de Usuário |
| Moacyr Filipe Pereira | Módulo de Cliente |
| Mateus Alves Costa | Módulo de Serviço |
| Juan Pablo Rocha Hempel | Módulo de Agendamento, autenticação, segurança JWT, tratamento de exceções e configuração Swagger/OpenAPI |

## Observações

- Este repositório representa um projeto acadêmico.
- As rotas e permissões descritas refletem a configuração encontrada no código do projeto.
- O arquivo `application.exemplo.properties` é um modelo de configuração; crie seu próprio `application.properties` localmente.
- Nunca publique credenciais do PostgreSQL ou a chave secreta JWT.
- Para conferir os schemas, parâmetros, modelos de requisição e respostas documentados pela aplicação, consulte o Swagger com o servidor em execução.
