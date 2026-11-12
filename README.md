# Sistema para Mercado usando Celular

Aplicativo Android para facilitar o registro de vendas e o controle de estoque de um pequeno mercado, usando somente o celular no atendimento.

A proposta é ter letras grandes, poucos botões e passos fáceis de entender, pensando em uma pessoa com pouca familiaridade com tecnologia.

## Funcionalidades planejadas

- **Produtos:** cadastrar nome, código de barras, preço e quantidade; registrar a chegada de mercadorias.
- **Vender:** ler o código pela câmera ou buscar pelo nome, conferir a compra e finalizar a venda.
- **Estoque:** baixar a quantidade somente quando a venda for concluída.
- **Vendas de hoje:** consultar as vendas registradas e o total vendido.

Também planejamos funcionamento sem internet, dados salvos no aparelho e uma forma simples de fazer e recuperar backups.

## Estado atual

O projeto tem a tela inicial com os botões **Vender**, **Produtos** e **Vendas de hoje**, além do ícone e do tema visual. A geração do APK de teste está configurada no GitHub Actions.

**Produtos** permite cadastrar nome, código de barras, preço e estoque, salvando os dados no celular.

**Vender** permite selecionar produtos cadastrados, informar quantidades, remover itens e conferir o total. Ao confirmar a venda, o aplicativo registra data, itens e total, e baixa o estoque na mesma gravação local. Quantidades acima do estoque são bloqueadas. Sair de uma compra pede confirmação e não altera o estoque; girar a tela preserva os itens da compra.

**Vendas de hoje** consulta os registros locais do dia, mostrando total vendido, quantidade de vendas, horário e produtos de cada compra, da mais recente para a mais antiga. O dia segue o fuso do celular. A tela atualiza ao ser aberta, ao voltar para o aplicativo, à meia-noite ou pelo botão **Atualizar**. A consulta usa os preços salvos em cada venda e não altera o estoque nem os registros. Se houver dados inválidos, apresenta um aviso em vez de mostrar um total incompleto.

A leitura pela câmera e os backups ficam para etapas seguintes.

As telas usam fundo claro, botões grandes e cartões com preço e estoque destacados. O total da compra tem uma área própria. Os campos de cadastro têm rótulos permanentes. O visual compartilhado fica na classe `Visual`.

## Próximos passos

1. Compilar e testar a consulta de vendas de hoje no celular.
2. Conferir o fluxo completo: cadastrar produto, vender e consultar o total do dia.
3. Construir e testar uma funcionalidade por vez.

## Teste da lógica de compra

Com um JDK instalado, execute na pasta do projeto:

```text
javac -encoding UTF-8 -d out/test app/src/main/java/com/mercadinho/app/Carrinho.java test/com/mercadinho/app/CarrinhoTest.java
java -cp out/test com.mercadinho.app.CarrinhoTest
javac -encoding UTF-8 -d out/test app/src/main/java/com/mercadinho/app/VendasDoDia.java test/com/mercadinho/app/VendasDoDiaTest.java
java -cp out/test com.mercadinho.app.VendasDoDiaTest
```

Os testes verificam os cálculos e limites do carrinho, a soma das vendas e a seleção do dia em diferentes fusos, incluindo mudanças de horário de verão. O teste de datas requer JDK 8 ou superior. Não substituem a compilação Android nem os testes de gravação e navegação no celular.

O desenvolvimento será feito em alterações pequenas, com explicação do que mudou e autorização do responsável antes de cada commit.
