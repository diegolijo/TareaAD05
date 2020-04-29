package Objetos;

public class Archivo {

    private int id;
    private String nombre;
    private int directorioid;
    private byte[] bytes;

    public Archivo() {
    }

    public Archivo(int id, String nombre, int directorioid, byte[] bytes) {
        this.id = id;
        this.nombre = nombre;
        this.directorioid = directorioid;
        this.bytes = bytes;
    }



    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getDirectorioid() {
        return directorioid;
    }

    public byte[] getBytes() {
        return bytes;
    }

}
