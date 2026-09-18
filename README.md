<h1 align="center">
  Visto! ✅
</h1>

<p align="center">
  Helpdesk pensado pelo técnico, para o técnico — sem perder o usuário de vista.
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java"/>
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?style=flat-square&logo=springboot"/>
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql"/>
  <img alt="License" src="https://img.shields.io/badge/license-MIT-green?style=flat-square"/>
  <img alt="Status" src="https://img.shields.io/badge/status-em%20desenvolvimento-yellow?style=flat-square"/>
</p>

---

## O problema

Quem trabalha com suporte técnico conhece bem essa realidade:

- Você abre o sistema e não tem uma visão geral dos chamados — precisa entrar em cada um para saber o que está acontecendo
- Não consegue consultar um chamado anterior enquanto atende outro — o sistema não abre duas abas
- Dois técnicos acabam trabalhando no mesmo chamado sem saber, atrapalhando o atendimento um do outro
- Sua fila não mostra o que é mais urgente — chamados críticos se perdem entre os demais
- Um colega resolve um problema difícil hoje, e amanhã outro técnico perde horas no mesmo problema porque não existe registro da solução
- Você e um colega estão atendendo o mesmo incidente em empresas diferentes e ninguém sabe

Do lado do usuário, os problemas também existem:

- Abre chamados duplicados na esperança de ser atendido mais rápido
- Usa um chamado já resolvido para reportar problemas completamente diferentes
- Não sabe se alguém viu o chamado dele

## A solução

O **Visto!** é um helpdesk construído a partir da visão de quem atende — com as ferramentas que um técnico de suporte realmente precisa, sem abrir mão da experiência do usuário.

### Para o técnico
- 📋 Painel unificado com visão geral de todos os chamados abertos
- 🧠 Base de conhecimento: soluções documentadas e reutilizáveis por qualquer técnico
- ⚡ Fila inteligente com priorização por criticidade e SLA
- 🔔 Alerta quando dois técnicos acessam o mesmo chamado simultaneamente
- 🔗 Notificação de chamados com sintomas similares em andamento

### Para o usuário
- 🔍 Link público para acompanhar o status do chamado em tempo real
- 📧 Notificações por e-mail a cada atualização
- 💬 Comunicação direta com o técnico no chamado
- 🚫 Detecção de chamados duplicados antes de abrir um novo

## O diferencial

A maioria dos sistemas de helpdesk guarda o histórico mas não transforma isso em conhecimento. O **Visto!** trata cada chamado resolvido como um ativo — com problema, cenário, itens avaliados e procedimento documentados. Na próxima vez que o mesmo incidente aparecer, qualquer técnico resolve sozinho.

## Tecnologias

- **Backend:** Java 17 + Spring Boot 3.5
- **Banco de dados:** PostgreSQL
- **Autenticação:** Spring Security + JWT
- **Documentação:** Swagger / OpenAPI
- **Infra:** Docker + GitHub Actions

## Como rodar localmente

### Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker Desktop

### Passos
```bash
# Clone o repositório
git clone https://github.com/mdsjr/visto.git
cd visto

# Suba o banco de dados
docker-compose up -d

# Rode a aplicação
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`  
A documentação Swagger em `http://localhost:8080/swagger-ui.html`

## Status do projeto

| Funcionalidade | Status |
|---|---|
| Autenticação JWT (cadastro, login, perfis ADMIN/TECNICO/USUARIO) | ✅ Pronto |
| Chamados: abertura, fila por criticidade, assumir, status, prioridade/SLA | ✅ Pronto |
| Link público de acompanhamento do chamado | ✅ Pronto |
| Base de conhecimento (problema, cenário, itens avaliados, procedimento + busca) | ✅ Pronto |
| Documentação Swagger / OpenAPI | ✅ Pronto |
| Comunicação usuário ↔ técnico no chamado | 🔜 Planejado |
| Alerta de dois técnicos no mesmo chamado | 🔜 Planejado |
| Controle de duplicados / sintomas similares | 🔜 Planejado |
| Notificações por e-mail | 🔜 Planejado |
| Painel do técnico (front-end) | 🔜 Planejado |

## Endpoints principais

| Método | Rota | Quem | Descrição |
|---|---|---|---|
| POST | `/auth/register` | público | Cadastro (retorna token) |
| POST | `/auth/login` | público | Login (retorna token) |
| GET | `/auth/me` | autenticado | Dados do usuário logado |
| POST | `/chamados` | autenticado | Abre chamado |
| GET | `/chamados` | autenticado | Fila (técnico/admin) ou meus chamados (usuário) |
| GET | `/chamados/{id}` | autenticado | Detalhe |
| PATCH | `/chamados/{id}/assumir` | técnico/admin | Assume o chamado |
| PATCH | `/chamados/{id}/status` | técnico/admin (usuário só FECHADO) | Muda status |
| PATCH | `/chamados/{id}/prioridade` | técnico/admin | Muda prioridade e recalcula SLA |
| GET | `/public/chamados/{codigo}` | público | Acompanhamento sem login |
| GET/POST | `/conhecimento` | autenticado / técnico | Busca e cria artigos |
| GET/PUT/DELETE | `/conhecimento/{id}` | autenticado / autor ou admin | Detalhe, edição, exclusão |

Envie o token no header `Authorization: Bearer <token>`.

> **Nota (MVP):** o campo `perfil` (`ADMIN`, `TECNICO`, `USUARIO`) pode ser informado no cadastro para facilitar os testes. Antes de ir para produção, isso deve ser restrito a um admin.

### Rodando os testes

```bash
mvn verify
```

Os testes usam o profile `test` com banco H2 em memória — não precisam do Docker.

## Contribuindo

Contribuições são bem-vindas! Leia o [CONTRIBUTING.md](CONTRIBUTING.md) para saber como participar.

## Licença

Distribuído sob a licença MIT. Veja [LICENSE](LICENSE) para mais informações.

---

<p align="center">
  Feito por <a href="https://github.com/mdsjr">Moacir Domingos</a> — técnico de suporte que resolveu construir a ferramenta que queria ter
</p>
