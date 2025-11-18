package co.uniquindio.edu.co.Estructuras;

import java.util.*;

public class BFS {

    /**
     * Realiza un recorrido BFS desde un usuario dado
     * @param grafo Grafo social
     * @param usuarioInicio Username del usuario de inicio
     * @return Lista de usuarios en orden de recorrido BFS
     */
    public static List<String> recorridoBFS(GrafoSocial grafo, String usuarioInicio) {
        List<String> recorrido = new ArrayList<>();

        if (!grafo.contieneUsuario(usuarioInicio)) {
            return recorrido;
        }

        Set<String> visitados = new HashSet<>();
        Queue<String> cola = new LinkedList<>();

        cola.offer(usuarioInicio);
        visitados.add(usuarioInicio);

        while (!cola.isEmpty()) {
            String usuarioActual = cola.poll();
            recorrido.add(usuarioActual);

            Set<String> amigos = grafo.obtenerAmigos(usuarioActual);
            for (String amigo : amigos) {
                if (!visitados.contains(amigo)) {
                    visitados.add(amigo);
                    cola.offer(amigo);
                }
            }
        }

        return recorrido;
    }

    /**
     * Encuentra amigos de amigos (usuarios a 2 grados de separación)
     * @param grafo Grafo social
     * @param username Username del usuario
     * @return Set de usernames de amigos (excluyendo amigos directos y el usuario mismo)
     */
    public static Set<String> encontrarAmigosDeAmigos(GrafoSocial grafo, String username) {
        Set<String> amigosDeAmigos = new HashSet<>();

        if (!grafo.contieneUsuario(username)) {
            return amigosDeAmigos;
        }

        Set<String> amigosDirectos = grafo.obtenerAmigos(username);

        // Por cada amigo directo, obtener sus amigos
        for (String amigo : amigosDirectos) {
            Set<String> amigosDelAmigo = grafo.obtenerAmigos(amigo);
            for (String amigoDelAmigo : amigosDelAmigo) {
                // No incluir al usuario mismo ni a sus amigos directos
                if (!amigoDelAmigo.equals(username) && !amigosDirectos.contains(amigoDelAmigo)) {
                    amigosDeAmigos.add(amigoDelAmigo);
                }
            }
        }

        return amigosDeAmigos;
    }

    /**
     * Encuentra sugerencias de usuarios para seguir basado en amigos en común
     * @param grafo Grafo social
     * @param username Username del usuario
     * @param limite Número máximo de sugerencias
     * @return Lista de usernames sugeridos ordenados por número de amigos en común
     */
    public static List<String> obtenerSugerencias(GrafoSocial grafo, String username, int limite) {
        if (!grafo.contieneUsuario(username)) {
            return new ArrayList<>();
        }

        Set<String> amigosDirectos = grafo.obtenerAmigos(username);
        Map<String, Integer> conteoAmigosComunes = new HashMap<>();

        // Contar amigos en común para cada amigo de amigo
        for (String amigo : amigosDirectos) {
            Set<String> amigosDelAmigo = grafo.obtenerAmigos(amigo);
            for (String candidato : amigosDelAmigo) {
                if (!candidato.equals(username) && !amigosDirectos.contains(candidato)) {
                    conteoAmigosComunes.put(candidato,
                            conteoAmigosComunes.getOrDefault(candidato, 0) + 1);
                }
            }
        }

        // Ordenar por número de amigos en común (descendente)
        List<Map.Entry<String, Integer>> sugerencias = new ArrayList<>(conteoAmigosComunes.entrySet());
        sugerencias.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Retornar solo el límite especificado
        List<String> resultado = new ArrayList<>();
        int count = Math.min(limite, sugerencias.size());
        for (int i = 0; i < count; i++) {
            resultado.add(sugerencias.get(i).getKey());
        }

        return resultado;
    }

    /**
     * Calcula la distancia (grados de separación) entre dos usuarios
     * @param grafo Grafo social
     * @param usuario1 Username del primer usuario
     * @param usuario2 Username del segundo usuario
     * @return Distancia entre usuarios, -1 si no hay conexión
     */
    public static int calcularDistancia(GrafoSocial grafo, String usuario1, String usuario2) {
        if (!grafo.contieneUsuario(usuario1) || !grafo.contieneUsuario(usuario2)) {
            return -1;
        }

        if (usuario1.equals(usuario2)) {
            return 0;
        }

        Set<String> visitados = new HashSet<>();
        Queue<ParUsuarioDistancia> cola = new LinkedList<>();

        cola.offer(new ParUsuarioDistancia(usuario1, 0));
        visitados.add(usuario1);

        while (!cola.isEmpty()) {
            ParUsuarioDistancia actual = cola.poll();
            String usuarioActual = actual.usuario;
            int distanciaActual = actual.distancia;

            Set<String> amigos = grafo.obtenerAmigos(usuarioActual);
            for (String amigo : amigos) {
                if (amigo.equals(usuario2)) {
                    return distanciaActual + 1;
                }

                if (!visitados.contains(amigo)) {
                    visitados.add(amigo);
                    cola.offer(new ParUsuarioDistancia(amigo, distanciaActual + 1));
                }
            }
        }

        return -1; // No hay conexión
    }

    /**
     * Encuentra el camino más corto entre dos usuarios
     * @param grafo Grafo social
     * @param origen Username del usuario origen
     * @param destino Username del usuario destino
     * @return Lista de usernames que forman el camino, vacía si no hay camino
     */
    public static List<String> encontrarCamino(GrafoSocial grafo, String origen, String destino) {
        if (!grafo.contieneUsuario(origen) || !grafo.contieneUsuario(destino)) {
            return new ArrayList<>();
        }

        if (origen.equals(destino)) {
            return Arrays.asList(origen);
        }

        Set<String> visitados = new HashSet<>();
        Queue<String> cola = new LinkedList<>();
        Map<String, String> predecesores = new HashMap<>();

        cola.offer(origen);
        visitados.add(origen);
        predecesores.put(origen, null);

        boolean encontrado = false;

        while (!cola.isEmpty() && !encontrado) {
            String actual = cola.poll();

            Set<String> amigos = grafo.obtenerAmigos(actual);
            for (String amigo : amigos) {
                if (!visitados.contains(amigo)) {
                    visitados.add(amigo);
                    predecesores.put(amigo, actual);
                    cola.offer(amigo);

                    if (amigo.equals(destino)) {
                        encontrado = true;
                        break;
                    }
                }
            }
        }

        // Reconstruir el camino
        if (!encontrado) {
            return new ArrayList<>();
        }

        List<String> camino = new ArrayList<>();
        String actual = destino;

        while (actual != null) {
            camino.add(0, actual);
            actual = predecesores.get(actual);
        }

        return camino;
    }

    /**
     * Clase auxiliar para almacenar usuario y su distancia
     */
    private static class ParUsuarioDistancia {
        String usuario;
        int distancia;

        ParUsuarioDistancia(String usuario, int distancia) {
            this.usuario = usuario;
            this.distancia = distancia;
        }
    }
}
