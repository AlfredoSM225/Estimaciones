package com.agenda.estimaciondedesempeno;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.TreeSet;

public class VentanaPrincipal extends JFrame {

    // Componentes de la interfaz
    private JTextField txtArchivo = new JTextField(30);
    private JTextField txtClases = new JTextField(5);
    private JCheckBox chkEncabezado = new JCheckBox("El archivo tiene encabezado", true);
    private JRadioButton rbPaso = new JRadioButton("Paso a paso", true);
    private JRadioButton rbCompleto = new JRadioButton("Procesar todo");
    private JButton btnBuscar = new JButton("Buscar...");
    private JButton btnIniciar = new JButton("Iniciar");
    private JButton btnSiguiente = new JButton("Siguiente dato");
    private JButton btnResto = new JButton("Procesar el resto");
    private JButton btnTerminar = new JButton("Terminar");
    private JTextArea areaEvaluacion = new JTextArea();
    private JTextArea areaResultados = new JTextArea();
    private JTable tablaMatriz = new JTable();
    private JTabbedPane pestanas = new JTabbedPane();
    private JLabel lblEstado = new JLabel("Seleccione un archivo para comenzar.");

    // Datos del proceso
    private List<Dato> datos;
    private Evaluador evaluador;
    private String[] clases;
    private int posicion;
    private String carpeta;
    private String[] archivosClases;
    private String archivoResultados;
    private String archivoMatriz;

    public VentanaPrincipal() {
        setTitle("Evaluador de modelos de clasificación");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        armarVentana();
        reiniciarBotones();
    }

    // Crea y acomoda todos los componentes
    private void armarVentana() {
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbPaso);
        grupo.add(rbCompleto);

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila1.add(new JLabel("Archivo CSV:"));
        fila1.add(txtArchivo);
        fila1.add(btnBuscar);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila2.add(new JLabel("Cantidad de clases:"));
        fila2.add(txtClases);
        fila2.add(chkEncabezado);

        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila3.add(new JLabel("Modo:"));
        fila3.add(rbPaso);
        fila3.add(rbCompleto);
        fila3.add(btnIniciar);

        JPanel superior = new JPanel(new GridLayout(3, 1));
        superior.add(fila1);
        superior.add(fila2);
        superior.add(fila3);

        Font fuente = new Font(Font.MONOSPACED, Font.PLAIN, 13);
        areaEvaluacion.setEditable(false);
        areaEvaluacion.setFont(fuente);
        areaResultados.setEditable(false);
        areaResultados.setFont(fuente);
        tablaMatriz.setRowHeight(30);

        pestanas.addTab("Evaluación por dato", new JScrollPane(areaEvaluacion));
        pestanas.addTab("Resultados", new JScrollPane(areaResultados));
        pestanas.addTab("Matriz de confusión", new JScrollPane(tablaMatriz));

        JPanel botones = new JPanel();
        botones.add(btnSiguiente);
        botones.add(btnResto);
        botones.add(btnTerminar);

        JPanel inferior = new JPanel(new BorderLayout());
        inferior.add(botones, BorderLayout.NORTH);
        inferior.add(lblEstado, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);

