# 1. [ZUP - BackEnd Developer] Transferência entre contas e gerenciamento de saldo

## Desafio: transferência entre contas e gerenciamento de saldo

Construir aplicações bancárias são tarefas extremamente desafiadoras, às vezes por simplesmente serem repletas de regras de negócio complexas, ou por estas soluções serem amplamente utilizadas por milhões de pessoas. Dado isso, sua missão hoje é ajudar na construção de algumas funcionalidades.

Sua missão será a construção de um microsserviço que expõe uma API REST para ser consumida por outros serviços e aplicações existentes na infraestrutura de uma grande instituição financeira (um banco). Para isso, você precisará implementar uma API REST que precisa expor 4 endpoints responsáveis pelas seguintes tarefas:

1. Cadastro de conta corrente
1. Transferência entre contas correntes
1. Listagem de transferências de uma conta
1. Consulta de saldo de uma conta

Este microsserviço será um componente importante dentro arquitetura desenhada para a empresa, pois dezenas ou até centenas de outros microsserviços, sistemas satelites e alguns sistemas legados se comunicarão com ele para consumir seus endpoints. Não à toa, o time de arquitetos está prevendo que em momentos de picos alguns dos endpoints poderão receber centenas ou mesmo alguns poucos milhares de requisições por segundo, e por isso deve-se ter cuidado com concorrência e volumetria dos dados em produção.

## Restrições

As tecnologias utilizadas para implementação deste microsserviço são definidas pelo time de arquitetura da instituição financeira, desta forma você precisa segui-las sem exceção. As tecnologias a serem utilizadas são:

1. Java 8
1. Spring Boot
1. Spring Data JPA e Hibernate
1. Bean Validation
1. Banco de dados relacional: no desafio usaremos o banco em memória H2 Database
1. jUnit para testes automatizados

Apesar desta restrição nas tecnologias, você pode extrair o melhor destas tecnologias como achar necessário.

## Tarefas do desafio

### Tarefa 01: Cadastro de conta corrente

Crie um endpoint na sua API REST para cadastro de uma nova conta corrente, aqui seu objetivo é receber os dados básicos de um cliente para criar uma conta para o mesmo. Siga as seguintes restrições:

- Toda conta deve conter os seguintes campos: **agência**, **número da conta**, **email**, **CPF** e **titular**
- Todos os campos são obrigatórios
- O campo **email** deve ser valido e único no sistema
- O campo **CPF** deve ser valido e único no sistema
- O campo **agência** deve conter exatamente 4 dígitos e todos devem ser numéricos
- O campo **número da conta** deve conter exatamente 6 dígitos e todos devem ser numéricos
- O campo **titular** deve conter máximo de 120 caracteres

### Tarefa 02: Transferência entre contas correntes

Crie um endpoint na sua API REST para que seja possível realizar transferências bancarias entre duas contas existentes. Portanto, para implentar este endpoint siga as seguintes restrições:

- Os seguintes campos são necessários e obrigatórios: **conta origem**, **conta destino** e **valor de transferência**
- As **contas de origem e destino** informadas devem existir
- O campo **valor de transferência** deve ser maior que zero
- A **conta de origem** deve possuir saldo suficiente para realizar uma transferência
- Deve ser armazenado o **instante** da transferência

### Tarefa 03: Listagem de transferências de uma conta

Agora, crie um endpoint na sua API REST para listar todas as transferências realizadas por uma determinada conta. Lembre-se, que tanto as transferências enviadas quanto as recebidas devem ser listadas por seu endpoint. Siga as seguintes restrições:

- O campo **identificador da conta** é obrigatório
- A conta informada deve existir no sistema

### Tarefa 04: Consulta de saldo de uma conta

Nesta tarefa, crie um endpoint na sua API REST para consultar o saldo de uma conta corrente existente. Para isso, siga as seguintes restrições:

- O campo **identificador da conta** é obrigatório
- Como resultado, deve-se retornar os seguintes dados para o usuário: **agência**, **número da conta** e **saldo**

## Observações

- A exceção de lock otimista pode ocorrer também quando duas transferências concorrentes são realizadas **para** uma mesma conta
  - Usar alguma forma de retry?
- Data e hora da transferência nas verificações dos testes de listagem?
- Testes automáticos de lock otimista e restrições de unicidade?
- Testes para mensagens de log?
- Erro ao usar o object mapper para transformar o JSON de resposta em um objeto Page: ``com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Cannot construct instance of `org.springframework.data.domain.Page` (no Creators, like default constructor, exist): abstract types either need to be mapped to concrete types, have custom deserializer, or contain additional type information``
- [Sobre usar o object mapper para transformar o JSON de resposta em um objeto Page](https://stackoverflow.com/questions/34647303/spring-resttemplate-with-paginated-api/46847429)
