/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package persistencia;

import enums.PoliticaPlanificacion;
import enums.TipoProceso;
import modelo.Proceso;
import sistema.Buffer;
import sistema.ClusterManager;
import sistema.Computador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author ispam
 */
public class ConfigManager {
    public static void guardarConfiguracionCSV(ClusterManager cluster, File archivo) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write("#GLOBALES;duracionCicloMs;latenciaRedCiclos\n");
            bw.write("GLOBAL;" + cluster.getRelojGlobal().getDuracionCicloMs() + ";" + cluster.getLatenciaRedCiclos() + "\n");

            bw.write("#COMPUTADORES;id;nombre;ramTotal;politica;quantum\n");
            for (int i = 0; i < cluster.getComputadores().getTamano(); i++) {
                Computador c = cluster.getComputadores().obtener(i);
                bw.write("COMPUTADOR;" + c.getIdComputador() + ";" + c.getNombre() + ";" + c.getRamTotal() + ";"
                        + c.getPoliticaActual().name() + ";" + c.getQuantumConfigurado() + "\n");
            }

            bw.write("#BUFFERS;idBuffer;capacidad;idComputadorAnfitrion\n");
            for (int i = 0; i < cluster.getBuffers().getTamano(); i++) {
                Buffer b = cluster.getBuffers().obtener(i);
                bw.write("BUFFER;" + b.getIdBuffer() + ";" + b.getCapacidad() + ";" + b.getIdComputadorAnfitrion() + "\n");
            }
        }
    }

    public static void cargarConfiguracionCSV(ClusterManager cluster, File archivo) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;

                String[] tokens = linea.split(";");
                String tipo = tokens[0].toUpperCase();

                switch (tipo) {
                    case "GLOBAL":
                        int duracionMs = Integer.parseInt(tokens[1]);
                        int latenciaRed = Integer.parseInt(tokens[2]);
                        cluster.getRelojGlobal().setDuracionCicloMs(duracionMs);
                        cluster.setLatenciaRedCiclos(latenciaRed);
                        break;

                    case "COMPUTADORES":
                    case "COMPUTADOR":
                        String nombreComp = tokens[2];
                        int ram = Integer.parseInt(tokens[3]);
                        PoliticaPlanificacion pol = PoliticaPlanificacion.valueOf(tokens[4]);
                        int quantum = Integer.parseInt(tokens[5]);
                        cluster.crearComputador(nombreComp, ram, pol, quantum);
                        break;

                    case "BUFFERS":
                    case "BUFFER":
                        String idBuf = tokens[1];
                        int cap = Integer.parseInt(tokens[2]);
                        int idAnfitrion = Integer.parseInt(tokens[3]);
                        cluster.crearBuffer(idBuf, cap, idAnfitrion);
                        break;

                    case "PROCESO":
                        String nombreP = tokens[1];
                        TipoProceso tipoP = TipoProceso.valueOf(tokens[2]);
                        int prio = Integer.parseInt(tokens[3]);
                        int ramP = Integer.parseInt(tokens[4]);
                        int dline = Integer.parseInt(tokens[5]);
                        int compDest = Integer.parseInt(tokens[6]);

                        if (tipoP == TipoProceso.PRODUCTOR || tipoP == TipoProceso.CONSUMIDOR) {
                            String bufTarget = tokens[7];
                            int freq = Integer.parseInt(tokens[8]);
                            int elems = Integer.parseInt(tokens[9]);
                            Proceso p = new Proceso(nombreP, tipoP, prio, ramP, dline, bufTarget, freq, elems);
                            cluster.agregarProceso(p, compDest);
                        } else {
                            int insts = Integer.parseInt(tokens[7]);
                            Proceso p = new Proceso(nombreP, tipoP, prio, ramP, dline, insts);
                            cluster.agregarProceso(p, compDest);
                        }
                        break;

                    default:
                        break;
                }
            }
        }
    }
}
