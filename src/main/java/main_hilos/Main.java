package main_hilos;

import static DB.DB.*;
import Metodos.*;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {

        try {
            Metodos metodos = new Metodos();

            Connection con = conexionBD();

            //creamos las tabklas si no existen
            crearTablas(con);

            //buscamos directorio raiz
            File carpetaRaiz = metodos.leerAppJson();

            //recorremos la estructura de carpetas
            if (!carpetaRaiz.exists()) {
                carpetaRaiz.mkdir();
            }

            insertDirectorio(carpetaRaiz, con);
            metodos.recorrerDirectorios(carpetaRaiz.getPath(), con);
            metodos.recorrerArchivos(carpetaRaiz.getPath(), con);

            //
            metodos.restaurarDirectorios(con);
            //comprobamos archivos de la DB
            metodos.restaurarArchivos(con);

            crearFuncion(con);
            crearTrigger(con);

            desconexionDB(con);

            Connection conS = conexionBD();
            HiloSubir hiloSubir = new HiloSubir(conS);
            hiloSubir.start();

            Connection conB = conexionBD();
            HiloBajar hiloBajar = new HiloBajar(conB);
            hiloBajar.start();

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