        // Acciones de los botones
        btnBuscar.addActionListener(e -> buscarArchivo());
        btnIniciar.addActionListener(e -> iniciar());
        btnSiguiente.addActionListener(e -> procesarSiguiente());
        btnResto.addActionListener(e -> procesarResto());
        btnTerminar.addActionListener(e -> terminar());
    }

    // Abre el selector de archivos
    private void buscarArchivo() {
        JFileChooser selector = new JFileChooser();
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtArchivo.setText(selector.getSelectedFile().getAbsolutePath());
        }
    }

    // Valida datos, lee el archivo y arranca el modo elegido
    private void iniciar() {
        try {
            String ruta = txtArchivo.getText().trim();
            if (ruta.isEmpty()) {
                throw new Exception("Debe indicar el archivo CSV de entrada.");
            }

            int numClases;
            try {
                numClases = Integer.parseInt(txtClases.getText().trim());
            } catch (NumberFormatException ex) {
                throw new Exception("La cantidad de clases debe ser un número entero.");
            }
            if (numClases < 2) {
                throw new Exception("Debe haber al menos 2 clases.");
            }

            datos = LectorCSV.leer(ruta, chkEncabezado.isSelected());
            clases = obtenerClases(numClases);
            carpeta = new File(ruta).getAbsoluteFile().getParent();

            if (!pedirNombresArchivos()) {
                lblEstado.setText("Proceso cancelado.");
                return;
            }

            evaluador = new Evaluador(clases);
            posicion = 0;
            areaEvaluacion.setText("");
            areaResultados.setText("");
            tablaMatriz.setModel(new DefaultTableModel());
            pestanas.setSelectedIndex(0);
            btnIniciar.setEnabled(false);

            if (rbCompleto.isSelected()) {
                procesarResto();
            } else {
                btnSiguiente.setEnabled(true);
                btnResto.setEnabled(true);
                btnTerminar.setEnabled(true);
                procesarSiguiente();   // evalua el primer dato
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    // Obtiene los nombres de las clases que aparecen en el archivo
    private String[] obtenerClases(int numClases) throws Exception {
        TreeSet<String> conjunto = new TreeSet<>();
        for (Dato d : datos) {
            conjunto.add(d.getClaseVerdadera());
            conjunto.add(d.getClasePredicha());
        }
        if (conjunto.size() != numClases) {
            throw new Exception("Indicó " + numClases + " clases, pero el archivo tiene "
                    + conjunto.size() + ": " + conjunto);
        }
        return conjunto.toArray(new String[0]);
    }

    // Pregunta el nombre de cada archivo de salida. Devuelve false si cancela
    private boolean pedirNombresArchivos() {
        archivosClases = new String[clases.length];
        for (int i = 0; i < clases.length; i++) {
            archivosClases[i] = pedirNombre("Nombre del archivo para la clase " + clases[i] + ":",
                    "Clase_" + clases[i] + ".csv");
            if (archivosClases[i] == null) return false;
        }
        archivoResultados = pedirNombre("Nombre del archivo de resultados finales:", "ResultadosFinal.csv");
        if (archivoResultados == null) return false;

        archivoMatriz = pedirNombre("Nombre del archivo de la matriz de confusión:", "MatrizConfusion.csv");
        return archivoMatriz != null;
    }

    // Muestra un cuadro para escribir un nombre y devuelve la ruta completa
    private String pedirNombre(String mensaje, String porDefecto) {
        String nombre = JOptionPane.showInputDialog(this, mensaje, porDefecto);
        if (nombre == null) return null;
        nombre = nombre.trim();
        if (nombre.isEmpty()) nombre = porDefecto;
        if (!nombre.toLowerCase().endsWith(".csv")) nombre = nombre + ".csv";
        return carpeta + File.separator + nombre;
    }

    // Evalua el siguiente dato y lo muestra
    private void procesarSiguiente() {
        try {
            evaluarDatoActual();
            if (posicion == datos.size()) {
                finalizar();
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    // Evalua todos los datos que faltan
    private void procesarResto() {
        try {
            while (posicion < datos.size()) {
                evaluarDatoActual();
            }
            finalizar();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    // Evalua el dato que sigue y escribe el resultado en pantalla
    private void evaluarDatoActual() {
        Dato d = datos.get(posicion);
        String[] resultados = evaluador.evaluarDato(d);

        StringBuilder texto = new StringBuilder();
        texto.append("Dato ").append(d.getId())
                .append(" | Real: ").append(d.getClaseVerdadera())
                .append(" | Predicción: ").append(d.getClasePredicha()).append("\n");
        for (int i = 0; i < clases.length; i++) {
            texto.append("    Clase ").append(clases[i]).append(": ").append(resultados[i]).append("\n");
        }
        areaEvaluacion.append(texto.toString() + "\n");

        posicion++;
        lblEstado.setText("Dato " + posicion + " de " + datos.size() + " evaluado.");
    }

    // Guarda los archivos y muestra los resultados finales
    private void finalizar() throws Exception {
        for (int i = 0; i < clases.length; i++) {
            GestorArchivos.guardarClase(archivosClases[i], clases[i], datos, evaluador.getEvaluaciones(i));
        }
        GestorArchivos.guardarResultados(archivoResultados, evaluador);
        GestorArchivos.guardarMatriz(archivoMatriz, evaluador);

        mostrarResultados();
        mostrarMatriz();
        pestanas.setSelectedIndex(1);
        reiniciarBotones();
        lblEstado.setText("Proceso terminado. Archivos guardados en: " + carpeta);
        JOptionPane.showMessageDialog(this, "Proceso terminado.\nArchivos guardados en:\n" + carpeta);
    }

    // Escribe sumas, metricas y promedios en la pestana de resultados
    private void mostrarResultados() {
        StringBuilder t = new StringBuilder();

        t.append("SUMAS POR CLASE\n");
        t.append(String.format("%-8s%-8s%-8s%-8s%-8s%n", "Clase", "TP", "TN", "FP", "FN"));
        for (int i = 0; i < clases.length; i++) {
            t.append(String.format("%-8s%-8d%-8d%-8d%-8d%n", clases[i],
                    evaluador.getTp(i), evaluador.getTn(i), evaluador.getFp(i), evaluador.getFn(i)));
        }

        t.append("\nMÉTRICAS POR CLASE\n");
        t.append(String.format("%-8s%-11s%-11s%-11s%-11s%n", "Clase", "Precision", "Recall", "Accuracy", "F-Score"));
        for (int i = 0; i < clases.length; i++) {
            t.append(String.format("%-8s%-11s%-11s%-11s%-11s%n", clases[i],
                    Evaluador.redondear(evaluador.precision(i)),
                    Evaluador.redondear(evaluador.recall(i)),
                    Evaluador.redondear(evaluador.accuracy(i)),
                    Evaluador.redondear(evaluador.fScore(i))));
        }

        t.append("\nPROMEDIO GENERAL DEL MODELO\n");
        t.append(String.format("%-11s%-11s%-11s%-11s%n", "Precision", "Recall", "Accuracy", "F-Score"));
        t.append(String.format("%-11s%-11s%-11s%-11s%n",
                Evaluador.redondear(evaluador.promedioPrecision()),
                Evaluador.redondear(evaluador.promedioRecall()),
                Evaluador.redondear(evaluador.promedioAccuracy()),
                Evaluador.redondear(evaluador.promedioFScore())));

        areaResultados.setText(t.toString());
    }

    // Llena la tabla de la matriz de confusion
    private void mostrarMatriz() {
        int n = clases.length;
        String[] columnas = new String[n + 1];
        columnas[0] = "Real \\ Predicción";
        for (int i = 0; i < n; i++) {
            columnas[i + 1] = "Clase " + clases[i];
        }

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        int[][] m = evaluador.getMatriz();
        for (int i = 0; i < n; i++) {
            Object[] fila = new Object[n + 1];
            fila[0] = "Clase " + clases[i];
            for (int j = 0; j < n; j++) {
                fila[j + 1] = m[i][j];
            }
            modelo.addRow(fila);
        }
        tablaMatriz.setModel(modelo);
    }

    // Cancela el proceso paso a paso (no se guardan archivos)
    private void terminar() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea terminar el proceso? No se guardarán los resultados.",
                "Terminar", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            reiniciarBotones();
            lblEstado.setText("Proceso terminado por el usuario. No se generaron archivos.");
        }
    }

    // Muestra un error en una ventana y en la barra de estado
    private void mostrarError(String mensaje) {
        lblEstado.setText("Error: " + mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        reiniciarBotones();
    }

    // Deja los botones listos para una nueva ejecucion
    private void reiniciarBotones() {
        btnIniciar.setEnabled(true);
        btnSiguiente.setEnabled(false);
        btnResto.setEnabled(false);
        btnTerminar.setEnabled(false);
    }
}
