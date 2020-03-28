package jit.tarea5;

import com.google.gson.internal.LinkedTreeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jit.tarea5.LeerJson.leerdbConnection;

import static jit.tarea5.Metodos.*;

public class BD {

    final static String SEPARADOR = File.separator;

    public static Connection conexionBDJson() {
        LinkedTreeMap configuracion = leerdbConnection();

        //URL e base de datos a cal nos conectamos
        String url = configuracion.get("address").toString();
        String db = configuracion.get("name").toString();

        //Indicamos as propiedades da conexión
        Properties props = new Properties();
        props.setProperty("user", configuracion.get("user").toString());
        props.setProperty("password", configuracion.get("password").toString());

        //Dirección de conexión a base de datos
        String postgres = "jdbc:postgresql://" + url + "/" + db;

        //Conectamos a base de datos
        try {
            Connection conn = DriverManager.getConnection(postgres, props);

            return conn;

        } catch (SQLException ex) {
            System.err.println("Error: " + ex.toString());
            return null;
        }
    }

    public static void crearTablas() {

        try {
            Connection conn = conexionBDJson();
            String sqlTablaDirectorios
                    = "CREATE TABLE IF NOT EXISTS directorios "
                    + "(id SERIAL PRIMARY KEY, "
                    + "nombre VARCHAR(200) UNIQUE NOT NULL);";
            //Executamos a sentencia SQL 
            CallableStatement createFunction = conn.prepareCall(sqlTablaDirectorios);
            createFunction.execute();

            String sqlTablaArchivos
                    = "CREATE TABLE IF NOT EXISTS archivos (id SERIAL PRIMARY KEY,"
                    + "nombre VARCHAR(200) UNIQUE NOT NULL, "
                    + "directorioid  INTEGER  REFERENCES directorios(id),"
                    + "archivo BYTEA NOT NULL);";
            //Executamos a sentencia SQL 
            createFunction = conn.prepareCall(sqlTablaArchivos);
            createFunction.execute();
            createFunction.close();

            //Cerramos a conexión coa base de datos
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertarDirectorio(File carpeta) {

        try {
            if (existeDirEnBD(carpeta) != true) {
                //consulta que inserta a carpeta en la DB 
                String sqlInsert = new String(
                        "INSERT INTO directorios (nombre) VALUES (?);");
                PreparedStatement ps = conexionBDJson().prepareStatement(sqlInsert);

                ps.setString(1, carpeta.getAbsolutePath().toString());
                ps.executeUpdate();

                System.out.println("directorio insertado -> " + carpeta);
               
                ps.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertarArchivo(File archivo,Connection con) {

        try {
            if (existeArcEnBD(archivo) != true) {

                String path = getRuta(archivo);

                // consultamos el id de esa ruta en tabla directorios
                int idPath = selectIdPorPath(path);

                FileInputStream fiStream = new FileInputStream(archivo);

                //Creamos a consulta que inserta a imaxe na base de datos
                String sqlInsert
                        = "INSERT INTO archivos (nombre,directorioid,archivo) VALUES (?,?,?);";
                PreparedStatement ps = con.prepareStatement(sqlInsert);

                ps.setString(1, archivo.getName());
                ps.setInt(2, idPath);
                ps.setBinaryStream(3, fiStream, (int) archivo.length());

                //Executamos a consulta
                ps.executeUpdate();

                System.out.println("archivo insertado -> " + archivo.getName());

                //Cerrramos a consulta e o arquivo aberto
                ps.close();

            }
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean existeDirEnBD(File file) {
        boolean existe = false;

        try {
            String sql = "SELECT * FROM directorios WHERE nombre = ?";
            PreparedStatement ps = conexionBDJson().prepareStatement(sql);

            ps.setString(1, file.getAbsolutePath());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                existe = true;
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }

    public static boolean existeArcEnBD(File file) {
        boolean existe = false;
        int id = selectIdPorPath(getRuta(file));

        try {
            String sql = "SELECT * FROM archivos WHERE nombre = ? AND directorioid = ?";
            PreparedStatement ps = conexionBDJson().prepareStatement(sql);

            ps.setString(1, file.getName());
            ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                existe = true;
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }

    public static int selectIdPorPath(String path) {
        int id = 0;

        try {
            String sql = "SELECT id FROM directorios WHERE nombre = ?;";
            PreparedStatement ps = conexionBDJson().prepareStatement(sql);

            ps.setString(1, path);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                id = rs.getInt(1);
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    //devuelve el numero de archivos en la BD
    public static int contarArcEnBD() {
        int numArchivos = 0;

        try {
            String sql = "SELECT COUNT(*) FROM archivos";
            PreparedStatement ps = conexionBDJson().prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                numArchivos = rs.getInt(1);
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numArchivos;
    }

    //devuelve el numero de archivos en la BD
    public static int contarDirEnBD() {
        int numDIrectorios = 0;

        try {
            String sql = "SELECT COUNT(*) FROM directorios";
            PreparedStatement ps = conexionBDJson().prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                numDIrectorios = rs.getInt(1);
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numDIrectorios;
    }

    public static String selectPathPorId(int id) {
        String path = null;

        try {
            String sql = "SELECT nombre FROM directorios WHERE id = ?;";
            PreparedStatement ps = conexionBDJson().prepareStatement(sql);

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                path = rs.getString(1);
            }

            rs.close();
            ps.close();

        } catch (Exception ex) {
            System.out.println("Venezuela");
        }
        return path;
    }

    public static Archivo[] seleccionarArchivos() {

        int numArchivos = contarArcEnBD();

        Archivo[] array = new Archivo[numArchivos];

        try {
            String sql = "SELECT * FROM archivos";
            PreparedStatement ps = conexionBDJson().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int cont = 0;
            while (rs.next()) {

                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                int directorioid = rs.getInt("directorioid");
                byte[] bytes = rs.getBytes("archivo");
                array[cont] = new Archivo(id, nombre, directorioid, bytes);

                cont += 1;
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        return array;
    }

    public static Directorio[] seleccionarDirectorios() {

        int numDirectorios = contarDirEnBD();

        Directorio[] array = new Directorio[numDirectorios];

        try {
            String sql = "SELECT * FROM directorios";
            PreparedStatement ps = conexionBDJson().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int cont = 0;
            while (rs.next()) {

                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");

                array[cont] = new Directorio(id, nombre);

                cont += 1;
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        return array;
    }

}
