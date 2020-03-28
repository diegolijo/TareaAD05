package jit.tarea5;

import com.google.gson.internal.LinkedTreeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jit.tarea5.BD.*;
import static jit.tarea5.LeerJson.leerapp;

public class Metodos {

    final static String SEPARADOR = File.separator;

    //obtenemos ruta del archivo sin nombre
    public static String getRuta(File archivo) {
        String path = archivo.getAbsolutePath();
        int pos = path.lastIndexOf(SEPARADOR);
        path = path.substring(0, pos);
        return path;

    }

    public static File appJson() {
        LinkedTreeMap app = leerapp();
        File file = new File(app.get("directory").toString());
        return file;
    }

    // leemos directorio raiz
    public static void recorrerDirectorios(String path) {

        File fileRaiz = new File(path);
        String[] dir = fileRaiz.list();

        for (int i = 0; i < dir.length; i++) {
            File archivo = new File(fileRaiz + SEPARADOR + dir[i]);

            //operaciones en cada directorio
            if (archivo.isDirectory()) {

                insertarDirectorio(archivo);

                // llamamos a la recursiva para comprobar subcarpetas
                recorrerDirectorios(fileRaiz + SEPARADOR + dir[i]);
            }
        }
    }

    // leemos directorio raiz
    public static void recorrerArchivos(String path,Connection con) {

        File fileRaiz = new File(path);
        String[] dir = fileRaiz.list();

        for (int i = 0; i < dir.length; i++) {
            File archivo = new File(fileRaiz + SEPARADOR + dir[i]);

            //operaciones en cada archivo
            if (archivo.isFile()) {
                insertarArchivo(archivo,con);
            }

            // llamamos a la recursiva para leer archivos dentro de la carpeta
            if (archivo.isDirectory()) {
                recorrerArchivos(fileRaiz + SEPARADOR + dir[i],con);
            }
        }
    }

    public static void restaurarArchivos() {

        Archivo[] array = seleccionarArchivos();

        for (int i = 0; i < array.length; i++) {

            String ruta = selectPathPorId(array[i].getDirectorioid()) + SEPARADOR + array[i].getNombre();

            if (!new File(ruta).exists()) {
                
                try {                                       
                    File file = new File(ruta);
                    byte[] bytes = array[i].getBytes();
                    OutputStream os = new FileOutputStream(file);
                    os.write(bytes);
                    os.close();

                } catch (IOException ex) {
                    Logger.getLogger(Metodos.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    
        public static void restaurarDirectorios() {

        Directorio[] array = seleccionarDirectorios();

        for (int i = 0; i < array.length; i++) {

            String ruta = array[i].getNombre();

            if (!new File(ruta).exists()) {
                
                File file = new File(ruta);
                file.mkdirs();
            }
        }
    }

}
