package co.uniquindio.edu.co.Servicios;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JamendoAPI {

    private static final String CLIENT_ID = "9965752d";
    private static final String BASE_URL = "https://api.jamendo.com/v3.0/tracks/";

    public static List<CancionAPI> obtenerCancionesPopulares() {
        List<CancionAPI> canciones = new ArrayList<>();
        try {
            String urlStr = BASE_URL + "?client_id=" + CLIENT_ID
                    + "&format=json&limit=10&order=popularity_week";

            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream())
            );
            StringBuilder respuesta = new StringBuilder();
            String linea;
            while ((linea = in.readLine()) != null) {
                respuesta.append(linea);
            }
            in.close();

            JSONObject json = new JSONObject(respuesta.toString());
            JSONArray results = json.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);

                // Obtener duración en segundos de Jamendo
                int duracionSegundos = obj.getInt("duration");

                // Obtener año de la fecha de lanzamiento (formato: "YYYY-MM-DD")
                String releasedate = obj.getString("releasedate");
                int year = Integer.parseInt(releasedate.substring(0, 4));

                CancionAPI c = new CancionAPI(
                        obj.getString("name"),
                        obj.getString("artist_name"),
                        obj.getString("audio"),
                        duracionSegundos,
                        year
                );
                canciones.add(c);
            }

        } catch (Exception e) {
            System.out.println("Error al conectar con Jamendo:");
            e.printStackTrace();
        }

        return canciones;
    }

    public static class CancionAPI {
        private String titulo;
        private String artista;
        private String urlAudio;
        private int duracion; // en segundos
        private int year;

        public CancionAPI(String titulo, String artista, String urlAudio, int duracion, int year) {
            this.titulo = titulo;
            this.artista = artista;
            this.urlAudio = urlAudio;
            this.duracion = duracion;
            this.year = year;
        }

        public String getTitulo() { return titulo; }
        public String getArtista() { return artista; }
        public String getUrlAudio() { return urlAudio; }
        public int getDuracion() { return duracion; }
        public int getYear() { return year; }
    }
}
