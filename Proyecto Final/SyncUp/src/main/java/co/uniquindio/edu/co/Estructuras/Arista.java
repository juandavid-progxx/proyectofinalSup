package co.uniquindio.edu.co.Estructuras;

import co.uniquindio.edu.co.Modelo.Cancion;

public class Arista {

    private Cancion origen;
    private Cancion destino;
    private double peso; // Peso de la arista (similitud entre canciones)

    /**
     * Constructor de Arista
     */
    public Arista(Cancion origen, Cancion destino, double peso) {
        this.origen = origen;
        this.destino = destino;
        this.peso = peso;
    }

    public Cancion getOrigen() {
        return origen;
    }

    public void setOrigen(Cancion origen) {
        this.origen = origen;
    }

    public Cancion getDestino() {
        return destino;
    }

    public void setDestino(Cancion destino) {
        this.destino = destino;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    @Override
    public String toString() {
        return origen.getTitulo() + " <-> " + destino.getTitulo() + " (similitud: " + peso + ")";
    }
}
