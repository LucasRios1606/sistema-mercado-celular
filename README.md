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

**Produtos** permite cadastrar nome, código de barras, preço e estoque, salvando os dados no celular. Cada produto tem **Editar produto** para alterar nome, código e preço, e **Repor estoque** para somar as unidades recebidas ao estoque atual, com prévia da quantidade resultante. Códigos duplicados e entradas inválidas são bloqueados. Cancelar os formulários não altera os dados. A edição mantém o estoque e não modifica os nomes ou preços registrados nas vendas antigas. Preços são tratados em centavos, aceitando vírgula ou ponto e até duas casas decimais.

**Vender** permite buscar produtos por parte do nome ou código, selecionar um resultado, informar quantidades, remover itens e conferir o total. A busca filtra enquanto se digita, ignora diferenças entre maiúsculas e minúsculas e acentos, e tem um botão para limpar o filtro. O leitor integrado à câmera preenche a busca com códigos EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39 e ITF. Se o código ainda não foi cadastrado, o app avisa; a busca manual continua disponível. Sem resultados, o botão de adicionar fica desativado. Pesquisar ou ler código não altera os itens já adicionados à compra. Ao finalizar, escolhe-se dinheiro, Pix ou cartão. Para dinheiro, o valor recebido é informado em reais e o troco é calculado em centavos; não se conclui a venda enquanto o valor recebido for menor que o total. A forma de pagamento, o recebido e o troco são registrados na venda. O histórico mostra a forma de pagamento e o troco quando aplicável. Vendas anteriores à mudança continuam visíveis sem forma de pagamento. Ao confirmar a venda, o aplicativo registra data, itens e total, e baixa o estoque na mesma gravação local. Quantidades acima do estoque são bloqueadas. Sair de uma compra pede confirmação e não altera o estoque; girar a tela preserva os itens da compra, o texto da busca, o produto selecionado e a quantidade digitada.

**Vendas de hoje** consulta os registros locais do dia, mostrando total vendido, quantidade de vendas, horário e produtos de cada compra, da mais recente para a mais antiga. O dia segue o fuso do celular. A tela atualiza ao ser aberta, ao voltar para o aplicativo, à meia-noite ou pelo botão **Atualizar**. A consulta usa os preços salvos em cada venda e não altera o estoque nem os registros. Se houver dados inválidos, apresenta um aviso em vez de mostrar um total incompleto.

A tela **Produtos** também permite buscar por parte do nome ou código enquanto se digita, ignorando acentos e diferenças entre maiúsculas e minúsculas. **Limpar busca** mostra a lista completa novamente. O filtro é mantido ao girar a tela e reaplicado após cadastrar, editar ou repor estoque. Quando não há resultados, a tela orienta a mudar ou limpar a busca.

**Salvar cópia dos dados**, na tela inicial, exporta todos os produtos (incluindo estoque) e todo o histórico de vendas para um arquivo JSON no local escolhido pelo usuário. O arquivo identifica o aplicativo, a versão do formato e a data da cópia. A gravação ocorre em segundo plano e só informa sucesso após fechar o arquivo. Cancelar a escolha não exporta dados; falhas são informadas sem alterar os registros originais.

**Recuperar cópia** permite escolher um arquivo de até 10 MB, conferir a data e as quantidades e confirmar a substituição de todos os produtos, estoque e vendas. Não mescla cadastros. Antes de continuar, a tela recomenda salvar uma cópia dos dados atuais e destaca quando a cópia escolhida está vazia. A validação rejeita formato incompatível, campos ausentes, códigos e IDs duplicados, valores inválidos, estouro numérico, totais divergentes e pagamentos inconsistentes; vendas antigas sem os campos de pagamento continuam aceitas. A leitura e a gravação ocorrem em segundo plano, mantendo a operação durante a rotação. Cancelar a seleção ou a confirmação não grava nada. Os dois conjuntos são gravados juntos; se a gravação retornar falha, o app tenta reverter ambos e informa caso não consiga confirmar a reversão. A restauração no aparelho ainda precisa de validação prática.

As telas usam fundo claro, botões grandes e cartões com preço e estoque destacados. O total da compra tem uma área própria. Os campos de cadastro têm rótulos permanentes. O visual compartilhado fica na classe `Visual`.

## Próximos passos

1. Compilar e testar a leitura de código de barras no celular.
2. Conferir códigos cadastrados e não cadastrados, junto com busca manual, forma de pagamento e troco.
3. Construir e testar uma funcionalidade por vez.

