/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author ispam
 */
public class MetricasComputador {
    private int ciclosTotales;
    private int ciclosCpuOcupada;
    private int procesosCompletados;
    private int procesosExpiradosDeadline;
    private long sumaTiemposRespuesta;
    private long sumaTiemposEsperaListos;
    private long sumaCuadradosTiemposEspera;
    private long sumaTiempoBloqueadoSemaforo;

    public MetricasComputador() {
        this.ciclosTotales = 0;
        this.ciclosCpuOcupada = 0;
        this.procesosCompletados = 0;
        this.procesosExpiradosDeadline = 0;
        this.sumaTiemposRespuesta = 0;
        this.sumaTiemposEsperaListos = 0;
        this.sumaCuadradosTiemposEspera = 0;
        this.sumaTiempoBloqueadoSemaforo = 0;
    }

    public synchronized void registrarCiclo(boolean cpuOcupada) {
        ciclosTotales++;
        if (cpuOcupada) {
            ciclosCpuOcupada++;
        }
    }

    public synchronized void registrarProcesoFinalizado(Proceso proceso, int cicloActual) {
        procesosCompletados++;
        proceso.setCicloFin(cicloActual);
        int tiempoRespuesta = cicloActual - proceso.getCicloLlegada();
        sumaTiemposRespuesta += tiempoRespuesta;
        long espera = proceso.getTiempoEsperaListos();
        sumaTiemposEsperaListos += espera;
        sumaCuadradosTiemposEspera += (espera * espera);
        sumaTiempoBloqueadoSemaforo += proceso.getTiempoBloqueadoSemaforo();

        if (proceso.haExpiradoDeadline()) {
            procesosExpiradosDeadline++;
        }
    }

    public synchronized double getUtilizacionCPU() {
        if (ciclosTotales == 0) return 0.0;
        return ((double) ciclosCpuOcupada / ciclosTotales) * 100.0;
    }

    public synchronized double getThroughput() {
        if (ciclosTotales == 0) return 0.0;
        return (double) procesosCompletados / ciclosTotales;
    }

    public synchronized double getTiempoRespuestaPromedio() {
        if (procesosCompletados == 0) return 0.0;
        return (double) sumaTiemposRespuesta / procesosCompletados;
    }

    public synchronized double getTasaCumplimientoDeadlines() {
        if (procesosCompletados == 0) return 100.0;
        int aTiempo = procesosCompletados - procesosExpiradosDeadline;
        return ((double) aTiempo / procesosCompletados) * 100.0;
    }

    public synchronized double getIndiceEquidad() {
        if (procesosCompletados == 0 || sumaCuadradosTiemposEspera == 0) return 1.0;
        double numerador = ((double) sumaTiemposEsperaListos) * sumaTiemposEsperaListos;
        double denominador = procesosCompletados * (double) sumaCuadradosTiemposEspera;
        return numerador / denominador;
    }

    public synchronized double getTiempoPromedioBloqueoSemaforos() {
        if (procesosCompletados == 0) return 0.0;
        return (double) sumaTiempoBloqueadoSemaforo / procesosCompletados;
    }

    public synchronized SnapshotMetricas obtenerSnapshot() {
        return new SnapshotMetricas(
            getUtilizacionCPU(),
            getThroughput(),
            getTiempoRespuestaPromedio(),
            getTasaCumplimientoDeadlines(),
            getIndiceEquidad(),
            getTiempoPromedioBloqueoSemaforos(),
            ciclosTotales,
            ciclosCpuOcupada,
            procesosCompletados,
            procesosExpiradosDeadline
        );
    }

    public int getCiclosTotales() {
        return ciclosTotales;
    }
    public int getCiclosCpuOcupada() {
        return ciclosCpuOcupada;
    }
    public int getProcesosCompletados() {
        return procesosCompletados;
    }

    public static class SnapshotMetricas {
        public final double utilizacionCPU;
        public final double throughput;
        public final double tiempoRespuestaPromedio;
        public final double tasaCumplimientoDeadlines;
        public final double indiceEquidad;
        public final double tiempoPromedioBloqueoSemaforos;
        public final int ciclosTotales;
        public final int ciclosCpuOcupada;
        public final int procesosCompletados;
        public final int procesosExpiradosDeadline;

        public SnapshotMetricas(double utilizacionCPU, double throughput, double tiempoRespuestaPromedio,
                                double tasaCumplimientoDeadlines, double indiceEquidad, double tiempoPromedioBloqueoSemaforos,
                                int ciclosTotales, int ciclosCpuOcupada, int procesosCompletados, int procesosExpiradosDeadline) {
            this.utilizacionCPU = utilizacionCPU;
            this.throughput = throughput;
            this.tiempoRespuestaPromedio = tiempoRespuestaPromedio;
            this.tasaCumplimientoDeadlines = tasaCumplimientoDeadlines;
            this.indiceEquidad = indiceEquidad;
            this.tiempoPromedioBloqueoSemaforos = tiempoPromedioBloqueoSemaforos;
            this.ciclosTotales = ciclosTotales;
            this.ciclosCpuOcupada = ciclosCpuOcupada;
            this.procesosCompletados = procesosCompletados;
            this.procesosExpiradosDeadline = procesosExpiradosDeadline;
        }
    }
}
