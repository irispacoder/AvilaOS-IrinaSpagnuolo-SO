/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistema;
import avilaos.estructuras.ListaEnlazada;
import enums.MotivoBloqueo;
import modelo.Proceso;

/**
 *
 * @author ispam
 */
public class Buffer {
    private final String idBuffer;
    private final int capacidad;
    private final int idComputadorAnfitrion;

    private final Semaforo mutex;
    private final Semaforo lleno;
    private final Semaforo vacio;

    private int elementosActuales;
    private int totalElementosProducidos;
    private int totalElementosConsumidos;

    public Buffer(String idBuffer, int capacidad, int idComputadorAnfitrion) {
        this.idBuffer = idBuffer;
        this.capacidad = capacidad;
        this.idComputadorAnfitrion = idComputadorAnfitrion;

        this.mutex = new Semaforo(idBuffer + "_MUTEX", 1);
        this.lleno = new Semaforo(idBuffer + "_LLENO", 0);
        this.vacio = new Semaforo(idBuffer + "_VACIO", capacidad);

        this.elementosActuales = 0;
        this.totalElementosProducidos = 0;
        this.totalElementosConsumidos = 0;
    }

    public ResultadoOperacionBuffer producir(Proceso proceso, int latenciaRed) {
        if (esAccesoRemoto(proceso) && !proceso.haCumplidoLatenciaRed(latenciaRed)) {
            proceso.incrementarCiclosLatenciaPendientes();
            return new ResultadoOperacionBuffer(false, new ListaEnlazada<>(), false);
        }

        if (!vacio.semWait(proceso, MotivoBloqueo.ESPERA_SEMAFORO_VACIO)) {
            return new ResultadoOperacionBuffer(false, new ListaEnlazada<>(), true);
        }

        if (!mutex.semWait(proceso, MotivoBloqueo.ESPERA_SEMAFORO_MUTEX)) {
            return new ResultadoOperacionBuffer(false, new ListaEnlazada<>(), true);
        }

        synchronized (this) {
            elementosActuales++;
            totalElementosProducidos++;
        }
        proceso.incrementarElementosProcesados();
        proceso.resetearLatenciaRed();

        ListaEnlazada<Proceso> desbloqueados = new ListaEnlazada<>();
        Proceso p1 = mutex.semSignal();
        Proceso p2 = lleno.semSignal();

        if (p1 != null) desbloqueados.agregarAlFinal(p1);
        if (p2 != null) desbloqueados.agregarAlFinal(p2);

        return new ResultadoOperacionBuffer(true, desbloqueados, false);
    }

    public ResultadoOperacionBuffer consumir(Proceso proceso, int latenciaRed) {
        if (esAccesoRemoto(proceso) && !proceso.haCumplidoLatenciaRed(latenciaRed)) {
            proceso.incrementarCiclosLatenciaPendientes();
            return new ResultadoOperacionBuffer(false, new ListaEnlazada<>(), false);
        }

        if (!lleno.semWait(proceso, MotivoBloqueo.ESPERA_SEMAFORO_LLENO)) {
            return new ResultadoOperacionBuffer(false, new ListaEnlazada<>(), true);
        }

        if (!mutex.semWait(proceso, MotivoBloqueo.ESPERA_SEMAFORO_MUTEX)) {
            return new ResultadoOperacionBuffer(false, new ListaEnlazada<>(), true);
        }

        synchronized (this) {
            elementosActuales--;
            totalElementosConsumidos++;
        }
        proceso.incrementarElementosProcesados();
        proceso.resetearLatenciaRed();

        ListaEnlazada<Proceso> desbloqueados = new ListaEnlazada<>();
        Proceso p1 = mutex.semSignal();
        Proceso p2 = vacio.semSignal();

        if (p1 != null) desbloqueados.agregarAlFinal(p1);
        if (p2 != null) desbloqueados.agregarAlFinal(p2);

        return new ResultadoOperacionBuffer(true, desbloqueados, false);
    }

    public boolean esAccesoRemoto(Proceso proceso) {
        return proceso.getComputadorAsignado() != this.idComputadorAnfitrion;
    }

    public String getIdBuffer() {
        return idBuffer;
    }
    public int getCapacidad() {
        return capacidad;
    }
    public int getIdComputadorAnfitrion() {
        return idComputadorAnfitrion;
    }
    public synchronized int getElementosActuales() {
        return elementosActuales;
    }
    public synchronized int getTotalElementosProducidos() {
        return totalElementosProducidos;
    }
    public synchronized int getTotalElementosConsumidos() {
        return totalElementosConsumidos;
    }
    public Semaforo getMutex() {
        return mutex;
    }
    public Semaforo getLleno() {
        return lleno;
    }
    public Semaforo getVacio() {
        return vacio;
    }

    public static class ResultadoOperacionBuffer {
        private final boolean exito;
        private final ListaEnlazada<Proceso> procesosDesbloqueados;
        private final boolean procesoQuedoBloqueado;

        public ResultadoOperacionBuffer(boolean exito, ListaEnlazada<Proceso> procesosDesbloqueados, boolean procesoQuedoBloqueado) {
            this.exito = exito;
            this.procesosDesbloqueados = procesosDesbloqueados;
            this.procesoQuedoBloqueado = procesoQuedoBloqueado;
        }

        public boolean isExito() {
            return exito;
        }
        public ListaEnlazada<Proceso> getProcesosDesbloqueados() {
            return procesosDesbloqueados;
        }
        public boolean isProcesoQuedoBloqueado() {
            return procesoQuedoBloqueado;
        }
    }
}
