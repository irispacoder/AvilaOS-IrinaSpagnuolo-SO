/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificadores;
import avilaos.estructuras.ListaEnlazada;
import interfaces.IScheduler;
import modelo.Proceso;

/**
 *
 * @author ispam
 */
public class RRScheduler implements IScheduler{
    @Override
    public Proceso seleccionarSiguiente(ListaEnlazada<Proceso> colaListos, Proceso procesoEnCPU, int quantumRestante) {
        if (procesoEnCPU != null && quantumRestante > 0) {
            return procesoEnCPU;
        }        
        if (colaListos == null || colaListos.estaVacia()) {
            return (quantumRestante > 0) ? procesoEnCPU : null;
        }
        return colaListos.eliminarPrimero();
    }
    @Override
    public boolean debeDesalojar(Proceso procesoEnCPU, ListaEnlazada<Proceso> colaListos, int quantumRestante) {        
        return procesoEnCPU != null && quantumRestante <= 0 && colaListos != null && !colaListos.estaVacia();
    }
}
