/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistema;
import avilaos.estructuras.Cola;
import avilaos.estructuras.ListaEnlazada;
import enums.EstadoProcesos;
import enums.ModoCPU;
import enums.MotivoBloqueo;
import enums.PoliticaPlanificacion;
import enums.TipoProceso;
import enums.PasoBuffer;
import interfaces.IScheduler;
import modelo.MetricasComputador;
import modelo.Proceso;
import planificadores.EDFScheduler;
import planificadores.FCFSScheduler;
import planificadores.PrioridadesScheduler;
import planificadores.RRScheduler;
import sistema.Buffer.ResultadoPasoBuffer;
import interfaces.IClockListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 *
 * @author ispam
 */
public class Computador implements Runnable, IClockListener{
    private final int idComputador;
    private final String nombre;
    private final int ramTotal;
    private int ramUsada;
    private Proceso procesoEnCPU;
    private ModoCPU modoCPU;
    private IScheduler planificadorActual;
    private PoliticaPlanificacion politicaActual;
    private int quantumConfigurado;
    private int quantumRestante;
    private final Cola<Proceso> colaNuevos;
    private final ListaEnlazada<Proceso> colaListos;
    private final ListaEnlazada<Proceso> colaBloqueados;
    private final ListaEnlazada<Proceso> colaTerminados;
    private final MetricasComputador metricas;

    private Thread hiloComputador;
    private volatile boolean ejecucionActiva;
    private final Semaphore semaforoPasoCiclo;
    private CountDownLatch latchSincronizacionReloj;
    private int cicloActualPaso;

    private int duracionBloqueoIOCiclos;
    private ClusterManager clusterManager;

    public Computador(int idComputador, String nombre, int ramTotal, PoliticaPlanificacion politicaInicial, int quantumInicial) {
        this.idComputador = idComputador;
        this.nombre = nombre;
        this.ramTotal = ramTotal;
        this.ramUsada = 0;
        this.procesoEnCPU = null;
        this.modoCPU = ModoCPU.SISTEMA_OPERATIVO;
        this.quantumConfigurado = quantumInicial;
        this.quantumRestante = quantumInicial;
        this.colaNuevos = new Cola<>();
        this.colaListos = new ListaEnlazada<>();
        this.colaBloqueados = new ListaEnlazada<>();
        this.colaTerminados = new ListaEnlazada<>();
        this.metricas = new MetricasComputador();
        this.duracionBloqueoIOCiclos = 3;
        this.semaforoPasoCiclo = new Semaphore(0);
        this.ejecucionActiva = false;
        this.setPoliticaPlanificacion(politicaInicial);
    }

    public void setClusterManager(ClusterManager cm) {
        this.clusterManager = cm;
    }

    public synchronized void setModoCPU(ModoCPU modo) {
        this.modoCPU = modo;
    }

    public synchronized void iniciarHilo() {
        if (!ejecucionActiva) {
            ejecucionActiva = true;
            hiloComputador = new Thread(this, "Computador-Thread-" + idComputador);
            hiloComputador.start();
        }
    }

    public synchronized void detenerHilo() {
        ejecucionActiva = false;
        if (hiloComputador != null) {
            hiloComputador.interrupt();
        }
    }

    public void dispararCicloParalelo(int cicloActual, CountDownLatch latch) {
        this.latchSincronizacionReloj = latch;
        onClockTick(cicloActual);
    }
    
    @Override
    public void onClockTick(int cicloActual) {
        this.cicloActualPaso = cicloActual;
        this.semaforoPasoCiclo.release();
    }

    @Override
    public void run() {
        while (ejecucionActiva) {
            try {
                semaforoPasoCiclo.acquire();
                if (!ejecucionActiva) break;

                ejecutarCicloComputador(cicloActualPaso);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                if (latchSincronizacionReloj != null) {
                    latchSincronizacionReloj.countDown();
                }
            }
        }
    }

    private synchronized void ejecutarCicloComputador(int cicloActual) {
        admitirProcesosNuevos();
        actualizarEstadoProcesos(cicloActual);
        limpiarProcesosExpirados(cicloActual);
        evaluarDesbloqueosIO();
        evaluarPlanificacion(cicloActual);
        ejecutarCicloCPU(cicloActual);
        metricas.registrarCiclo(procesoEnCPU != null);
    }

