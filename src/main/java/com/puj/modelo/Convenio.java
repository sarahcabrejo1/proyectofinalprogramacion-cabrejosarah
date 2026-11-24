package com.puj.modelo;

public enum Convenio {
    NINGUNO(0),
    ALMACEN(30),
    RESTAURANTE(60),
    CINE(120);  

    private final int minutosGratis;

    Convenio(int minutosGratis) {
        this.minutosGratis = minutosGratis;
    }

    public int getMinutosGratis() {
       return minutosGratis;
    }
}
