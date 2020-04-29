package DB;

import Objetos.Archivo;

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

import static Metodos.Metodos.*;
import Objetos.Directorio;

public class DB {

    final static String raiz = leerAppJson().getAbsolutePath();

    final static String SEPARADOR = File.separator;

    public static Connection conexionBD() {
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

    public static void desconexionDB(Connection con) {
        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void crearTablas(Connection con) {

        try {

            String sqlTablaDirectorios
                    = "CREATE TABLE IF NOT EXISTS directorios "
                    + "(id SERIAL PRIMARY KEY, "
                    + "nombre VARCHAR(200) UNIQUE NOT NULL);";
            //Executamos a sentencia SQL 
            CallableStatement createFunction = con.prepareCall(sqlTablaDirectorios);
            createFunction.execute();

            String sqlTablaArchivos
                    = "CREATE TABLE IF NOT EXISTS archivos (id SERIAL PRIMARY KEY,"
                    + "nombre VARCHAR(200) NOT NULL, "
                    + "directorioid  INTEGER  REFERENCES directorios(id),"
                    + "archivo BYTEA NOT NULL);";
            //Executamos a sentencia SQL 
            createFunction = con.prepareCall(sqlTablaArchivos);
            createFunction.execute();
            createFunction.close();

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertDirectorio(File carpeta, Connection con) {

        String _path = carpeta.getAbsolutePath();
        _path = "." + _path.substring(raiz.length(), _path.length());

        try {
            if (existeDirEnBD(_path, con) != true) {
                //consulta que inserta a carpeta en la DB 
                String sqlInsert = new String(
                        "INSERT INTO directorios (nombre) VALUES (?);");
                PreparedStatement ps = con.prepareStatement(sqlInsert);

                ps.setString(1, _path);
                ps.executeUpdate();

                System.out.println("directorio insertado -> " + carpeta);

                ps.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertArchivo(File archivo, Connection con) {

        try {
            if (existeArcEnBD(archivo, con) != true) {

                String path = getRuta(archivo);

                // consultamos el id de esa ruta en tabla directorios
                int idPath = selectIdPorPath(path, con);

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
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static int selectIdPorPath(String path, Connection con) {
        int id = 0;

        String _path = "." + path.substring(raiz.length(), path.length());

        try {
            String sql = "SELECT id FROM directorios WHERE nombre = ?;";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, _path);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                id = rs.getInt(1);
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    public static String selectPathPorId(int id, Connection con) {
        String path = null;

        try {
            String sql = "SELECT nombre FROM directorios WHERE id = ?;";
            PreparedStatement ps = con.prepareStatement(sql);

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

    public static Archivo[] selectArchivos(Connection con) {

        int numArchivos = countArchivos(con);

        Archivo[] array = new Archivo[numArchivos];

        try {
            String sql = "SELECT * FROM archivos";
            PreparedStatement ps = con.prepareStatement(sql);
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
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return array;
    }

    public static Archivo selectArchivoPorId(Connection con, int id) {

        Archivo file = new Archivo();

        try {
            String sql = "SELECT * FROM archivos WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String nombre = rs.getString("nombre");
                int directorioid = rs.getInt("directorioid");
                byte[] bytes = rs.getBytes("archivo");
                file = new Archivo(id, nombre, directorioid, bytes);

            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return file;
    }

    public static Directorio[] selectDirectorios(Connection con) {

        int numDirectorios = countDir(con);

        Directorio[] array = new Directorio[numDirectorios];

        try {
            String sql = "SELECT * FROM directorios";
            PreparedStatement ps = con.prepareStatement(sql);
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
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return array;
    }

    //devuelve el numero de archivos en la DB
    public static int countArchivos(Connection con) {
        int numArchivos = 0;

        try {
            String sql = "SELECT COUNT(*) FROM archivos";
            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                numArchivos = rs.getInt(1);
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numArchivos;
    }

    //devuelve el numero de dir en la DB
    public static int countDir(Connection con) {
        int numDIrectorios = 0;

        try {
            String sql = "SELECT COUNT(*) FROM directorios";
            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                numDIrectorios = rs.getInt(1);
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numDIrectorios;
    }

    //comprueba si existe carpeta en la DB
    public static boolean existeDirEnBD(String nombre, Connection con) {
        boolean existe = false;

        try {

            String sql = "SELECT * FROM directorios WHERE nombre = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                existe = true;
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }

    //comprueba si existe archivo en la DB
    public static boolean existeArcEnBD(File file, Connection con) {
        boolean existe = false;
        int id = selectIdPorPath(getRuta(file), con);

        try {
            String sql = "SELECT * FROM archivos WHERE nombre = ? AND directorioid = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, file.getName());
            ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                existe = true;
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }

    public static void crearFuncion(Connection con) {
        //Creamos a función que notificará que se engadiu unha nova mensaxe
        try {
            String sql = new String(
                    "CREATE OR REPLACE FUNCTION notificar_mensaje() "
                    + "RETURNS trigger AS $$ "
                    + "BEGIN "
                    + "PERFORM pg_notify('nuevo_mensaje',NEW.id::text); "
                    + "RETURN NEW; "
                    + "END; "
                    + "$$ LANGUAGE plpgsql; ");

            CallableStatement createFunction = con.prepareCall(sql);
            createFunction.execute();
            createFunction.close();

        } catch (SQLException e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    public static void crearTrigger(Connection con) {
        //Creamos o triguer que se executa tras unha nova mensaxe
        String sql = new String(
                "DROP TRIGGER IF EXISTS not_nuevo_mensaje ON archivos; "
                + "CREATE TRIGGER not_nuevo_mensaje "
                + "AFTER INSERT "
                + "ON archivos "
                + "FOR EACH ROW "
                + "EXECUTE PROCEDURE notificar_mensaje(); ");
        try {
            CallableStatement createTrigger = con.prepareCall(sql);
            createTrigger.execute();
            createTrigger.close();
        } catch (SQLException e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
        }
    }

}
