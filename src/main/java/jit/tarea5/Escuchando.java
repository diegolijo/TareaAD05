package jit.tarea5;

import java.io.File;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jit.tarea5.BD.*;
import static jit.tarea5.Metodos.*;

public class Escuchando extends Thread {

    Connection con;

    public Escuchando(Connection con) {
        this.con = con;
    }

    @Override
    public void run() {

        File carpetaRaiz = appJson();

        try {
            while (true) {
                System.out.println("jit.tarea5.Escuchando.run()");
                
                recorrerArchivos(carpetaRaiz.getPath(), conexionBDJson());

                Thread.sleep(2000);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Escuchando.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
