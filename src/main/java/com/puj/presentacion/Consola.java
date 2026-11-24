package com.puj.presentacion;

import com.puj.excepciones.FechaInvalidaException;
import com.puj.excepciones.PlacaInvalidaException;
import com.puj.excepciones.VehiculoNoEncontradoException;  
import com.puj.excepciones.VehiculoYaAdentroException;
import com.puj.modelo.Convenio;
import com.puj.modelo.Tiquete;
import com.puj.modelo.Vehiculo;
import com.puj.negocio.IParqueadero;
import com.puj.negocio.Parqueadero;
import com.puj.persistencia.ManejoArchivos;

import java.io.BufferedReader;  
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Consola {

    private static final String ARCHIVO_SISTEMA = "sistema.dat";

    private static IParqueadero sistema = new Parqueadero();
    private static BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            opcion = leerOpcion();
            switch (opcion){
                case 1:
                      cargarMensualidades();
                    break;  
                case 2:
                    procesarMovimientos();
                    break;
                case 3:
                    registrarEntrada();
                    break;  
                case 4:
                    registrarSalida();
                    break;  
                case 5:
                    verVehiculosAdentro();
                    break;
                case 6:
                    cierreDeCaja();
                    break;
                case 7:
                    placasFrecuentes();
                    break;
                case 8:
                    guardarSistema();
                    break;
                case 9:
                    cargarSistema();
                    break;
                case 0:
                    System.out.println("Hasta luego.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("===== PARQUEADERO PLAZA CENTRAL =====");
        System.out.println("1. Cargar mensualidades");
          System.out.println("2. Procesar movimientos");
        System.out.println("3. Registrar una entrada");
        System.out.println("4. Registrar una salida");
        System.out.println("5. Ver los vehiculos adentro");
        System.out.println("6. Cierre de caja de un dia");
        System.out.println("7. Placas frecuentes");
        System.out.println("8. Guardar el sistema");  
        System.out.println("9. Cargar el sistema");
        System.out.println("0. Salir");
        System.out.print("Opcion: ");
    }



    private static String pedir(String mensaje) {
        System.out.print(mensaje);
        try {
            String texto = lector.readLine();
            if (texto == null) {
                return "";
            }
            return texto.trim();
        } catch (IOException e) {
            System.out.println("Error al leer del teclado: " + e.getMessage());
            return "";
        }
    }

    private static int leerOpcion() {
        try {
            String texto = lector.readLine();
            if (texto == null) {
                return 0;  
            }
            return Integer.parseInt(texto.trim());
        } catch (IOException e) {
            System.out.println("Error al leer del teclado: " + e.getMessage());
            return 0;  
        } catch (NumberFormatException e) {
            return -1;
        }
    }

   private static void imprimir(List<String> lineas) {
        for (String linea : lineas){
            System.out.println(linea);
        }
    }


    private static void cargarMensualidades() {
        String carpeta = pedir("Carpeta de los archivos: ");
        String ruta = carpeta + "/Mensualidades.txt";
        try {
            imprimir(ManejoArchivos.cargarMensualidades(ruta, sistema));
        } catch (IOException e) {
            System.out.println("No se pudo leer " + ruta + ": " + e.getMessage());
        }
    }

    private static void procesarMovimientos() {
        String carpeta = pedir("Carpeta de los archivos: ");
        String ruta = carpeta + "/Movimientos.txt";  
        try {
            imprimir(ManejoArchivos.procesarMovimientos(ruta, sistema));
        } catch (IOException e) {
            System.out.println("No se pudo leer " + ruta + ": " + e.getMessage());
        }
    }

    private static void registrarEntrada() {
        try {
            String placa = pedir("Placa: ");
            String tipo = pedir("Tipo (CARRO, MOTO, ELECTRICO): ").toUpperCase();
              Vehiculo v = ManejoArchivos.crearVehiculo(tipo, placa);
            LocalDateTime entrada = ManejoArchivos.leerFechaHora(pedir("Fecha y hora de entrada (yyyy-MM-dd HH:mm): "));
            Tiquete t = sistema.registrarEntrada(v, entrada);
             System.out.println("Entrada registrada: " + t);
        } catch (PlacaInvalidaException e) {
            System.out.println("Rechazado (PlacaInvalidaException): " + e.getMessage());
        } catch (VehiculoYaAdentroException e) {
              System.out.println("Rechazado (VehiculoYaAdentroException): " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Dato invalido: " + e.getMessage());
        }

    }

    private static void registrarSalida() {
        try {
            String placa = pedir("Placa: ");
            LocalDateTime salida = ManejoArchivos.leerFechaHora(pedir("Fecha y hora de salida (yyyy-MM-dd HH:mm): "));
            Convenio convenio = ManejoArchivos.leerConvenio(
                    pedir("Convenio (NINGUNO, ALMACEN, RESTAURANTE, CINE): ").toUpperCase());
            Tiquete t = sistema.registrarSalida(placa, salida, convenio);
            System.out.println("Salida registrada: " + t);
            System.out.println("  Minutos:  " + t.getMinutos());
            System.out.println("  Parqueo:  " + t.getValorParqueo());
            System.out.println("  Recarga:  " + t.getValorRecarga());
            System.out.println("  TOTAL:    " + t.getTotal());
        } catch (VehiculoNoEncontradoException e) {
            System.out.println("Rechazado (VehiculoNoEncontradoException): " + e.getMessage());
        } catch (FechaInvalidaException e) {
            System.out.println("Rechazado (FechaInvalidaException): " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Dato invalido: " + e.getMessage());
        }

    }

    private static void verVehiculosAdentro() {
        List<String> lineas = sistema.reporteVehiculosAdentro();
        System.out.println("Vehiculos adentro");
        if (lineas.isEmpty()) {
            System.out.println("(no hay vehiculos adentro)");
        }
        imprimir(lineas);
    }

    private static void cierreDeCaja() {
        try {
            LocalDate fecha = ManejoArchivos.leerFecha(pedir("Fecha del cierre (yyyy-MM-dd): "));
            List<String> lineas = sistema.reporteCierre(fecha);
            imprimir(lineas);  
            String archivo = "Cierre_" + fecha + ".txt";
            ManejoArchivos.exportarReporte(lineas, archivo);  
            System.out.println("Cierre exportado a " + archivo);
        } catch (IllegalArgumentException e) {
           System.out.println("Dato invalido: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("No se pudo exportar el cierre: " + e.getMessage());
        }
    }

    private static void placasFrecuentes() {
        List<String> lineas = sistema.reportePlacasFrecuentes();
        System.out.println("Placas frecuentes");
        if(lineas.isEmpty()) {
            System.out.println("(no hay tiquetes cerrados)");
        }

        imprimir(lineas);
    }

   private static void guardarSistema() {
        try {
            ManejoArchivos.guardarSistema(sistema, ARCHIVO_SISTEMA);
            System.out.println("Sistema guardado en " + ARCHIVO_SISTEMA);
        } catch (IOException e) {
            System.out.println("No se pudo guardar el sistema: " + e.getMessage());
        }
    }

     private static void cargarSistema() {
       try {
            sistema = ManejoArchivos.cargarSistema(ARCHIVO_SISTEMA);
            System.out.println("Sistema recuperado de " + ARCHIVO_SISTEMA);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No se pudo cargar el sistema: " + e.getMessage());
        }
    }

}
