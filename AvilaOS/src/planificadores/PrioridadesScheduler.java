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
        if (colaListos == null || colaListos.estaVacia()) {
            return procesoEnCPU;
        }

        colaListos.ordenar(Comparator.comparingInt(Proceso::getPrioridad));

        if (procesoEnCPU != null) {
            Proceso masPrioritario = colaListos.obtener(0);
            if (masPrioritario.getPrioridad() < procesoEnCPU.getPrioridad()) {
                colaListos.agregarAlFinal(procesoEnCPU);
                colaListos.ordenar(Comparator.comparingInt(Proceso::getPrioridad));
                return colaListos.eliminarPrimero();
            }
            return procesoEnCPU;
        }

        return colaListos.eliminarPrimero();
    }

    @Override
    public boolean debeDesalojar(Proceso procesoEnCPU, ListaEnlazada<Proceso> colaListos, int quantumRestante, int cicloActual) {
        if (procesoEnCPU == null || colaListos == null || colaListos.estaVacia()) {
            return false;
        }
        colaListos.ordenar(Comparator.comparingInt(Proceso::getPrioridad));
        Proceso mejorEnCola = colaListos.obtener(0);
        return mejorEnCola.getPrioridad() < procesoEnCPU.getPrioridad();
    }
}
