# API de Chamados do IFRN

API REST acadêmica para registrar e acompanhar chamados de suporte. O projeto usa Java 21, Spring Boot 4, Maven, Spring Web MVC, Spring Data JPA, PostgreSQL, Flyway e Jakarta Validation.

O código segue a arquitetura simples `Controller → Service → Repository → PostgreSQL`. As regras de negócio e transições ficam nos services; controllers tratam somente do contrato HTTP. Usuários são recursos somente de leitura e são inseridos pelo seed do Flyway.

## Requisitos atuais

- Docker com Docker Compose;
- arquivo `.env` baseado em `.env.example`.

Java 21 e Maven são necessários somente para desenvolvimento e execução fora dos containers. A integração contínua pertence à próxima etapa.

## Execução com Docker

Crie o arquivo local de configuração e suba a API com o PostgreSQL:

```bash
cp .env.example .env
docker compose up --build
```

O PostgreSQL possui healthcheck e a API só inicia depois que o banco estiver pronto. Na primeira inicialização, o Flyway cria as tabelas e aplica o seed. A API responde em `http://localhost:8080`, sem exigir Java, Maven ou PostgreSQL instalados na máquina.

O volume `postgres_data` mantém os dados entre reinicializações. Para encerrar os containers sem apagar o banco:

```bash
docker compose down
```

## Execução para desenvolvimento

Para executar a aplicação diretamente na máquina, suba apenas o banco e use o Maven Wrapper:

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

O Spring importa o `.env` local de forma opcional; variáveis de ambiente do sistema continuam podendo sobrescrever esses valores. Nesse modo são necessários Java 21 e Docker.

Para compilar e executar os testes unitários e de contrato HTTP:

```bash
./mvnw test
```

## Integração contínua

O GitHub Actions executa `./mvnw clean verify` com Java 21 em todo `push` para a `main` e em todo pull request destinado à `main`. Essa verificação compila, empacota e executa a suíte de testes do projeto, usando cache das dependências Maven.

## Dados iniciais

O seed cria:

- setores 1 (`COTIC`) e 2 (`COMAN`);
- categorias 1 e 2 para COTIC, 3 e 4 para COMAN;
- solicitantes 1 (Ana) e 2 (Bruno);
- técnico 3 (Carlos/COTIC) e técnica 4 (Diana/COMAN).

Esses IDs permitem percorrer o fluxo completo no arquivo [api.http](api.http). Não existem endpoints de criação, alteração ou exclusão de usuários.

## Fluxo do chamado

1. Um usuário `SOLICITANTE` abre o chamado. O sistema define `ABERTO`, a data de abertura e prioridade `MEDIA` quando omitida.
2. Um `TECNICO` do setor da categoria assume o chamado, que passa a `EM_ATENDIMENTO`.
3. O solicitante e o técnico atribuído podem adicionar interações.
4. Apenas o técnico atribuído resolve o chamado, com solução obrigatória. O sistema define `RESOLVIDO` e a data de fechamento.
5. Como alternativa, apenas o solicitante original pode cancelar enquanto o chamado está `ABERTO`.

As únicas transições aceitas são `ABERTO → EM_ATENDIMENTO → RESOLVIDO` e `ABERTO → CANCELADO`. Chamados resolvidos ou cancelados não recebem novas interações.

## Endpoints

| Método | Rota | Resultado de sucesso |
| --- | --- | --- |
| `POST` | `/setores` | `201 Created` |
| `GET` | `/setores` | `200 OK`, paginado, filtro `nome` |
| `GET` | `/setores/{id}` | `200 OK` |
| `PUT` | `/setores/{id}` | `200 OK` |
| `DELETE` | `/setores/{id}` | `204 No Content` |
| `POST` | `/categorias` | `201 Created` |
| `GET` | `/categorias` | `200 OK`, filtros `nome` e `setorId` |
| `GET` | `/categorias/{id}` | `200 OK` |
| `PUT` | `/categorias/{id}` | `200 OK` |
| `DELETE` | `/categorias/{id}` | `204 No Content` |
| `GET` | `/setores/{id}/categorias` | `200 OK`, paginado |
| `GET` | `/usuarios` | `200 OK`, filtros `nome`, `tipo` e `setorId` |
| `GET` | `/usuarios/{id}` | `200 OK` |
| `POST` | `/chamados` | `201 Created` |
| `GET` | `/chamados` | `200 OK`, paginado e filtrado |
| `GET` | `/chamados/{id}` | `200 OK`, com interações cronológicas |
| `GET` | `/usuarios/{id}/chamados` | `200 OK`, filtro `status` |
| `PATCH` | `/chamados/{id}/assumir` | `200 OK` |
| `PATCH` | `/chamados/{id}/resolver` | `200 OK` |
| `PATCH` | `/chamados/{id}/cancelar` | `200 OK` |
| `POST` | `/chamados/{id}/interacoes` | `201 Created` |

Todas as coleções aceitam `page`, `size` e `sort`. O tamanho padrão é 10 e o máximo efetivo é 50. Os campos de ordenação usam os nomes externos em português, por exemplo:

```http
GET /chamados?status=ABERTO&prioridade=ALTA&setorId=1&page=0&size=10&sort=dataAbertura,desc
```

Os filtros de chamados são `status`, `prioridade`, `setorId`, `categoriaId`, `solicitanteId`, `tecnicoId`, `dataInicio` e `dataFim`. Datas usam ISO-8601, como `2026-10-01T00:00:00`.

## Erros

Entradas inválidas retornam `400`, recursos ausentes retornam `404` e conflitos de regra de negócio retornam `409`. O corpo é padronizado:

```json
{
  "timestamp": "2026-10-28T14:30:00",
  "status": 409,
  "message": "Técnico não pertence ao setor da categoria",
  "path": "/chamados/1/assumir"
}
```

Antes de excluir, os services verificam as dependências: categoria com chamado não pode ser removida, e setor com categoria, técnico ou chamado também não.

## Flyway e validação do esquema

O Flyway aplica as migrations em ordem e registra versão, descrição, checksum e resultado na tabela `flyway_schema_history`. Se o conteúdo de uma migration já aplicada for alterado, o checksum deixa de coincidir e a validação falha; a correção deve entrar em uma nova migration, sem editar o histórico aplicado.

O Hibernate usa `spring.jpa.hibernate.ddl-auto=validate`: ele confere se o mapeamento JPA corresponde ao banco, mas não altera o esquema. `ddl-auto=update` não é recomendado em produção porque produz mudanças automáticas sem um histórico SQL explícito, revisável e reproduzível, além de poder resultar em alterações inesperadas entre ambientes.
