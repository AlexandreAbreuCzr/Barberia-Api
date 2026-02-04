# Barbearia API

API REST para gerenciamento de barbearia (usuarios, servicos, agendamentos, comissoes e caixa).
Feita com Spring Boot 3 + PostgreSQL.

## Tecnologias
- Java 25
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- Spring Security + JWT
- Spring Mail
- Hibernate

## Setup
1. Clone o repositorio
```bash
git clone https://github.com/AlexandreAbreuCzr/Barberia-Api.git
cd Barbearia-Api
```

2. Configure variaveis de ambiente
```bash
# Banco de dados
export DB_URL=jdbc:postgresql://localhost:5432/barbearia_api
export DB_USERNAME=postgres
export DB_PASSWORD=SUA_SENHA

# JWT
export JWT_SECRET="uma-chave-grande-e-segura"

# Email (recuperacao de senha)
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=seu_email@gmail.com
export MAIL_PASSWORD=sua_senha_de_app
export MAIL_FROM=seu_email@gmail.com
```
> No Windows PowerShell use `setx` em vez de `export`.

3. Crie o banco
```sql
CREATE DATABASE barbearia_api;
```

4. Rode a aplicacao
```bash
./mvnw spring-boot:run
```
A API sobe em `http://localhost:8080`.

## Variaveis obrigatorias
| Variavel | Descricao |
| --- | --- |
| `DB_URL` | URL JDBC do PostgreSQL |
| `DB_USERNAME` | Usuario do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Segredo para assinar tokens JWT |
| `MAIL_HOST` | Host SMTP |
| `MAIL_PORT` | Porta SMTP |
| `MAIL_USERNAME` | Usuario SMTP |
| `MAIL_PASSWORD` | Senha SMTP (senha de app) |
| `MAIL_FROM` | Email remetente |

## Autenticacao
- Login: `POST /auth/login`
- Registro: `POST /auth/register`
- Recuperacao de senha: `POST /auth/password/forgot`
- Reset de senha: `POST /auth/password/reset`

Token JWT deve ir nos endpoints protegidos:
```http
Authorization: Bearer <token>
```

## Usuarios
| Endpoint | Metodo | Descricao | Acesso |
| --- | --- | --- | --- |
| `/usuario/me` | GET | Dados do usuario autenticado | Autenticado |
| `/usuario/me` | PATCH | Atualizar nome/telefone/senha | Autenticado |
| `/usuario/barbeiros` | GET | Listar profissionais | Publico |
| `/usuario/admin` | GET | Listar usuarios (filtros) | ADMIN |
| `/usuario/admin/{username}` | GET | Buscar usuario | ADMIN |
| `/usuario/admin/{username}/status` | PATCH | Atualizar status | ADMIN |
| `/usuario/admin/{username}/role` | PATCH | Atualizar role | ADMIN |

## Servicos
Base: `/servico`
| Endpoint | Metodo | Descricao | Acesso |
| --- | --- | --- | --- |
| `/servico` | GET | Listar servicos (filtros opcionais) | Publico |
| `/servico` | POST | Criar servico | ADMIN |
| `/servico` | POST (multipart) | Criar servico com imagem | ADMIN |
| `/servico/{id}` | PATCH | Atualizar servico | ADMIN |
| `/servico/{id}` | DELETE | Remover servico (soft delete quando necessario) | ADMIN |
| `/servico/{id}/imagem` | PATCH (multipart) | Atualizar imagem | ADMIN |

## Agendamentos
| Endpoint | Metodo | Descricao | Acesso |
| --- | --- | --- | --- |
| `/agendamento` | POST | Criar agendamento | Autenticado |
| `/agendamento/me` | GET | Listar agendamentos do usuario | Autenticado |
| `/agendamento/{id}` | GET | Buscar por ID | Autenticado |
| `/agendamento/{id}` | PUT | Atualizar data/hora | Autenticado |
| `/agendamento/{id}` | DELETE | Apagar requisitado | Autenticado |
| `/agendamento/{id}/cancelar` | PATCH | Cancelar requisitado/agendado | Autenticado |
| `/agendamento/{id}/aceitar` | PATCH | Aceitar requisitado | BARBEIRO/ADMIN |
| `/agendamento/{id}/concluir` | PATCH | Finalizar agendado | BARBEIRO/ADMIN |

Regras principais
- Antecedencia minima: 15 minutos.
- Domingo: nao permitido.
- Horario: 09:00-12:00 e 13:00-20:00.
- Conflitos consideram agendamentos REQUISITADOS e AGENDADOS.
- Barbeiro e opcional no create. Se nao informado, o primeiro barbeiro que aceitar fica com o pedido.

## Indisponibilidades
| Endpoint | Metodo | Descricao | Acesso |
| --- | --- | --- | --- |
| `/indisponibilidade` | POST | Criar indisponibilidade | BARBEIRO/ADMIN |
| `/indisponibilidade` | GET | Listar (com filtros) | Autenticado |
| `/indisponibilidade/{id}` | GET | Buscar por ID | Autenticado |
| `/indisponibilidade/{id}` | DELETE | Remover indisponibilidade | BARBEIRO/ADMIN |

Filtros em `GET /indisponibilidade`
- `barbeiroUsername`
- `inicio` (LocalDateTime)
- `fim` (LocalDateTime)
- `tipo` (enum TipoIndisponibilidade)

## Comissoes
| Endpoint | Metodo | Descricao | Acesso |
| --- | --- | --- | --- |
| `/comissao` | GET | Listar comissoes (filtros) | BARBEIRO/ADMIN |
| `/comissao/{id}` | PATCH | Atualizar comissao | ADMIN |
| `/comissao/taxa` | GET | Ver taxa global | ADMIN |
| `/comissao/taxa` | PATCH | Atualizar taxa global | ADMIN |

## Caixa
| Endpoint | Metodo | Descricao | Acesso |
| --- | --- | --- | --- |
| `/caixa` | GET | Extrato (filtros) | ADMIN |
| `/caixa` | POST | Lancamento manual | ADMIN |

## Uploads
| Endpoint | Metodo | Descricao | Acesso |
| --- | --- | --- | --- |
| `/uploads/**` | GET | Imagens de servicos | Publico |

## Contribuicao
1. Fork
2. Branch: `git checkout -b feature/minha-nova-funcionalidade`
3. Commit: `git commit -m "feat: minha nova funcionalidade"`
4. Push: `git push origin feature/minha-nova-funcionalidade`
5. Pull Request

## Licenca
GNU (c) Alexandre Abreu Czarnieski
