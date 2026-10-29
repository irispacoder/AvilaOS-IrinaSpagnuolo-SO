/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import enums.EstadoProcesos;
import enums.TipoProceso;
import enums.MotivoBloqueo;
import enums.PasoBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author ispam
 */
public class Proceso {
    private static final AtomicInteger contadorIdGlobal = new AtomicInteger(1);
    
    private final int id;
    private final String nombre;
    private int computadorAsignado;
    private EstadoProcesos estado;
    private MotivoBloqueo motivoBloqueo;
    private final TipoProceso tipo;
    private final int prioridad;
    private final int memoriaRequerida;
    private int deadline;
    private int tiempoRestante;
    private final int instruccionesTotales;
    private int pc;
    private int mar;
    private int tiempoBloqueado;
    private int tiempoBloqueadoSemaforo;
    private int tiempoEsperaListos;
    private boolean memoriaAsignada;
    
    private int cicloLlegada;
    private int cicloPrimerEjecucion;
    private int cicloFin;

    private String idBuffer;
    private int frecuenciaCiclos;
    private int contadorCiclosBuffer;
    private int elementosRequeridos;
    private int elementosProcesados;
    private int ciclosLatenciaPendientes;
    private int ciclosEjecutadosDesdeIO;
    private int intervaloIOCiclos;
    private PasoBuffer pasoBufferActual;

    public Proceso(String nombre, TipoProceso tipo, int prioridad, 
                   int memoriaRequerida, int deadline, int instruccionesTotales) {
        this.id = contadorIdGlobal.getAndIncrement();
        this.nombre = nombre;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.memoriaRequerida = memoriaRequerida;
        this.deadline = deadline;
        this.instruccionesTotales = instruccionesTotales;
        this.tiempoRestante = instruccionesTotales;
        this.estado = EstadoProcesos.NUEVO;
        this.motivoBloqueo = MotivoBloqueo.NINGUNO;
        this.pc = 0;
        this.mar = 0;
        this.computadorAsignado = -1;
        this.tiempoBloqueado = 0;
        this.tiempoBloqueadoSemaforo = 0;
        this.tiempoEsperaListos = 0;
        this.memoriaAsignada = false;
        this.ciclosLatenciaPendientes = 0;
        this.cicloLlegada = 0;
        this.cicloPrimerEjecucion = -1;
        this.cicloFin = -1;
        this.contadorCiclosBuffer = 0;
        this.ciclosEjecutadosDesdeIO = 0;
        this.intervaloIOCiclos = 5;
        this.pasoBufferActual = PasoBuffer.NINGUNO;
    }
    
    

    public Proceso(String nombre, TipoProceso tipo, int prioridad, 
                   int memoriaRequerida, int deadline, String idBuffer, 
                   int frecuenciaCiclos, int elementosRequeridos) {
        this(nombre, tipo, prioridad, memoriaRequerida, deadline, Integer.MAX_VALUE);
        this.idBuffer = idBuffer;
        this.frecuenciaCiclos = frecuenciaCiclos;
        this.elementosRequeridos = elementosRequeridos;
        this.elementosProcesados = 0;
        this.contadorCiclosBuffer = 0;
    }
    
    public void ejecutarCiclo() {
        if (this.tiempoRestante > 0) {
            this.tiempoRestante--;
            this.pc++;
            this.mar++;
            this.ciclosEjecutadosDesdeIO++;
            if ((this.tipo == TipoProceso.PRODUCTOR || this.tipo == TipoProceso.CONSUMIDOR) && pasoBufferActual == PasoBuffer.NINGUNO) {
                this.contadorCiclosBuffer++;
            }
        }
    }
    
    public boolean leTocaTocarBuffer() {
        if (tipo != TipoProceso.PRODUCTOR && tipo != TipoProceso.CONSUMIDOR) return false;
        if (elementosProcesados >= elementosRequeridos && pasoBufferActual == PasoBuffer.NINGUNO) {
        return false;
    }
        return contadorCiclosBuffer >= frecuenciaCiclos || pasoBufferActual != PasoBuffer.NINGUNO;
    }
    
    public void reiniciarContadorCiclosBuffer() {
        this.contadorCiclosBuffer = 0;
        this.pasoBufferActual = PasoBuffer.NINGUNO;
    }

    public void decrementarDeadline() {
        if (this.deadline > 0) {
            this.deadline--;
        }
    }
    
    public boolean requiereIO() {
        if (this.tipo == TipoProceso.IO_BOUND && this.tiempoRestante > 0 && this.ciclosEjecutadosDesdeIO >= intervaloIOCiclos) {
            this.ciclosEjecutadosDesdeIO = 0;
            return true;
        }
        return false;
    }

    public void resetearTiempoBloqueado() {
        this.tiempoBloqueado = 0;
    }
    