    public synchronized void setPoliticaPlanificacion(PoliticaPlanificacion politica) {
        this.politicaActual = politica;
        switch (politica) {
            case ROUND_ROBIN:
                this.planificadorActual = new RRScheduler();
                break;
            case EDF:
                this.planificadorActual = new EDFScheduler();
                break;
            case PRIORIDADES:
                this.planificadorActual = new PrioridadesScheduler();
                break;
            case FCFS:
            default:
                this.planificadorActual = new FCFSScheduler();
                break;
        }
    }

    public synchronized void agregarProcesoNuevo(Proceso proceso, int cicloActual) {
        proceso.setComputadorAsignado(this.idComputador);
        proceso.setCicloLlegada(cicloActual);
        proceso.setEstado(EstadoProcesos.NUEVO);
        colaNuevos.encolar(proceso);
        admitirProcesosNuevos();
    }

    public synchronized boolean reservarMemoriaBuffer(int capacidad) {
        if (ramUsada + capacidad <= ramTotal) {
            ramUsada += capacidad;
            return true;
        }
        return false;
    }

    public synchronized void admitirProcesosNuevos() {
        while (!colaNuevos.estaVacia()) {
            Proceso candidato = colaNuevos.frente();
            if (ramUsada + candidato.getMemoriaRequerida() <= ramTotal) {
                colaNuevos.desencolar();
                candidato.setMemoriaAsignada(true);
                ramUsada += candidato.getMemoriaRequerida();
                candidato.setEstado(EstadoProcesos.LISTO);
                colaListos.agregarAlFinal(candidato);
            } else {
                break;
            }
        }
    }

    private void actualizarEstadoProcesos(int cicloActual) {
        colaListos.paraCadaElemento(Proceso::incrementarTiempoEsperaListos);
        if (procesoEnCPU != null) {
            procesoEnCPU.decrementarDeadline();
        }
        colaListos.paraCadaElemento(Proceso::decrementarDeadline);

        colaBloqueados.paraCadaElemento(p -> {
            if (p.getMotivoBloqueo() == MotivoBloqueo.ESPERA_SEMAFORO) {
                p.incrementarTiempoBloqueadoSemaforo();
            } else if (p.getMotivoBloqueo() == MotivoBloqueo.LATENCIA_RED) {
                p.incrementarCiclosLatenciaPendientes();
            } else {
                p.incrementarTiempoBloqueado();
            }
        });
    }

    private void limpiarProcesosExpirados(int cicloActual) {
        ListaEnlazada<Proceso> expiradosListos = new ListaEnlazada<>();
        ListaEnlazada<Proceso> expiradosBloqueados = new ListaEnlazada<>();

        colaListos.paraCadaElemento(p -> {
            if (p.haExpiradoDeadline()) expiradosListos.agregarAlFinal(p);
        });
        colaBloqueados.paraCadaElemento(p -> {
            if (p.haExpiradoDeadline()) expiradosBloqueados.agregarAlFinal(p);
        });

        expiradosListos.paraCadaElemento(p -> {
            colaListos.eliminar(p);
            registrarLogLocal("Ciclo " + cicloActual + ": [EXPULSIÓN POR DEADLINE] Proceso " + p.getNombre() + " (ID: " + p.getId() + ") expiró en cola de LISTOS.");
            finalizarProcesoExpirado(p, cicloActual);
        });
        expiradosBloqueados.paraCadaElemento(p -> {
            colaBloqueados.eliminar(p);
            registrarLogLocal("Ciclo " + cicloActual + ": [EXPULSIÓN POR DEADLINE] Proceso " + p.getNombre() + " (ID: " + p.getId() + ") expiró en cola de BLOQUEADOS.");
            finalizarProcesoExpirado(p, cicloActual);
        });
    }

    private void finalizarProcesoExpirado(Proceso p, int cicloActual) {
        p.setEstado(EstadoProcesos.TERMINADO);
        if (p.isMemoriaAsignada()) {
            ramUsada -= p.getMemoriaRequerida();
            p.setMemoriaAsignada(false);
        }
        if (clusterManager != null) {
            clusterManager.removerProcesoDeSemaforos(p);
        }
        metricas.registrarProcesoFinalizado(p, cicloActual);
        colaTerminados.agregarAlFinal(p);
        admitirProcesosNuevos();
    }

