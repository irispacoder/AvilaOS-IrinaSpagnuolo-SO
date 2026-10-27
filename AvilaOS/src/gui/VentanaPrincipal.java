/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;
import enums.PoliticaPlanificacion;
import enums.TipoProceso;
import modelo.Proceso;
import persistencia.ConfigManager;
import sistema.Buffer;
import sistema.ClusterManager;
import sistema.Computador;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 *
 * @author ispam
 */
public class VentanaPrincipal extends JFrame {
    
    private final ClusterManager clusterManager;

    private final JLabel lblCicloReloj;
    private final JTextArea txtLogGlobal;
    private final JPanel pnlComputadores;
    private final JPanel pnlBuffers;
    private final PanelGraficoMetricas panelGrafico;

    private final avilaos.estructuras.ListaEnlazada<PanelComputadorView> panelesComputadores;
    private final Timer timerRefrescoGUI;
    private int ultimoIndiceLogLeido = 0;

    public VentanaPrincipal() {
        super("Simulador de Sistema Operativo Distribuido - ÁvilaOS");
        this.clusterManager = new ClusterManager(1000, 2);
        this.panelesComputadores = new avilaos.estructuras.ListaEnlazada<>();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnIniciar = new JButton("▶ Iniciar / Reanudar");
        JButton btnPausar = new JButton("⏸ Pausar");
        JButton btnDetener = new JButton("⏹ Detener");

        lblCicloReloj = new JLabel("  Ciclo Reloj Global: 0  ");
        lblCicloReloj.setFont(new Font("SansSerif", Font.BOLD, 14));

        JSpinner spDuracionCiclo = new JSpinner(new SpinnerNumberModel(1000, 50, 5000, 100));
        spDuracionCiclo.addChangeListener(e -> clusterManager.getRelojGlobal().setDuracionCicloMs((Integer) spDuracionCiclo.getValue()));

        JButton btnAgregarComp = new JButton("+ Computador");
        JButton btnAgregarBuffer = new JButton("+ Buffer");
        JButton btnAgregarProceso = new JButton("+ Proceso (Vista 1)");
        JButton btnGuardar = new JButton("💾 Guardar CSV");
        JButton btnCargar = new JButton("📁 Cargar CSV");

        toolBar.add(btnIniciar);
        toolBar.add(btnPausar);
        toolBar.add(btnDetener);
        toolBar.addSeparator();
        toolBar.add(lblCicloReloj);
        toolBar.add(new JLabel(" Duración Ciclo (ms):"));
        toolBar.add(spDuracionCiclo);
        toolBar.addSeparator();
        toolBar.add(btnAgregarComp);
        toolBar.add(btnAgregarBuffer);
        toolBar.add(btnAgregarProceso);
        toolBar.addSeparator();
        toolBar.add(btnGuardar);
        toolBar.add(btnCargar);

        add(toolBar, BorderLayout.NORTH);

        pnlComputadores = new JPanel(new GridLayout(1, 0, 10, 10));
        JScrollPane scrollComputadores = new JScrollPane(pnlComputadores);

        pnlBuffers = new JPanel();
        pnlBuffers.setLayout(new BoxLayout(pnlBuffers, BoxLayout.Y_AXIS));
        pnlBuffers.setBorder(BorderFactory.createTitledBorder("Buffers Compartidos & Semáforos"));
        JScrollPane scrollBuffers = new JScrollPane(pnlBuffers);
        scrollBuffers.setPreferredSize(new Dimension(300, 0));

        JSplitPane splitCentral = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollComputadores, scrollBuffers);
        splitCentral.setResizeWeight(0.75);

        JTabbedPane tabbedBottom = new JTabbedPane();

        txtLogGlobal = new JTextArea(8, 0);
        txtLogGlobal.setEditable(false);
        txtLogGlobal.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(txtLogGlobal);
        tabbedBottom.addTab("Log Global de Eventos", scrollLog);

