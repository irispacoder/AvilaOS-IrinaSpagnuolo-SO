/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;
import avilaos.estructuras.Cola;
import modelo.Proceso;

/**
 *
 * @author ispam
 */
public interface IScheduler {
    Proceso seleccionarSig(Cola<Proceso> colaListos);
}
