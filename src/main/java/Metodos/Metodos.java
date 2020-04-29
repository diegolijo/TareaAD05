package Metodos;

import Objetos.Directorio;
import Objetos.Archivo;
import static DB.DB.*;
import com.google.gson.internal.LinkedTreeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class Metodos {

    final static String SEPARADOR = File.separator;

    private static final String rutaJson = System.getProperty("user.dir") + SEPARADOR + "configuracion.json";
    
    final static String raiz = leerAppJson().getAbsolutePath();

    //obtenemos ruta del archivo sin nombre
    public static String getRuta(File archivo) {
        String path = archivo.getAbsolutePath();
        int pos = path.lastIndexOf(SEPARADOR);
        path = path.substring(0, pos);
        return path;

    }

   //lee el json y devuelve la carpeta raiz
    public static File leerAppJson() {
        
        Gson gson = new Gson();
        LinkedTreeMap configuracion = null;
        File archivo = new File(rutaJson);
        if (archivo.exists()) {
            Map jsonJavaRootObject = gson.fromJson(leerString(archivo), Map.class);
            configuracion = (LinkedTreeMap) jsonJavaRootObject.get("app");
        }
        
        LinkedTreeMap app = configuracion;
       
        File file = new File(app.get("directory").toString());
        return file;
    }
    
    //devulve  un String con el json
    public static String leerString(File archivo) {
        String entrada = "";
        try {
            FileInputStream stream = new FileInputStream(archivo);
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            int caracter;
            while ((caracter = reader.read()) != -1) {
                entrada += (char) caracter;
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("No se ha podido leer el fichero");
        }
        return entrada;
    }
    

    // leemos directorio raiz inserta en la DB los directorios que no esten guardados
    public static void recorrerDirectorios(String path,Connection con) {

        File fileRaiz = new File(path);
        String[] dir = fileRaiz.list();

        for (int i = 0; i < dir.length; i++) {
            File archivo = new File(fileRaiz + SEPARADOR + dir[i]);

            //operaciones en cada directorio
            if (archivo.isDirectory()) {

                insertDirectorio(archivo,con);

                // llamamos a la recursiva para comprobar subcarpetas
                recorrerDirectorios(fileRaiz + SEPARADOR + dir[i],con);
            }
        }
    }

    // leemos directorio raiz e inserta en la DB los archivos que no esten guardados
    public static void recorrerArchivos(String path, Connection con) {

        File fileRaiz = new File(path);
        String[] dir = fileRaiz.list();

        for (int i = 0; i < dir.length; i++) {
            File archivo = new File(fileRaiz + SEPARADOR + dir[i]);

            //operaciones en cada archivo
            if (archivo.isFile()) {
                insertArchivo(archivo, con);
            }

            // llamamos a la recursiva para leer archivos dentro de la carpeta
            if (archivo.isDirectory()) {
                recorrerArchivos(fileRaiz + SEPARADOR + dir[i], con);
            }
        }
    }

    // lee DB y hace una copia local de los arquivos que no existan
    public static void restaurarArchivos(Connection con) {

        Archivo[] array = selectArchivos(con);

        for (int i = 0; i < array.length; i++) {
            
            String _path = selectPathPorId(array[i].getDirectorioid(),con);

            String ruta = raiz + _path.substring(1,_path.length()) + SEPARADOR + array[i].getNombre();
            
            

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

    // lee DB y hace una copia local de los directorios que no existan
    public static void restaurarDirectorios(Connection con) {

        Directorio[] array = selectDirectorios(con);

        for (int i = 0; i < array.length; i++) {

            String ruta = array[i].getNombre();
            ruta = raiz +  ruta.substring(1,ruta.length());

            if (!new File(ruta).exists()) {

                File file = new File(ruta);
                file.mkdirs();
            }
        }
    }  
        
    
    public static void restaurarArchivo(Connection con, Archivo archivo){
        
       String _path = selectPathPorId( archivo.getDirectorioid(), con); 
       String path = raiz + _path.substring(1, _path.length()) + SEPARADOR + archivo.getNombre() ;

            if (!new File(path).exists()) {

                try {
                    File file = new File(path);
                    byte[] bytes = archivo.getBytes();
                    OutputStream os = new FileOutputStream(file);
                    os.write(bytes);
                    os.close();

                } catch (IOException ex) {
                    Logger.getLogger(Metodos.class.getName()).log(Level.SEVERE, null, ex);
                }
            }       
        
    }

    //lee el json y devuelve los datos de conexion
    public static LinkedTreeMap leerdbConnection() {
        Gson gson = new Gson();
        LinkedTreeMap configuracion = null;
        //      InputStreamReader inStream =   new InputStreamReader(Main.class.getResourceAsStream( SEPARADOR + "configuracion.json"));
        File archivo = new File(rutaJson);
        if (archivo.exists()) {
            Map jsonJavaRootObject = gson.fromJson(leerString(archivo), Map.class);
            configuracion = (LinkedTreeMap) jsonJavaRootObject.get("dbConnection");
        }
        return configuracion;
    }





}
