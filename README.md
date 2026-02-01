# Barbearia API

API REST para gerenciamento de barbearia, incluindo usuários, serviços e agendamentos.  
Feita com **Spring Boot 3 + PostgreSQL**.

---

## 🚀 Tecnologias

- Java 25
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- Spring Security + JWT
- Lombok
- Hibernate

---

## ⚙️ Setup

1. Clone o repositório:

```bash
git clone https://github.com/AlexandreAbreuCzr/Barberia-Api.git
cd Barbearia-Api
````

2. Configure variáveis de ambiente:

```bash
# Banco de dados
export DB_URL=jdbc:postgresql://localhost:5432/barbearia_api
export DB_USERNAME=postgres
export DB_PASSWORD=SUA_SENHA

# JWT secret
export JWT_SECRET="uma-chave-grande-e-segura"
```

> No Windows PowerShell use `setx` em vez de `export`.

3. Crie o banco PostgreSQL:

```sql
CREATE DATABASE barbearia_api;
```

4. Rode a aplicação:

```bash
./mvnw spring-boot:run
```

---

## 🔑 Autenticação

* Login: `POST /auth/login`
* Registro: `POST /auth/register`
* Token JWT obrigatório nos endpoints protegidos:

```http
Authorization: Bearer <token>
```

---

## 👥 Usuários

| Endpoint            | Método    | Descrição                    | Acesso      |
| ------------------- | --------- | ---------------------------- | ----------- |
| `/usuario/me`       | GET       | Dados do usuário autenticado | Autenticado |
| `/usuario/admin/**` | GET/PATCH | Gerenciamento de usuários    | ADMIN       |

---

## 💈 Serviços

| Endpoint         | Método | Descrição         | Acesso  |
| ---------------- | ------ | ----------------- | ------- |
| `/servicos`      | GET    | Listar serviços   | Público |
| `/servicos`      | POST   | Criar serviço     | ADMIN   |
| `/servicos/{id}` | PATCH  | Atualizar serviço | ADMIN   |
| `/servicos/{id}` | DELETE | Deletar serviço   | ADMIN   |

---

## 📅 Agendamentos

| Endpoint                     | Método | Descrição                      | Acesso      |
| ---------------------------- | ------ | ------------------------------ | ----------- |
| `/agendamento`               | POST   | Criar agendamento              | Autenticado |
| `/agendamento/me`            | GET    | Listar agendamentos do usuário | Autenticado |
| `/agendamento/{id}`          | GET    | Buscar agendamento por ID      | Autenticado |
| `/agendamento/{id}`          | PATCH  | Atualizar agendamento          | Autenticado |
| `/agendamento/{id}/cancelar` | PATCH  | Cancelar agendamento           | Autenticado |

> Observações:
>
> * Agendamentos não podem ser criados no passado.
> * Deve-se marcar com **no mínimo 1 dia de antecedência**.
> * Não é permitido agendar domingo.
> * Horário de atendimento: 09:00–12:00 e 13:00–20:00.

---

## 🛠️ Contribuição

1. Faça um fork
2. Crie branch: `git checkout -b feature/minha-nova-funcionalidade`
3. Commit: `git commit -m "feat: minha nova funcionalidade"`
4. Push: `git push origin feature/minha-nova-funcionalidade`
5. Crie Pull Request

---

## 📄 Licença

GNU © Alexandre Abreu Czarnieski