    public String getInstruccionActual() {
        if (tiempoRestante <= 0) return "NOP (FINALIZADO)";
        if (pasoBufferActual != PasoBuffer.NINGUNO) {
            switch (pasoBufferActual) {
                case WAIT_CONTEO -> {
                    return "SEM_WAIT(" + (tipo == TipoProceso.PRODUCTOR ? "VACIO" : "LLENO") + ")";
                }
                case WAIT_MUTEX -> {
                    return "SEM_WAIT(MUTEX)";
                }
                case OPERACION -> {
                    return (tipo == TipoProceso.PRODUCTOR ? "PRODUCIR_ELEM [" : "CONSUMIR_ELEM [") + (elementosProcesados + 1) + "]";
                }
                case SIGNAL_MUTEX -> {
                    return "SEM_SIGNAL(MUTEX)";
                }
                case SIGNAL_CONTEO -> {
                    return "SEM_SIGNAL(" + (tipo == TipoProceso.PRODUCTOR ? "LLENO" : "VACIO") + ")";
                }
                default -> {
                }
            }
        }
        switch (tipo) {
            case PRODUCTOR: return "PREPARAR_PRODUCCION_PC_" + pc;
            case CONSUMIDOR: return "PREPARAR_CONSUMO_PC_" + pc;
            case IO_BOUND: return "IO_REQUEST_PC_" + pc;
            case CPU_BOUND: default: return "EXEC_INSTR_PC_" + pc;
        }
    }

    public String getFormatoPC() {
        return "PC: 0x" + String.format("%04X", pc) + " (" + getInstruccionActual() + ")";
    }
    
    public boolean haExpiradoDeadline() {
        return this.deadline <= 0;
    }

    public void incrementarTiempoBloqueado() {
        this.tiempoBloqueado++;
    }
    public void incrementarTiempoBloqueadoSemaforo() {
        this.tiempoBloqueadoSemaforo++;
    }
    public void incrementarTiempoEsperaListos() {
        this.tiempoEsperaListos++;
    }
    public boolean haCumplidoLatenciaRed(int latenciaConfigurada) {
        return ciclosLatenciaPendientes >= latenciaConfigurada;
    }
    public void incrementarCiclosLatenciaPendientes() {
        this.ciclosLatenciaPendientes++;
    }
    public void resetearLatenciaRed() {
        this.ciclosLatenciaPendientes = 0;
    }

    public PasoBuffer getPasoBufferActual() {
        return pasoBufferActual;
    }
    public void setPasoBufferActual(PasoBuffer paso) {
        this.pasoBufferActual = paso;
    }

    
    
    //getters y setters
    
    public int getTiempoBloqueadoSemaforo() {
        return tiempoBloqueadoSemaforo;
    }
    public int getIntervaloIOCiclos() {
        return intervaloIOCiclos;
    }
    public void setIntervaloIOCiclos(int intervaloIOCiclos) {
        this.intervaloIOCiclos = intervaloIOCiclos;
    }

    public int getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public int getComputadorAsignado() {
        return computadorAsignado;
    }
    public void setComputadorAsignado(int computadorAsignado) {
        this.computadorAsignado = computadorAsignado;
    }
    public EstadoProcesos getEstado() {
        return estado;
    }
    public void setEstado(EstadoProcesos estado) {
        this.estado = estado;
    }
    public MotivoBloqueo getMotivoBloqueo() {
        return motivoBloqueo;
    }
    public void setMotivoBloqueo(MotivoBloqueo motivoBloqueo) {
        this.motivoBloqueo = motivoBloqueo;
    }
    public TipoProceso getTipo() {
        return tipo;
    }
    public int getPrioridad() {
        return prioridad;
    }
    public int getMemoriaRequerida() {
        return memoriaRequerida;
    }
    public int getDeadline() {
        return deadline;
    }
    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }
    public int getTiempoRestante() {
        return tiempoRestante;
    }
    public void setTiempoRestante(int tiempoRestante) {
        this.tiempoRestante = tiempoRestante;
    }
    public int getInstruccionesTotales() {
        return instruccionesTotales;
    }
    public int getPc() {
        return pc;
    }
    public int getMar() {
        return mar;
    }
    public int getTiempoBloqueado() {
        return tiempoBloqueado;
    }
    public int getTiempoEsperaListos() {
        return tiempoEsperaListos;
    }
    public boolean isMemoriaAsignada() {
        return memoriaAsignada;
    }
    public void setMemoriaAsignada(boolean memoriaAsignada) {
        this.memoriaAsignada = memoriaAsignada;
    }
    public String getIdBuffer() {
        return idBuffer;
    }
    public int getFrecuenciaCiclos() {
        return frecuenciaCiclos;
    }
    public int getElementosRequeridos() {
        return elementosRequeridos;
    }
    public int getElementosProcesados() {
        return elementosProcesados;
    }
    public void incrementarElementosProcesados() {
        this.elementosProcesados++;
    }
    public int getCicloLlegada() {
        return cicloLlegada;
    }
    public void setCicloLlegada(int cicloLlegada) {
        this.cicloLlegada = cicloLlegada;
    }
    public int getCicloPrimerEjecucion() {
        return cicloPrimerEjecucion;
    }
    public void setCicloPrimerEjecucion(int ciclo) {
        if (this.cicloPrimerEjecucion == -1) this.cicloPrimerEjecucion = ciclo;
    }
    public int getCicloFin() {
        return cicloFin;
    }
    public void setCicloFin(int cicloFin) {
        this.cicloFin = cicloFin;
    }
}