    private void evaluarDesbloqueosIO() {
        ListaEnlazada<Proceso> desbloqueados = new ListaEnlazada<>();
    
    colaBloqueados.paraCadaElemento(p -> {
        if (p.getMotivoBloqueo() == MotivoBloqueo.OPERACION_IO) {
            if (p.getTiempoBloqueado() >= duracionBloqueoIOCiclos) {
                desbloqueados.agregarAlFinal(p);
            }
        } else if (p.getMotivoBloqueo() == MotivoBloqueo.LATENCIA_RED) {
            // Verifica que haya cumplido la latencia simulada
            if (p.haCumplidoLatenciaRed(clusterManager.getLatenciaRedCiclos())) {
                desbloqueados.agregarAlFinal(p);
            }
        }
    });

    desbloqueados.paraCadaElemento(p -> {
        reinsertarProcesoListo(p);
    });
    }

    private void evaluarPlanificacion(int cicloActual) {
        if (procesoEnCPU != null && planificadorActual.debeDesalojar(procesoEnCPU, colaListos, quantumRestante, cicloActual)) {
            procesoEnCPU.setEstado(EstadoProcesos.LISTO);
            colaListos.agregarAlFinal(procesoEnCPU);
            procesoEnCPU = null;
            quantumRestante = quantumConfigurado;
        }

        if (procesoEnCPU == null && !colaListos.estaVacia()) {
            procesoEnCPU = planificadorActual.seleccionarSiguiente(colaListos, procesoEnCPU, quantumRestante, cicloActual);
            if (procesoEnCPU != null) {
                procesoEnCPU.setEstado(EstadoProcesos.EJECUCION);
                quantumRestante = quantumConfigurado;
            }
        } else if (procesoEnCPU != null && quantumRestante <= 0) {
            quantumRestante = quantumConfigurado;
        }
    }

    private void ejecutarCicloCPU(int cicloActual) {
        if (procesoEnCPU != null) {
            procesoEnCPU.setCicloPrimerEjecucion(cicloActual);

            if (procesoEnCPU.leTocaTocarBuffer() && clusterManager != null) {
                Buffer target = clusterManager.buscarBufferPorId(procesoEnCPU.getIdBuffer());
                if (target != null) {
                    boolean esRemoto = target.esAccesoRemoto(procesoEnCPU);
                    if (esRemoto) {
                        if (!procesoEnCPU.haCumplidoLatenciaRed(clusterManager.getLatenciaRedCiclos())) {
                            String nombreProc = procesoEnCPU.getNombre();
                            desalojarProcesoPorBloqueo(MotivoBloqueo.LATENCIA_RED);
                            registrarLogLocal("Ciclo " + cicloActual + ": Proceso " + nombreProc + " desalojado a BLOQUEADO por Latencia de Red (" + clusterManager.getLatenciaRedCiclos() + " ciclos).");
                            return;
                        }
                        
                    }

                    modoCPU = ModoCPU.SISTEMA_OPERATIVO;
                    procesoEnCPU.ejecutarCiclo();
                    quantumRestante--;

                    ResultadoPasoBuffer resPaso = target.ejecutarCicloPasoBuffer(procesoEnCPU, cicloActual);
                    registrarLogLocal("Ciclo " + cicloActual + ": [MODO SO] " + resPaso.getMensajeLog());

                    if (resPaso.getProcesoDesbloqueado() != null) {
                        Proceso desb = resPaso.getProcesoDesbloqueado();
                        if (desb.getPasoBufferActual() == PasoBuffer.WAIT_CONTEO) {
                            desb.setPasoBufferActual(PasoBuffer.WAIT_MUTEX);
                        }
                        
                        Computador compDueno = clusterManager.buscarComputadorPorId(resPaso.getProcesoDesbloqueado().getComputadorAsignado());
                        if (compDueno != null) {
                            compDueno.reinsertarProcesoListo(resPaso.getProcesoDesbloqueado());
                            registrarLogLocal("Ciclo " + cicloActual + ": Proceso " + resPaso.getProcesoDesbloqueado().getNombre() + " se desbloqueó de semáforo.");
                        }
                    }

                    if (resPaso.isQuedoBloqueado()) {
                        procesoEnCPU.setEstado(EstadoProcesos.BLOQUEADO);
                        procesoEnCPU.setMotivoBloqueo(MotivoBloqueo.ESPERA_SEMAFORO);
                        colaBloqueados.agregarAlFinal(procesoEnCPU);
                        procesoEnCPU = null;
                        return;
                    }

                    evaluarFinalizacionOExpiracion(cicloActual);
                    return;
                }
            }

            modoCPU = ModoCPU.USUARIO;
            procesoEnCPU.ejecutarCiclo();
            quantumRestante--;

            evaluarFinalizacionOExpiracion(cicloActual);
        } else {
            modoCPU = ModoCPU.SISTEMA_OPERATIVO;
        }
    }

