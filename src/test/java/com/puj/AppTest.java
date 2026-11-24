package com.puj;

import com.puj.modelo.CarroElectrico;
import com.puj.modelo.Convenio;
import com.puj.modelo.Tiquete;
import com.puj.persistencia.ManejoArchivos;

public class AppTest {

    public static void main(String[] args) {
        Tiquete t = new Tiquete(1, new CarroElectrico("DEF456"), ManejoArchivos.leerFechaHora("2026-03-02 07:30"));  
        t.cerrar(ManejoArchivos.leerFechaHora("2026-03-02 10:05"), Convenio.ALMACEN, false);
        if(t.getTotal() == 21250){
            System.out.println("Prueba correcta: total = " + t.getTotal());
        } else {
            System.out.println("Prueba fallida: se esperaba 21250 y dio " + t.getTotal());
        }
    }
}
