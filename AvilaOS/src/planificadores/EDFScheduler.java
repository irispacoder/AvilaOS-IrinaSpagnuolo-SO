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
public class EDFScheduler implements IScheduler{
    @Override
    public Proceso seleccionarSiguiente(ListaEnlazada<Proceso> colaListos, Proceso procesoEnCPU, int quantumRestante) {
        if (colaListos == null || colaListos.estaVacia()) {
            return procesoEnCPU;
        }

        // 1. Ordenamos nuestra lista enlazada propia por menor deadline
        colaListos.ordenarPorDeadline();

        // 2. El proceso con el deadline más urgente es el primero de la lista (índice 0)
        Proceso masUrgente = colaListos.obtener(0);

        // 3. Evaluar apropiación si hay un proceso en ejecución en la CPU
        if (procesoEnCPU != null) {
            if (masUrgente.getDeadline() < procesoEnCPU.getDeadline()) {
                colaListos.eliminarPrimero();            // Extraer el más urgente
                colaListos.agregarAlFinal(procesoEnCPU); // El actual en CPU regresa a listos
                return masUrgente;
            }
            return procesoEnCPU; // Continúa el de la CPU si su deadline es más urgente o igual
        }

        return colaListos.eliminarPrimero();
    }
    
    @Override
    public boolean debeDesalojar(Proceso procesoEnCPU, ListaEnlazada<Proceso> colaListos, int quantumRestante) {
        if (procesoEnCPU == null || colaListos == null || colaListos.estaVacia()) {
            return false;
        }

        // Ordenamos y revisamos si el primero de la cola tiene un deadline menor al de la CPU
        colaListos.ordenarPorDeadline();
        Proceso masUrgente = colaListos.obtener(0);

        return masUrgente.getDeadline() < procesoEnCPU.getDeadline();
    }
}
