package co.uniquindio.edu.co.Modelo;

public enum GeneroMusical {

    ROCK("Rock"),
    POP("Pop"),
    JAZZ("Jazz"),
    REGGAETON("Reggaeton"),
    ELECTRONICA("Electrónica"),
    CLASICA("Clásica"),
    HIP_HOP("Hip Hop"),
    SALSA("Salsa"),
    BACHATA("Bachata"),
    MERENGUE("Merengue"),
    BALADA("Balada"),
    COUNTRY("Country"),
    BLUES("Blues"),
    METAL("Metal"),
    PUNK("Punk"),
    INDIE("Indie"),
    ALTERNATIVA("Alternativa"),
    REGGAE("Reggae"),
    FOLK("Folk"),
    RNB("R&B"),
    SOUL("Soul"),
    FUNK("Funk"),
    CUMBIA("Cumbia"),
    VALLENATO("Vallenato"),
    OTRO("Otro");

    private final String nombre;

    GeneroMusical(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public static GeneroMusical fromString(String texto) {
        for (GeneroMusical genero : GeneroMusical.values()) {
            if (genero.nombre.equalsIgnoreCase(texto)) {
                return genero;
            }
        }
        return OTRO;
    }
}
