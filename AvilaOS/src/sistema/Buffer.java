/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistema;
import avilaos.estructuras.ListaEnlazada;
import enums.MotivoBloqueo;
import enums.PasoBuffer;
import enums.TipoProceso;
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

    public ResultadoPasoBuffer ejecutarCicloPasoBuffer(Proceso p, int cicloActual) {
        if (p.getPasoBufferActual() == PasoBuffer.NINGUNO) {
            p.setPasoBufferActual(PasoBuffer.WAIT_CONTEO);
        }

        switch (p.getPasoBufferActual()) {
            case WAIT_CONTEO -> {
                Semaforo semConteo = (p.getTipo() == TipoProceso.PRODUCTOR) ? vacio : lleno;
                if (!semConteo.semWait(p, MotivoBloqueo.ESPERA_SEMAFORO)) {
                    return new ResultadoPasoBuffer(false, true, null, "Proceso " + p.getNombre() + " bloqueado en semáforo " + semConteo.getNombre());
                }
                p.setPasoBufferActual(PasoBuffer.WAIT_MUTEX);
                return new ResultadoPasoBuffer(true, false, null, "Proceso " + p.getNombre() + " ejecutó semWait en " + semConteo.getNombre());
            }
            case WAIT_MUTEX -> {
                if (!mutex.semWait(p, MotivoBloqueo.ESPERA_SEMAFORO)) {
                    return new ResultadoPasoBuffer(false, true, null, "Proceso " + p.getNombre() + " bloqueado en semáforo " + mutex.getNombre());
                }
                p.setPasoBufferActual(PasoBuffer.OPERACION);
                return new ResultadoPasoBuffer(true, false, null, "Proceso " + p.getNombre() + " ejecutó semWait en " + mutex.getNombre());
            }
            case OPERACION -> {
                synchronized (this) {
                    if (p.getTipo() == TipoProceso.PRODUCTOR) {
                        elementosActuales++;
                        totalElementosProducidos++;
                    } else {
                        elementosActuales--;
                        totalElementosConsumidos++;
                    }
                    p.incrementarElementosProcesados();
                }
                p.setPasoBufferActual(PasoBuffer.SIGNAL_MUTEX);
                return new ResultadoPasoBuffer(true, false, null, "Proceso " + p.getNombre() + " realizó operación en Buffer " + idBuffer + " (Elem: " + elementosActuales + ")");
            }
            case SIGNAL_MUTEX -> {
                Proceso desbloqueado = mutex.semSignal();
                p.setPasoBufferActual(PasoBuffer.SIGNAL_CONTEO);
                return new ResultadoPasoBuffer(true, false, desbloqueado, "Proceso " + p.getNombre() + " ejecutó semSignal en " + mutex.getNombre());
            }
            case SIGNAL_CONTEO -> {
                Semaforo semConteo = (p.getTipo() == TipoProceso.PRODUCTOR) ? lleno : vacio;
                Proceso desbloqueado = semConteo.semSignal();
                p.setPasoBufferActual(PasoBuffer.NINGUNO);
                p.reiniciarContadorCiclosBuffer();
                p.resetearLatenciaRed();
                return new ResultadoPasoBuffer(true, false, desbloqueado, "Proceso " + p.getNombre() + " ejecutó semSignal en " + semConteo.getNombre() + " y finalizó transacción de Buffer.");
            }
            default -> {
                return new ResultadoPasoBuffer(true, false, null, "OK");
            }
        }
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

    public static class ResultadoPasoBuffer {
        private final boolean exito;
        private final boolean quedoBloqueado;
        private final Proceso procesoDesbloqueado;
        private final String mensajeLog;

        public ResultadoPasoBuffer(boolean exito, boolean quedoBloqueado, Proceso procesoDesbloqueado, String mensajeLog) {
            this.exito = exito;
            this.quedoBloqueado = quedoBloqueado;
            this.procesoDesbloqueado = procesoDesbloqueado;
            this.mensajeLog = mensajeLog;
        }

        public boolean isExito() {
            return exito;
        }
        public boolean isQuedoBloqueado() {
            return quedoBloqueado;
        }
        public Proceso getProcesoDesbloqueado() {
            return procesoDesbloqueado;
        }
        public String getMensajeLog() {
            return mensajeLog;
        }
    }
}
