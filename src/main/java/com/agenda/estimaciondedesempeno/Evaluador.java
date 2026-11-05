package com.agenda.estimaciondedesempeno;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Evaluador {
    private String[] clases;
    private int[] tp, tn, fp, fn;
    private int[][] matriz;                       // matriz[real][prediccion]
    private List<List<String>> evaluaciones;      // resultado de cada dato, por clase

    public Evaluador(String[] clases) {
        this.clases = clases;
        int n = clases.length;
        tp = new int[n];
        tn = new int[n];
        fp = new int[n];
        fn = new int[n];
        matriz = new int[n][n];
        evaluaciones = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            evaluaciones.add(new ArrayList<>());
        }
    }

    // Evalua un dato contra cada clase y devuelve TP, TN, FP o FN por clase
    public String[] evaluarDato(Dato d) {
        int n = clases.length;
        String[] resultados = new String[n];
        int real = indiceDe(d.getClaseVerdadera());
        int pred = indiceDe(d.getClasePredicha());

        matriz[real][pred]++;

        for (int i = 0; i < n; i++) {
            boolean esReal = (i == real);   // el dato SI pertenece a la clase i
            boolean esPred = (i == pred);   // el modelo dijo que es la clase i
            String r;

            if (esReal && esPred) {
                r = "TP";
                tp[i]++;
            } else if (!esReal && esPred) {
                r = "FP";
                fp[i]++;
            } else if (esReal && !esPred) {
                r = "FN";
                fn[i]++;
            } else {
                r = "TN";
                tn[i]++;
            }
            resultados[i] = r;
            evaluaciones.get(i).add(r);
        }
        return resultados;
    }

    // Busca la posicion de una clase en el arreglo de clases
    private int indiceDe(String clase) {
        for (int i = 0; i < clases.length; i++) {
            if (clases[i].equals(clase)) return i;
        }
        return -1;
    }

    // ---------- Metricas por clase ----------
    public double precision(int i) {
        int den = tp[i] + fp[i];
        if (den == 0) return 0;
        return (double) tp[i] / den;
    }

    public double recall(int i) {
        int den = tp[i] + fn[i];
        if (den == 0) return 0;
        return (double) tp[i] / den;
    }

    public double accuracy(int i) {
        int total = tp[i] + tn[i] + fp[i] + fn[i];
        if (total == 0) return 0;
        return (double) (tp[i] + tn[i]) / total;
    }

    public double fScore(int i) {
        double p = precision(i);
        double r = recall(i);
        if (p + r == 0) return 0;
        return 2 * p * r / (p + r);
    }

    // ---------- Promedios generales ----------
    public double promedioPrecision() {
        double suma = 0;
        for (int i = 0; i < clases.length; i++) suma += precision(i);
        return suma / clases.length;
    }

    public double promedioRecall() {
        double suma = 0;
        for (int i = 0; i < clases.length; i++) suma += recall(i);
        return suma / clases.length;
    }

    public double promedioAccuracy() {
        double suma = 0;
        for (int i = 0; i < clases.length; i++) suma += accuracy(i);
        return suma / clases.length;
    }

    public double promedioFScore() {
        double suma = 0;
        for (int i = 0; i < clases.length; i++) suma += fScore(i);
        return suma / clases.length;
    }

    // Convierte un numero a texto con 4 decimales y punto (para que no rompa el CSV)
    public static String redondear(double valor) {
        return String.format(Locale.US, "%.4f", valor);
    }

    // ---------- Getters ----------
    public String[] getClases() { return clases; }
    public int getTp(int i) { return tp[i]; }
    public int getTn(int i) { return tn[i]; }
    public int getFp(int i) { return fp[i]; }
    public int getFn(int i) { return fn[i]; }
    public int[][] getMatriz() { return matriz; }
    public List<String> getEvaluaciones(int i) { return evaluaciones.get(i); }
}
