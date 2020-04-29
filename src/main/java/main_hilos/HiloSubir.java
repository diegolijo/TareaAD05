package main_hilos;

import static Metodos.Metodos.*;
import java.io.File;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.SQLException;

public class HiloSubir extends Thread {

    private Connection con;
    private File carpetaRaiz;

    HiloSubir(Connection con) throws SQLException {

        this.con = con;

        this.carpetaRaiz = leerAppJson();

    }

    public void run() {
        try {
            while (true) {

                if (carpetaRaiz.exists()) {
                    recorrerDirectorios(carpetaRaiz.getPath(), con);
                    recorrerArchivos(carpetaRaiz.getPath(), con);
                }
                Thread.sleep(5000);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(HiloSubir.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
