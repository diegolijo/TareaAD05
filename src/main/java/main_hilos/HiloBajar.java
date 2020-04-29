/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main_hilos;

import static DB.DB.*;
import static Metodos.Metodos.*;
import Objetos.Archivo;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jit
 */
public class HiloBajar extends Thread {

    private Connection con;
    private org.postgresql.PGConnection pgcon;
    private File carpetaRaiz;


    public HiloBajar( Connection con) throws SQLException {
        this.con = con;
        this.pgcon = con.unwrap(org.postgresql.PGConnection.class);

        this.carpetaRaiz = leerAppJson();

        Statement stmt = con.createStatement();
        stmt.execute("LISTEN nuevo_mensaje");
        stmt.close();
    }

    @Override
    public void run() {
        try {
            while (true) {

                org.postgresql.PGNotification notifications[] = pgcon.getNotifications();

                //Si hay notificaciones
                if (notifications != null) {
                    for (int i = 0; i < notifications.length; i++) {
                        //Descargamos el archivo 
                        Archivo archivo = selectArchivoPorId(con, Integer.parseInt(notifications[i].getParameter()));
                        restaurarArchivo(con, archivo);
                    }
                }
                
                Thread.sleep(500);
            }
        } catch (Exception e) {
            Logger.getLogger(HiloSubir.class.getName()).log(Level.SEVERE, null, e);
        }
    }

}