        panelGrafico = new PanelGraficoMetricas(clusterManager);
        tabbedBottom.addTab("📈 Utilización de CPU vs. Tiempo", panelGrafico);

        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitCentral, tabbedBottom);
        splitPrincipal.setResizeWeight(0.65);

        add(splitPrincipal, BorderLayout.CENTER);

        btnIniciar.addActionListener(e -> clusterManager.iniciarSimulacion());
        btnPausar.addActionListener(e -> clusterManager.pausarSimulacion());
        btnDetener.addActionListener(e -> {
            clusterManager.detenerSimulacion();
            refrescarGUI();
        });

        btnAgregarComp.addActionListener(e -> mostrarDialogoCrearComputador());
        btnAgregarBuffer.addActionListener(e -> mostrarDialogoCrearBuffer());
        btnAgregarProceso.addActionListener(e -> mostrarDialogoCrearProceso());

        btnGuardar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    ConfigManager.guardarConfiguracionCSV(clusterManager, fc.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Configuración guardada exitosamente.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCargar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    ConfigManager.cargarConfiguracionCSV(clusterManager, fc.getSelectedFile());
                    reconstruirPanelesComputadores();
                    refrescarGUI();
                    JOptionPane.showMessageDialog(this, "Configuración cargada exitosamente.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        clusterManager.crearComputador("Comp-1", 1024, PoliticaPlanificacion.ROUND_ROBIN, 3);
        clusterManager.crearComputador("Comp-2", 1024, PoliticaPlanificacion.EDF, 3);
        clusterManager.crearBuffer("B1", 5, 1);
        reconstruirPanelesComputadores();

        timerRefrescoGUI = new Timer(100, e -> refrescarGUI());
        timerRefrescoGUI.start();
    }

    private void reconstruirPanelesComputadores() {
        pnlComputadores.removeAll();
        panelesComputadores.vaciar();

        for (int i = 0; i < clusterManager.getComputadores().getTamano(); i++) {
            Computador c = clusterManager.getComputadores().obtener(i);
            PanelComputadorView pView = new PanelComputadorView(c);
            panelesComputadores.agregarAlFinal(pView);
            pnlComputadores.add(pView);
        }
        if (panelGrafico != null) {
            panelGrafico.reconstruirSeries();
        }
        pnlComputadores.revalidate();
        pnlComputadores.repaint();
    }

    private void refrescarGUI() {
        lblCicloReloj.setText("  Ciclo Reloj Global: " + clusterManager.getRelojGlobal().getCicloActual() + "  ");

        panelesComputadores.paraCadaElemento(PanelComputadorView::actualizarRefrescoGUI);

        pnlBuffers.removeAll();
        for (int i = 0; i < clusterManager.getBuffers().getTamano(); i++) {
            Buffer b = clusterManager.getBuffers().obtener(i);
            JPanel pBuf = new JPanel(new GridLayout(4, 1));
            pBuf.setBorder(BorderFactory.createTitledBorder("Buffer [" + b.getIdBuffer() + "] (Anfitrión: Comp " + b.getIdComputadorAnfitrion() + ")"));
            pBuf.add(new JLabel("Ocupación: " + b.getElementosActuales() + " / " + b.getCapacidad()));
            pBuf.add(new JLabel("Semáforo MUTEX: " + b.getMutex().getValor() + " (Bloq: " + b.getMutex().obtenerCopiaColaBloqueados().getTamano() + ")"));
            pBuf.add(new JLabel("Semáforo LLENO: " + b.getLleno().getValor() + " (Bloq: " + b.getLleno().obtenerCopiaColaBloqueados().getTamano() + ")"));
            pBuf.add(new JLabel("Semáforo VACIO: " + b.getVacio().getValor() + " (Bloq: " + b.getVacio().obtenerCopiaColaBloqueados().getTamano() + ")"));
            pnlBuffers.add(pBuf);
        }
        pnlBuffers.revalidate();
        pnlBuffers.repaint();

        if (panelGrafico != null) {
            panelGrafico.actualizarGrafico();
        }

        avilaos.estructuras.ListaEnlazada<String> nuevosLogs = clusterManager.obtenerNuevosLogs(ultimoIndiceLogLeido);
        if (!nuevosLogs.estaVacia()) {
            for (int i = 0; i < nuevosLogs.getTamano(); i++) {
                txtLogGlobal.append(nuevosLogs.obtener(i) + "\n");
            }
            ultimoIndiceLogLeido += nuevosLogs.getTamano();
            txtLogGlobal.setCaretPosition(txtLogGlobal.getDocument().getLength());
        }
    }

    private void mostrarDialogoCrearComputador() {
        JTextField txtNombre = new JTextField("Comp-" + (clusterManager.getComputadores().getTamano() + 1));
        JTextField txtRam = new JTextField("1024");
        JComboBox<PoliticaPlanificacion> cbPol = new JComboBox<>(PoliticaPlanificacion.values());
        JTextField txtQuantum = new JTextField("3");

        Object[] fields = {"Nombre:", txtNombre, "RAM (MB):", txtRam, "Política Inicial:", cbPol, "Quantum:", txtQuantum};
        if (JOptionPane.showConfirmDialog(this, fields, "Crear Nuevo Computador", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                clusterManager.crearComputador(txtNombre.getText(), Integer.parseInt(txtRam.getText()),
                        (PoliticaPlanificacion) cbPol.getSelectedItem(), Integer.parseInt(txtQuantum.getText()));
                reconstruirPanelesComputadores();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Entrada inválida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarDialogoCrearBuffer() {
        JTextField txtId = new JTextField("B" + (clusterManager.getBuffers().getTamano() + 1));
        JTextField txtCapacidad = new JTextField("5");
        JTextField txtAnfitrion = new JTextField("1");

        Object[] fields = {"ID Buffer:", txtId, "Capacidad:", txtCapacidad, "ID Computador Anfitrión:", txtAnfitrion};
        if (JOptionPane.showConfirmDialog(this, fields, "Crear Nuevo Buffer Compartido", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                clusterManager.crearBuffer(txtId.getText(), Integer.parseInt(txtCapacidad.getText()), Integer.parseInt(txtAnfitrion.getText()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Entrada inválida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarDialogoCrearProceso() {
        JTextField txtNombre = new JTextField("P1");
        JComboBox<TipoProceso> cbTipo = new JComboBox<>(TipoProceso.values());
        JTextField txtPrio = new JTextField("1");
        JTextField txtRam = new JTextField("128");
        JTextField txtDeadline = new JTextField("50");
        JTextField txtCompDestino = new JTextField("0");

        JTextField txtIdBuffer = new JTextField("B1");
        JTextField txtFrecuencia = new JTextField("3");
        JTextField txtElementos = new JTextField("10");

        JTextField txtInstrucciones = new JTextField("20");

        Object[] fields = {
            "Nombre Proceso:", txtNombre,
            "Tipo:", cbTipo,
            "Prioridad:", txtPrio,
            "RAM Requerida (MB):", txtRam,
            "Deadline (Ciclos):", txtDeadline,
            "Comp. Destino (0 = Automático):", txtCompDestino,
            "--- Para CPU_BOUND / IO_BOUND ---",
            "Instrucciones Totales:", txtInstrucciones,
            "--- Para PRODUCTOR / CONSUMIDOR ---",
            "ID Buffer Target:", txtIdBuffer,
            "Frecuencia (Ciclos/elem):", txtFrecuencia,
            "Elementos Requeridos:", txtElementos
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Crear Nuevo Proceso (Vista 1)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                TipoProceso tipo = (TipoProceso) cbTipo.getSelectedItem();
                int prio = Integer.parseInt(txtPrio.getText());
                int ram = Integer.parseInt(txtRam.getText());
                int deadline = Integer.parseInt(txtDeadline.getText());
                int dest = Integer.parseInt(txtCompDestino.getText());

                Proceso nuevo;
                if (tipo == TipoProceso.PRODUCTOR || tipo == TipoProceso.CONSUMIDOR) {
                    nuevo = new Proceso(txtNombre.getText(), tipo, prio, ram, deadline,
                            txtIdBuffer.getText(), Integer.parseInt(txtFrecuencia.getText()), Integer.parseInt(txtElementos.getText()));
                } else {
                    nuevo = new Proceso(txtNombre.getText(), tipo, prio, ram, deadline, Integer.parseInt(txtInstrucciones.getText()));
                }

                clusterManager.agregarProceso(nuevo, dest);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Entrada inválida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
}
