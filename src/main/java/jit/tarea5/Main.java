package jit.tarea5;

import java.io.File;
import static jit.tarea5.BD.*;
import static jit.tarea5.Metodos.*;

public class Main {

    public static void main(String[] args) {

        //creamos las tabklas si no existen
        crearTablas();

        //buscamos directorio raiz
        File carpetaRaiz = appJson();

        //recorremos la estructura de carpetas
        if (carpetaRaiz.exists()) {
            insertarDirectorio(carpetaRaiz);
            recorrerDirectorios(carpetaRaiz.getPath());
            recorrerArchivos(carpetaRaiz.getPath(),conexionBDJson());
        }
        
        //
        restaurarDirectorios();
        //comprobamos archivos de la BD
        restaurarArchivos();
        
        // hilo para actualizar
       Escuchando escucha = new Escuchando(conexionBDJson());
        escucha.start();

    }

}
