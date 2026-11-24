package com.puj.persistencia;

import com.puj.excepciones.FechaInvalidaException;
import com.puj.excepciones.PlacaInvalidaException;
import com.puj.excepciones.VehiculoNoEncontradoException;
import com.puj.excepciones.VehiculoYaAdentroException;
import com.puj.modelo.Carro;
import com.puj.modelo.CarroElectrico;
import com.puj.modelo.Convenio;
import com.puj.modelo.Mensualidad;  
import com.puj.modelo.Moto;
import com.puj.modelo.Tiquete;
import com.puj.modelo.Vehiculo;
import com.puj.negocio.IParqueadero;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ManejoArchivos {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public static List<String> cargarMensualidades(String ruta, IParqueadero sistema) throws IOException {
        List<String> mensajes = new ArrayList<>();
        int aceptadas = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();
                if (!linea.isEmpty() && !linea.startsWith("#")) {
                    try {
                       String[] campos = linea.split("\\*", -1);  
                        if (campos.length != 3) {
                            throw new IllegalArgumentException("Linea mal formada: una mensualidad lleva tres campos (placa*inicio*fin)");
                        }
                        String placa = campos[0].trim();
                        LocalDate inicio = leerFecha(campos[1].trim());
                        LocalDate fin = leerFecha(campos[2].trim());
                        sistema.agregarMensualidad(new Mensualidad(placa, inicio, fin));
                        aceptadas++;
                    } catch (FechaInvalidaException e){
                        mensajes.add("Linea " + numeroLinea + ": FechaInvalidaException: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        mensajes.add("Linea " + numeroLinea + ": " + e.getMessage());
                    }
                }
            }
        }
        mensajes.add("Mensualidades cargadas: " + aceptadas);
          return mensajes;
    }


    public static List<String> procesarMovimientos(String ruta, IParqueadero sistema) throws IOException {
        List<String> mensajes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
              String linea;
              int numeroLinea = 0;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();
                if (!linea.isEmpty() && !linea.startsWith("#")) {
                    try {
                        String[] campos = linea.split("\\*", -1);
                          String movimiento = campos[0].trim();
                        if (movimiento.equals("E")) {
                            if (campos.length != 4) {
                                throw new IllegalArgumentException("Linea mal formada: una entrada lleva cuatro campos (E*placa*tipo*fecha hora)");
                            }
                            Vehiculo v = crearVehiculo(campos[2].trim(), campos[1].trim());
                            LocalDateTime entrada = leerFechaHora(campos[3].trim());  
                            Tiquete t = sistema.registrarEntrada(v, entrada);
                              mensajes.add("Linea " + numeroLinea + ": " + t);  
                        } else if (movimiento.equals("S")){
                            if (campos.length != 4){
                                throw new IllegalArgumentException("Linea mal formada: una salida lleva cuatro campos (S*placa*fecha hora*convenio)");
                            }

                            String placa = campos[1].trim();
                            LocalDateTime salida = leerFechaHora(campos[2].trim());
                            Convenio convenio = leerConvenio(campos[3].trim());
                           Tiquete t = sistema.registrarSalida(placa, salida, convenio);
                            mensajes.add("Linea " + numeroLinea + ": " + t);
                        } else {
                            throw new IllegalArgumentException("Linea mal formada: movimiento desconocido '" + movimiento + "', solo hay E y S");
                        }
                    } catch (PlacaInvalidaException e) {
                        mensajes.add("Linea " + numeroLinea + ": PlacaInvalidaException: " + e.getMessage());
                    } catch (VehiculoYaAdentroException e) {
                        mensajes.add("Linea " + numeroLinea + ": VehiculoYaAdentroException: " + e.getMessage());
                    } catch (VehiculoNoEncontradoException e) {
                         mensajes.add("Linea " + numeroLinea + ": VehiculoNoEncontradoException: " + e.getMessage());
                    } catch (FechaInvalidaException e) {
                        mensajes.add("Linea " + numeroLinea + ": FechaInvalidaException: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        mensajes.add("Linea " + numeroLinea + ": " + e.getMessage());
                    }
                }
            }
        }
        return mensajes;
    }


    public static void exportarReporte(List<String> lineas, String ruta) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            for (String linea : lineas) {
                writer.write(linea + "\n");
            }
        }
    }


    public static void guardarSistema(IParqueadero sistema, String ruta) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ruta))) {
            oos.writeObject(sistema);
        }
    }


    public static IParqueadero cargarSistema(String ruta) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ruta))) {
            return (IParqueadero) ois.readObject();
        }
    }


    public static Vehiculo crearVehiculo(String tipo, String placa) {
        switch (tipo) {
            case "CARRO":
                return new Carro(placa);
            case "MOTO":
                return new Moto(placa);
            case "ELECTRICO":
                return new CarroElectrico(placa);
            default:
                throw new IllegalArgumentException("Linea mal formada: tipo de vehiculo desconocido '" + tipo + "'");
        }
    }

   public static Convenio leerConvenio(String texto) {
        try {
           return Convenio.valueOf(texto);  
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Linea mal formada: convenio desconocido '" + texto + "'");
        }
    }

    public static LocalDateTime leerFechaHora(String texto) {
        try {
            LocalDateTime fecha = LocalDateTime.parse(texto, FORMATO_FECHA_HORA);
            if(!fecha.format(FORMATO_FECHA_HORA).equals(texto)) {
                throw new IllegalArgumentException("Linea mal formada: la fecha '" + texto + "' no existe");
            }
            return fecha;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Linea mal formada: fecha y hora invalida '" + texto + "', se espera yyyy-MM-dd HH:mm");
        }
    }

    public static LocalDate leerFecha(String texto) {
        try {
            LocalDate fecha = LocalDate.parse(texto, FORMATO_FECHA);
            if (!fecha.format(FORMATO_FECHA).equals(texto)){
                throw new IllegalArgumentException("Linea mal formada: la fecha '" + texto + "' no existe");
            }
            return fecha;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Linea mal formada: fecha invalida '" + texto + "', se espera yyyy-MM-dd");
        }
    }
}
