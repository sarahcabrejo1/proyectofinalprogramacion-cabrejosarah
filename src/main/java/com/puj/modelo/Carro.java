package com.puj.modelo;

public class Carro extends Vehiculo {

    private static final long TARIFA_MINUTO = 120;

    public Carro(String placa) {
        super(placa);
    }

    @Override
    public String getTipo() {
        return "CARRO";
    }

    @Override
    public boolean placaValida() {
        String p = getPlaca();
        if (p == null || p.length() != 6) {
            return false;
        }
        return esLetra(p.charAt(0)) && esLetra(p.charAt(1)) && esLetra(p.charAt(2))
                && esDigito(p.charAt(3)) && esDigito(p.charAt(4)) && esDigito(p.charAt(5));
    }

    @Override
    public long calcularTarifa(long minutos) {
        return minutos * TARIFA_MINUTO;
    }
}
