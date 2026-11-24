package com.puj.negocio;

import com.puj.excepciones.FechaInvalidaException;
import com.puj.excepciones.PlacaInvalidaException;
import com.puj.excepciones.VehiculoNoEncontradoException;
import com.puj.excepciones.VehiculoYaAdentroException;
import com.puj.modelo.Convenio;
import com.puj.modelo.Mensualidad;  
import com.puj.modelo.Tiquete;
import com.puj.modelo.Vehiculo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;  
import java.util.ArrayList;
import java.util.List;

public class Parqueadero implements IParqueadero, Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] TIPOS = {"CARRO", "MOTO", "ELECTRICO"};

    private int siguienteNumero;  
     private ArrayList<Mensualidad> mensualidades;
    private ArrayList<Tiquete> tiquetes;

    public Parqueadero() {
        this.siguienteNumero = 1;
        this.mensualidades = new ArrayList<>();
        this.tiquetes = new ArrayList<>();
    }



    @Override
    public void agregarMensualidad(Mensualidad m) throws FechaInvalidaException {
        if (m.getFin().isBefore(m.getInicio())) {
              throw new FechaInvalidaException("La mensualidad de " + m.getPlaca() + " termina el "
                    + m.getFin() + ", antes de su inicio el " + m.getInicio());
        }
        mensualidades.add(m);
    }

    @Override
    public Tiquete registrarEntrada(Vehiculo v, LocalDateTime entrada)
            throws PlacaInvalidaException, VehiculoYaAdentroException {

          if (!v.placaValida()) {
            throw new PlacaInvalidaException("La placa " + v.getPlaca()
                    + " no es valida para un vehiculo " + v.getTipo());
        }

        if (buscarAbierto(v.getPlaca()) != null) {
            throw new VehiculoYaAdentroException("El vehiculo " + v.getPlaca()
                    + " ya esta dentro del parqueadero");
        }

        Tiquete t = new Tiquete(siguienteNumero, v, entrada);  
        siguienteNumero++;
        tiquetes.add(t);
        return t;
    }

    @Override
    public Tiquete registrarSalida(String placa, LocalDateTime salida, Convenio convenio)
            throws VehiculoNoEncontradoException, FechaInvalidaException {

        Tiquete t = buscarAbierto(placa);
        if (t == null){
            throw new VehiculoNoEncontradoException("No hay un tiquete abierto para la placa " + placa);
        }

          if (salida.isBefore(t.getEntrada())) {
            throw new FechaInvalidaException("La salida de " + placa + " (" + salida.format(FORMATO)
                    + ") es anterior a su entrada (" + t.getEntrada().format(FORMATO) + ")");
        }

        boolean mensualidad = tieneMensualidad(placa, t.getEntrada().toLocalDate());
        t.cerrar(salida, convenio, mensualidad);
        return t;
    }



     @Override
    public List<String> reporteVehiculosAdentro(){
       ArrayList<Tiquete> abiertos = new ArrayList<>();
        for (Tiquete t : tiquetes) {
             if (t.estaAbierto()) {
                abiertos.add(t);
            }
        }


        for (int i = 0; i < abiertos.size() - 1; i++) {
            int menor = i;
            for (int j = i + 1; j < abiertos.size(); j++) {
                if (vaAntes(abiertos.get(j), abiertos.get(menor))) {
                    menor = j;
                }
            }
           Tiquete aux = abiertos.get(i);
            abiertos.set(i, abiertos.get(menor));
            abiertos.set(menor, aux);
        }

        List<String> lineas = new ArrayList<>();
         for (Tiquete t : abiertos) {
            lineas.add(t.getNumero() + " " + t.getVehiculo().getPlaca() + " "
                    + t.getVehiculo().getTipo() + " " + t.getEntrada().format(FORMATO));
        }
        return lineas;
    }

    private boolean vaAntes(Tiquete a, Tiquete b) {
       if (a.getEntrada().isBefore(b.getEntrada())) {
            return true;
        }
        if (a.getEntrada().isEqual(b.getEntrada())) {
            return a.getVehiculo().getPlaca().compareTo(b.getVehiculo().getPlaca()) < 0;
        }
        return false;
    }

    @Override
    public List<String> reporteCierre(LocalDate fecha) {
        int cantidad = 0;
        long parqueo = 0;
        long recarga = 0;
        long total = 0;
          int[] cantidadPorTipo = new int[TIPOS.length];
        long[] totalPorTipo = new long[TIPOS.length];

        for (Tiquete t : tiquetes) {
            if (!t.estaAbierto() && t.getSalida().toLocalDate().isEqual(fecha)) {
                cantidad++;
                parqueo += t.getValorParqueo();
                  recarga += t.getValorRecarga();
                 total += t.getTotal();
                for (int i = 0; i < TIPOS.length; i++) {
                    if (TIPOS[i].equals(t.getVehiculo().getTipo())) {
                        cantidadPorTipo[i]++;
                        totalPorTipo[i] += t.getTotal();
                    }
                }
            }
        }

        List<String> lineas = new ArrayList<>();
        lineas.add("CIERRE " + fecha);
        lineas.add("tiquetes=" + cantidad);
        lineas.add("parqueo=" + parqueo);
        lineas.add("recarga=" + recarga);
        lineas.add("total=" + total);
        for (int i = 0; i < TIPOS.length; i++) {
            lineas.add(TIPOS[i] + " tiquetes=" + cantidadPorTipo[i] + " total=" + totalPorTipo[i]);
        }
        return lineas;
    }

     @Override
    public List<String> reportePlacasFrecuentes() {
        ArrayList<String> placas = new ArrayList<>();
        ArrayList<Integer> visitas = new ArrayList<>();
        ArrayList<Long> pagado = new ArrayList<>();

        for (Tiquete t : tiquetes) {
            if (!t.estaAbierto()){
                String placa = t.getVehiculo().getPlaca();
                int pos = placas.indexOf(placa);
                if (pos == -1) {
                      placas.add(placa);
                    visitas.add(1);
                    pagado.add(t.getTotal());  
                } else {
                    visitas.set(pos, visitas.get(pos) + 1);
                    pagado.set(pos, pagado.get(pos) + t.getTotal());
                }
            }
        }


        for (int i = 0; i < placas.size() - 1; i++) {
            int mejor = i;
            for (int j = i + 1; j < placas.size(); j++) {
                if (esMasFrecuente(visitas.get(j), pagado.get(j), placas.get(j),
                        visitas.get(mejor), pagado.get(mejor), placas.get(mejor))){
                    mejor = j;
                }
            }
            String auxPlaca = placas.get(i);
            placas.set(i, placas.get(mejor));
            placas.set(mejor, auxPlaca);

            Integer auxVisitas = visitas.get(i);
            visitas.set(i, visitas.get(mejor));
            visitas.set(mejor, auxVisitas);

           Long auxPagado = pagado.get(i);
            pagado.set(i, pagado.get(mejor));
            pagado.set(mejor, auxPagado);
        }

        List<String> lineas = new ArrayList<>();  
          for (int i = 0; i < placas.size() && i < 3; i++) {
            lineas.add((i + 1) + ". " + placas.get(i) + " visitas=" + visitas.get(i)
                    + " pagado=" + pagado.get(i));
        }
        return lineas;
    }

    private boolean esMasFrecuente(int visitasA, long pagadoA, String placaA,
                                   int visitasB, long pagadoB, String placaB){
        if (visitasA != visitasB) {
            return visitasA > visitasB;
        }
        if (pagadoA != pagadoB) {
            return pagadoA > pagadoB;
        }
       return placaA.compareTo(placaB) < 0;
    }

    @Override
    public List<Tiquete> getTiquetes() {
          return tiquetes;
    }



    private Tiquete buscarAbierto(String placa) {
        for (Tiquete t : tiquetes) {
            if (t.estaAbierto() && t.getVehiculo().getPlaca().equals(placa)) {
                return t;
            }
        }
        return null;
    }

    private boolean tieneMensualidad(String placa, LocalDate fecha) {
        for (Mensualidad m : mensualidades) {
           if (m.getPlaca().equals(placa) && m.cubre(fecha)) {
               return true;  
            }
        }
        return false;
    }



    public int getSiguienteNumero() {
        return siguienteNumero;
    }

    public void setSiguienteNumero(int siguienteNumero) {
        this.siguienteNumero = siguienteNumero;
    }

    public ArrayList<Mensualidad> getMensualidades() {
        return mensualidades;
    }

    public void setMensualidades(ArrayList<Mensualidad> mensualidades) {
        this.mensualidades = mensualidades;  
    }


    public void setTiquetes(ArrayList<Tiquete> tiquetes) {
        this.tiquetes = tiquetes;
    }

    @Override
    public String toString() {
        return "Parqueadero: " + tiquetes.size() + " tiquetes, " + mensualidades.size()
                + " mensualidades, siguiente tiquete " + siguienteNumero;
    }
}
