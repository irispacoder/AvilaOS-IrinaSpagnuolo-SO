/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificadores;
import avilaos.estructuras.ListaEnlazada;
import modelo.Proceso;
import interfaces.IScheduler;

/**
 *
 * @author ispam
 */
public class PrioridadesScheduler implements IScheduler{

    /**
     *
     * @param colaListos
     * @param procesoEnCPU
     * @param quantumRestante
     * @return
     */
    @Override
    public Proceso seleccionarSiguiente(ListaEnlazada<Proceso> colaListos, Proceso procesoEnCPU, int quantumRestante) {
        if (colaListos == null || colaListos.estaVacia()) {
            return procesoEnCPU;
        }

        // 1. Ordenamos nuestra lista enlazada propia con nuestro método manual
        colaListos.ordenarPorPrioridad();

        // 2. El proceso más prioritario ahora es el primero de la lista (índice 0)
        Proceso masPrioritario = colaListos.obtener(0);

        // 3. Evaluar apropiación (Preemption) si hay alguien ejecutándose en la CPU
        if (procesoEnCPU != null) {
            if (masPrioritario.getPrioridad() < procesoEnCPU.getPrioridad()) {
                colaListos.eliminarPrimero();            // Extraer el de mayor prioridad
                colaListos.agregarAlFinal(procesoEnCPU); // El actual en CPU regresa a listos
                return masPrioritario;
            }
            return procesoEnCPU; // Continúa el de la CPU si su prioridad es igual o mejor
        }

        return colaListos.eliminarPrimero();
    }
    @Override
    public boolean debeDesalojar(Proceso procesoEnCPU, ListaEnlazada<Proceso> colaListos, int quantumRestante) {
        if (procesoEnCPU == null || colaListos == null || colaListos.estaVacia()) {
            return false;
        }

        // Ordenamos y revisamos si el primero de la cola le gana en prioridad al de la CPU
        colaListos.ordenarPorPrioridad();
        Proceso mejorEnCola = colaListos.obtener(0);

        return mejorEnCola.getPrioridad() < procesoEnCPU.getPrioridad();
    }

    
}
