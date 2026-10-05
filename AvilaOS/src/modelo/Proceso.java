/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import enums.EstadoProcesos;
import enums.TipoProceso;

/**
 *
 * @author ispam
 */
public class Proceso {
    private final int id;
    private final String nombre;
    private int computadorAsignado;
    private EstadoProcesos estado;
    private final TipoProceso tipo;
    private final int prioridad;
    private final int memoriaRequerida;
    private int deadline;
    private int tiempoRestante;
    private int pc;

    private String idBuffer;
    private int frecuenciaCiclos;
    private int elementosRequeridos;
    private int elementosProcesados;

    public Proceso(int id, String nombre, TipoProceso tipo, int prioridad, 
                   int memoriaRequerida, int deadline, int instruccionesTotales) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.memoriaRequerida = memoriaRequerida;
        this.deadline = deadline;
        this.tiempoRestante = instruccionesTotales;
        this.estado = EstadoProcesos.NUEVO;
        this.pc = 0;
        this.computadorAsignado = -1;
    }

    public Proceso(int id, String nombre, TipoProceso tipo, int prioridad, 
                   int memoriaRequerida, int deadline, String idBuffer, 
                   int frecuenciaCiclos, int elementosRequeridos) {
        this(id, nombre, tipo, prioridad, memoriaRequerida, deadline, elementosRequeridos * frecuenciaCiclos);
        this.idBuffer = idBuffer;
        this.frecuenciaCiclos = frecuenciaCiclos;
        this.elementosRequeridos = elementosRequeridos;
        this.elementosProcesados = 0;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getComputadorAsignado() { return computadorAsignado; }
    public void setComputadorAsignado(int computadorAsignado) { this.computadorAsignado = computadorAsignado; }
    public EstadoProcesos getEstado() { return estado; }
    public void setEstado(EstadoProcesos estado) { this.estado = estado; }
    public TipoProceso getTipo() { return tipo; }
    public int getPrioridad() { return prioridad; }
    public int getMemoriaRequerida() { return memoriaRequerida; }
    public int getDeadline() { return deadline; }
    public void setDeadline(int deadline) { this.deadline = deadline; }
    public int getTiempoRestante() { return tiempoRestante; }
    public void setTiempoRestante(int tiempoRestante) { this.tiempoRestante = tiempoRestante; }
    public int getPc() { return pc; }
    public void incrementarPc() { this.pc++; }
    public String getIdBuffer() { return idBuffer; }
    public int getFrecuenciaCiclos() { return frecuenciaCiclos; }
    public int getElementosRequeridos() { return elementosRequeridos; }
    public int getElementosProcesados() { return elementosProcesados; }
    public void incrementarElementosProcesados() { this.elementosProcesados++; }
}
