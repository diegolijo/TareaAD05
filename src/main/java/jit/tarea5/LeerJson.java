package jit.tarea5;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class LeerJson {
    
    final static String SEPARADOR = File.separator;
    
    

    private static final String rutaJson = System.getProperty("user.dir") + SEPARADOR +"configuracion.json";
    
    

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
    
  

    
    //lee el json y devuelve la carpeta raiz
    public static LinkedTreeMap leerapp() {
        Gson gson = new Gson();
        LinkedTreeMap configuracion = null;
        File archivo = new File(rutaJson);
        if (archivo.exists()) {
            Map jsonJavaRootObject = gson.fromJson(leerString(archivo), Map.class);
            configuracion = (LinkedTreeMap) jsonJavaRootObject.get("app");
        }
        return configuracion;
    }

    //el metodo 'leerString' devulve  un String con el json
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

}
