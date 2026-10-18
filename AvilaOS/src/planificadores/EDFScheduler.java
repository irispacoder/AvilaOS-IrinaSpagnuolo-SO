/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificadores;

import java.util.Comparator;
import avilaos.estructuras.ListaEnlazada;
import interfaces.IScheduler;
import modelo.Proceso;

/**
 * 
 * @author ispam
 */
public class EDFScheduler implements IScheduler {

    @Override
    public Proceso seleccionarSiguiente(ListaEnlazada<Proceso> colaListos, Proceso procesoEnCPU, int quantumRestante, int cicloActual) {
        if (colaListos == null || colaListos.estaVacia()) {
            return procesoEnCPU;
        }

        colaListos.ordenar(Comparator.comparingInt(Proceso::getDeadline));

        if (procesoEnCPU != null) {
            Proceso masUrgente = colaListos.obtener(0);
            if (masUrgente.getDeadline() < procesoEnCPU.getDeadline()) {
                colaListos.agregarAlFinal(procesoEnCPU);
                colaListos.ordenar(Comparator.comparingInt(Proceso::getDeadline));
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
        colaListos.ordenar(Comparator.comparingInt(Proceso::getDeadline));
        Proceso masUrgente = colaListos.obtener(0);
        return masUrgente.getDeadline() < procesoEnCPU.getDeadline();
    }
}
