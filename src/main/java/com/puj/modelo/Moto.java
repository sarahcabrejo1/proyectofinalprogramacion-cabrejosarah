package com.puj.modelo;  

public class Moto extends Vehiculo {

    private static final long TARIFA_MINUTO = 80;

    public Moto(String placa) {
        super(placa);  
    }

    @Override
     public String getTipo() {
        return "MOTO";
    }

    @Override
    public boolean placaValida() {
        String p = getPlaca();  
        if (p == null || p.length() != 6) {
            return false;
        }

        return esLetra(p.charAt(0)) && esLetra(p.charAt(1)) && esLetra(p.charAt(2))
                && esDigito(p.charAt(3)) && esDigito(p.charAt(4)) && esLetra(p.charAt(5));
    }

    @Override
    public long calcularTarifa(long minutos) {
        return minutos * TARIFA_MINUTO;
    }
}
