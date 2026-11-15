package com.estudiobdd.model;

public enum TipoPasso {
    DADO("Dado"),
    QUANDO("Quando"),
    ENTAO("Então"),
    E("E"),
    MAS("Mas");

    private final String palavraChave;

    TipoPasso(String palavraChave) {
        this.palavraChave = palavraChave;
    }

    /** Palavra-chave Gherkin em português. */
    public String palavraChave() {
        return palavraChave;
    }

    /** E e Mas continuam o tipo do passo anterior. */
    public boolean ehContinuacao() {
        return this == E || this == MAS;
    }
}
