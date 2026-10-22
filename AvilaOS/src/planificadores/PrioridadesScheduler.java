/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificadores;
import java.util.Comparator;
import avilaos.estructuras.ListaEnlazada;
import modelo.Proceso;
import interfaces.IScheduler;

/**
 *
 * @author ispam
 */
public class PrioridadesScheduler implements IScheduler{

    @Override
    public Proceso seleccionarSiguiente(ListaEnlazada<Proceso> colaListos, Proceso procesoEnCPU, int quantumRestante, int cicloActual) {
        if (procesoEnCPU != null) return procesoEnCPU;
        if (colaListos == null || colaListos.estaVacia()) return null;
        colaListos.ordenar(Comparator.comparingInt(Proceso::getPrioridad));
        return colaListos.eliminarPrimero();
    }

    @Override
    public boolean debeDesalojar(Proceso procesoEnCPU, ListaEnlazada<Proceso> colaListos, int quantumRestante, int cicloActual) {
        if (procesoEnCPU == null || colaListos == null || colaListos.estaVacia()) return false;
        colaListos.ordenar(Comparator.comparingInt(Proceso::getPrioridad));
        return colaListos.obtener(0).getPrioridad() < procesoEnCPU.getPrioridad();
    }
}