    private void evaluarFinalizacionOExpiracion(int cicloActual) {
       if (procesoEnCPU == null) return;
       boolean completoElementos = (procesoEnCPU.getTipo() == TipoProceso.PRODUCTOR || procesoEnCPU.getTipo() == TipoProceso.CONSUMIDOR)
                && procesoEnCPU.getElementosProcesados() >= procesoEnCPU.getElementosRequeridos()
                && procesoEnCPU.getPasoBufferActual() == PasoBuffer.NINGUNO;

        if (procesoEnCPU.haExpiradoDeadline()) {
            String nombreProc = procesoEnCPU.getNombre();
            int idProc = procesoEnCPU.getId();
            Proceso exp = procesoEnCPU;
            procesoEnCPU = null;
            registrarLogLocal("Ciclo " + cicloActual + ": [EXPULSIÓN EN CPU] Proceso " + nombreProc + " (ID: " + idProc + ") superó su deadline.");
            finalizarProcesoExpirado(exp, cicloActual);
        } else if (procesoEnCPU.getTiempoRestante() <= 0 || completoElementos) {
            finalizarProcesoNormal(cicloActual);
        } else if (procesoEnCPU.requiereIO()) {
            desalojarProcesoPorBloqueo(MotivoBloqueo.OPERACION_IO);
        }
    
    }

    private void finalizarProcesoNormal(int cicloActual) {
        procesoEnCPU.setEstado(EstadoProcesos.TERMINADO);
        ramUsada -= procesoEnCPU.getMemoriaRequerida();
        procesoEnCPU.setMemoriaAsignada(false);
        if (clusterManager != null) {
            clusterManager.removerProcesoDeSemaforos(procesoEnCPU);
        }
        metricas.registrarProcesoFinalizado(procesoEnCPU, cicloActual);
        colaTerminados.agregarAlFinal(procesoEnCPU);
        procesoEnCPU = null;
        admitirProcesosNuevos();
    }

    public void desalojarProcesoPorBloqueo(MotivoBloqueo motivo) {
        if (procesoEnCPU != null) {
            procesoEnCPU.setEstado(EstadoProcesos.BLOQUEADO);
            procesoEnCPU.setMotivoBloqueo(motivo);
            colaBloqueados.agregarAlFinal(procesoEnCPU);
            procesoEnCPU = null;
        }
    }

    public void reinsertarProcesoListo(Proceso proceso) {
        if (proceso != null) {
            colaBloqueados.eliminar(proceso);
            proceso.setEstado(EstadoProcesos.LISTO);
            proceso.setMotivoBloqueo(MotivoBloqueo.NINGUNO);
            colaListos.agregarAlFinal(proceso);
        }
    }

    private void registrarLogLocal(String msj) {
        if (clusterManager != null) {
            clusterManager.registrarLog(msj);
        }
    }

    public int getDuracionBloqueoIOCiclos() { return duracionBloqueoIOCiclos; }
    public void setDuracionBloqueoIOCiclos(int duracion) { this.duracionBloqueoIOCiclos = duracion; }

    public int getIdComputador() { return idComputador; }
    public String getNombre() { return nombre; }
    public int getRamTotal() { return ramTotal; }
    public synchronized int getRamUsada() { return ramUsada; }
    public synchronized int getRamLibre() { return ramTotal - ramUsada; }
    public synchronized Proceso getProcesoEnCPU() { return procesoEnCPU; }
    public synchronized ModoCPU getModoCPU() { return modoCPU; }
    public PoliticaPlanificacion getPoliticaActual() { return politicaActual; }
    public int getQuantumConfigurado() { return quantumConfigurado; }
    public void setQuantumConfigurado(int quantum) { this.quantumConfigurado = quantum; }
    public Cola<Proceso> getColaNuevos() { return colaNuevos; }
    public ListaEnlazada<Proceso> getColaListos() { return colaListos; }
    public ListaEnlazada<Proceso> getColaBloqueados() { return colaBloqueados; }
    public ListaEnlazada<Proceso> getColaTerminados() { return colaTerminados; }
    public MetricasComputador getMetricas() { return metricas; }
}
