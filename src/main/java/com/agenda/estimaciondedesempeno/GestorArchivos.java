package com.agenda.estimaciondedesempeno;

import java.io.*;
import java.util.List;

public class GestorArchivos {

    // Guarda el CSV de una clase (ID del dato y su evaluacion)
    public static void guardarClase(String ruta, String clase, List<Dato> datos,
                                    List<String> evaluaciones) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {
            pw.println("ID-Dato,Evaluacion clase " + clase);
            for (int i = 0; i < datos.size(); i++) {
                pw.println(datos.get(i).getId() + "," + evaluaciones.get(i));
            }
        }
    }

    // Guarda sumas, metricas por clase y promedios generales
    public static void guardarResultados(String ruta, Evaluador e) throws IOException {
        String[] clases = e.getClases();

        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {
            pw.println("SUMAS POR CLASE");
            pw.println("Clase,Sum TP,Sum TN,Sum FP,Sum FN");
            for (int i = 0; i < clases.length; i++) {
                pw.println(clases[i] + "," + e.getTp(i) + "," + e.getTn(i) + ","
                        + e.getFp(i) + "," + e.getFn(i));
            }

            pw.println();
            pw.println("METRICAS POR CLASE");
            pw.println("Clase,Precision,Recall,Accuracy,F-Score");
            for (int i = 0; i < clases.length; i++) {
                pw.println(clases[i] + "," + Evaluador.redondear(e.precision(i)) + ","
                        + Evaluador.redondear(e.recall(i)) + ","
                        + Evaluador.redondear(e.accuracy(i)) + ","
                        + Evaluador.redondear(e.fScore(i)));
            }

            pw.println();
            pw.println("PROMEDIO GENERAL DEL MODELO");
            pw.println("Precision,Recall,Accuracy,F-Score");
            pw.println(Evaluador.redondear(e.promedioPrecision()) + ","
                    + Evaluador.redondear(e.promedioRecall()) + ","
                    + Evaluador.redondear(e.promedioAccuracy()) + ","
                    + Evaluador.redondear(e.promedioFScore()));
        }
    }

    // Guarda la matriz de confusion de N x N
    public static void guardarMatriz(String ruta, Evaluador e) throws IOException {
        String[] clases = e.getClases();
        int[][] m = e.getMatriz();

        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {
            StringBuilder encabezado = new StringBuilder("Real/Prediccion");
            for (String c : clases) {
                encabezado.append(",Clase ").append(c);
            }
            pw.println(encabezado);

            for (int i = 0; i < clases.length; i++) {
                StringBuilder fila = new StringBuilder("Clase " + clases[i]);
                for (int j = 0; j < clases.length; j++) {
                    fila.append(",").append(m[i][j]);
                }
                pw.println(fila);
            }
        }
    }
}
