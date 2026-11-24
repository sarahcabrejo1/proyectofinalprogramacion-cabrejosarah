package com.puj.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Tiquete implements Serializable {

   private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int numero;
    private Vehiculo vehiculo;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private Convenio convenio;
      private long valorParqueo;
    private long valorRecarga;  

    public Tiquete(int numero, Vehiculo vehiculo, LocalDateTime entrada) {
        this.numero = numero;
        this.vehiculo = vehiculo;
        this.entrada = entrada;
        this.salida = null;
        this.convenio = null;
        this.valorParqueo = 0;
        this.valorRecarga = 0;  
    }

    public boolean estaAbierto() {
          return salida == null;
    }

     public long getMinutos() {
        if (salida == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(entrada, salida);
    }


    public void cerrar(LocalDateTime salida, Convenio convenio, boolean mensualidad) {
        this.salida = salida;
        this.convenio = convenio;

        long minutos = getMinutos();
        long cobrables = minutos - convenio.getMinutosGratis();
        if (cobrables < 0) {
              cobrables = 0;
        }

        if (mensualidad) {
             valorParqueo = 0;
        } else {
            valorParqueo = vehiculo.calcularTarifa(cobrables);
        }

       valorRecarga = vehiculo.cargoAdicional(minutos);  
    }

    public long getTotal() {
        return valorParqueo + valorRecarga;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }


    public Vehiculo getVehiculo(){
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public void setEntrada(LocalDateTime entrada) {
        this.entrada = entrada;
    }


      public LocalDateTime getSalida(){
        return salida;
    }

    public void setSalida(LocalDateTime salida) {
        this.salida = salida;
    }

    public Convenio getConvenio() {
        return convenio;  
    }

    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    public long getValorParqueo() {
        return valorParqueo;
    }

    public void setValorParqueo(long valorParqueo){
        this.valorParqueo = valorParqueo;
    }

    public long getValorRecarga() {
        return valorRecarga;
    }

    public void setValorRecarga(long valorRecarga) {
        this.valorRecarga = valorRecarga;
    }

    @Override
    public String toString(){
        String texto = "Tiquete " + numero + " " + vehiculo.getPlaca() + " " + vehiculo.getTipo()
                + " entrada=" + entrada.format(FORMATO);
        if (estaAbierto()) {
            return texto + " (abierto)";
        }
        return texto + " salida=" + salida.format(FORMATO)
                + " minutos=" + getMinutos()
                + " convenio=" + convenio
                + " parqueo=" + valorParqueo
                 + " recarga=" + valorRecarga
                + " total=" + getTotal();
    }
}
