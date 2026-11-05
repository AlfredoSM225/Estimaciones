package com.agenda.estimaciondedesempeno;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LectorCSV {

    public static List<Dato> leer(String ruta, boolean tieneEncabezado) throws IOException {
        List<Dato> datos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int numLinea = 0;

            while ((linea = br.readLine()) != null) {
                numLinea++;

                // Saltar lineas vacias y el encabezado
                if (linea.trim().isEmpty()) continue;
                if (tieneEncabezado && numLinea == 1) continue;

                // Separa por coma o punto y coma
                String[] partes = linea.split("[,;]");
                if (partes.length != 3) {
                    throw new IOException("La línea " + numLinea + " no tiene 3 columnas.");
                }
                datos.add(new Dato(partes[0].trim(), partes[1].trim(), partes[2].trim()));
            }
        }

        if (datos.isEmpty()) {
            throw new IOException("El archivo no contiene datos.");
        }
        return datos;
    }
}