## Teste da exportação no celular

- Com produtos e vendas cadastrados, usar **Salvar cópia dos dados**, escolher um local e conferir o aviso de sucesso. Abrir o JSON e comparar nomes, códigos, preços, estoque e vendas (incluindo as de dias anteriores) com os dados originais.
- Repetir sem cadastros: `produtos` e `vendas` devem ser listas vazias.
- Cancelar o aviso inicial e depois cancelar o seletor de arquivos; nenhuma mensagem de sucesso deve aparecer.
- Girar o celular durante a escolha do local e concluir a exportação.
- Testar falha de gravação, como destino indisponível ou sem espaço: deve aparecer aviso de falha; um arquivo parcial não deve ser tratado como cópia válida.
- Conferir que cadastrar, vender e repor estoque continuam funcionando após exportar ou cancelar.

## Teste da recuperação no celular

Usar dados de teste e guardar uma cópia antes de começar.

- Exportar produtos e vendas, alterar os dados no app e recuperar o arquivo exportado. Conferir que o estoque, os preços e o histórico voltaram ao estado da cópia, inclusive vendas de outros dias e pagamentos.
- Escolher um arquivo inválido, truncado, de outro aplicativo ou maior que 10 MB: a recuperação deve ficar desativada e os dados atuais devem continuar iguais.
- Cancelar o seletor e a confirmação de substituição: nenhum registro deve mudar.
- Testar uma cópia vazia: conferir o aviso explícito e que só a confirmação deixa as duas listas vazias.
- Girar a tela durante a leitura, na prévia e durante a gravação: a operação não deve ser repetida nem perdida. Na confirmação, se o diálogo fechar ao girar, tocar novamente em **Recuperar esta cópia**.
- Simular falha de gravação e conferir a mensagem e a reversão; reabrir o app para verificar os dados persistidos. Não usar o caixa se a mensagem informar que a reversão não pôde ser confirmada.

## Teste da lógica de compra

Com um JDK instalado, execute na pasta do projeto:

```text
javac -encoding UTF-8 -d out/test app/src/main/java/com/mercadinho/app/Carrinho.java test/com/mercadinho/app/CarrinhoTest.java
java -cp out/test com.mercadinho.app.CarrinhoTest
javac -encoding UTF-8 -d out/test app/src/main/java/com/mercadinho/app/VendasDoDia.java test/com/mercadinho/app/VendasDoDiaTest.java
java -cp out/test com.mercadinho.app.VendasDoDiaTest
javac -encoding UTF-8 -d out/test app/src/main/java/com/mercadinho/app/ValoresProduto.java test/com/mercadinho/app/ValoresProdutoTest.java
java -cp out/test com.mercadinho.app.ValoresProdutoTest
javac -encoding UTF-8 -d out/test app/src/main/java/com/mercadinho/app/BuscaProduto.java test/com/mercadinho/app/BuscaProdutoTest.java
java -cp out/test com.mercadinho.app.BuscaProdutoTest
javac -encoding UTF-8 -d out/test app/src/main/java/com/mercadinho/app/ValoresProduto.java app/src/main/java/com/mercadinho/app/Pagamento.java test/com/mercadinho/app/PagamentoTest.java
java -cp out/test com.mercadinho.app.PagamentoTest
javac -encoding UTF-8 -d out/test app/src/main/java/com/mercadinho/app/ValidacaoBackup.java test/com/mercadinho/app/ValidacaoBackupTest.java
java -cp out/test com.mercadinho.app.ValidacaoBackupTest
javac -encoding UTF-8 -d out/test app/src/main/java/com/mercadinho/app/RestauracaoDados.java test/com/mercadinho/app/RestauracaoDadosTest.java
java -cp out/test com.mercadinho.app.RestauracaoDadosTest
```

Os testes verificam os cálculos e limites do carrinho, a soma das vendas, a seleção do dia em diferentes fusos, os preços em centavos e os limites da reposição de estoque. O teste de datas requer JDK 8 ou superior. Não substituem a compilação Android nem os testes de gravação e navegação no celular.

Os testes de backup cobrem 37 casos das regras de integridade e compatibilidade e 5 cenários da gravação e reversão com armazenamento simulado. Não exercitam o parser JSON do Android, o seletor de arquivos nem a persistência real de `SharedPreferences`.

O desenvolvimento será feito em alterações pequenas, com explicação do que mudou e autorização do responsável antes de cada commit.
