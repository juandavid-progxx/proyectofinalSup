package co.uniquindio.edu.co.Modelo;

public class Administrador extends Usuario {

    //Constructor de Administrador

    public Administrador(String username, String password, String nombre) {
        super(username, password, nombre);
    }

    //Verifica si el usuario es administrador

    public boolean esAdministrador() {
        return true;
    }

    @Override
    public String toString() {
        return getNombre() + " (@" + getUsername() + ") [ADMIN]";
    }
}
