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
public class FCFSScheduler implements IScheduler{
    @Override
    public Proceso seleccionarSiguiente(ListaEnlazada<Proceso> colaListos, Proceso procesoEnCPU, int quantumRestante){
        if (procesoEnCPU != null){
            return procesoEnCPU;
        }
        if (colaListos == null || colaListos.estaVacia()){
            return null;
        }
        return colaListos.eliminarPrimero();
    }
    @Override
    public boolean debeDesalojar(Proceso procesoEnCPU, ListaEnlazada<Proceso> colaListos, int quantumRestante){
        return false; //fcfs nunca desaloja por eventos de llegada
    }
}
