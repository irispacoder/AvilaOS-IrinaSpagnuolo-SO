/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;
import avilaos.estructuras.ListaEnlazada;
import modelo.Proceso;

/**
 *
 * @author ispam
 */
public interface IScheduler {
    Proceso seleccionarSiguiente(ListaEnlazada<Proceso> colaListos, Proceso procesoEnCPU, int quantumRestante);
    boolean debeDesalojar(Proceso procesoEnCPU, ListaEnlazada<Proceso> colaListos, int quantumRestante);
}
