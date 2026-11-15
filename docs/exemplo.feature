# language: pt
Funcionalidade: Adicionar produto ao carrinho
  Como cliente da loja
  Quero adicionar um produto ao carrinho
  Para reunir os itens que pretendo comprar

  Cenário: Adicionar produto disponível
    Dado que o produto "Caneca" está em estoque
    Quando eu adiciono a "Caneca" ao carrinho
    Então o carrinho passa a ter 1 item
    E o total do carrinho é atualizado

  Cenário: Produto sem estoque
    Dado que o produto "Caneca" está sem estoque
    Quando eu tento adicionar a "Caneca" ao carrinho
    Então vejo a mensagem "Produto indisponível"
    Mas o carrinho não é alterado
