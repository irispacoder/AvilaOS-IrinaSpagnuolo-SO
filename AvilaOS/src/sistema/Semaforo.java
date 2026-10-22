/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistema;
import avilaos.estructuras.Cola;
import avilaos.estructuras.ListaEnlazada;
import enums.EstadoProcesos;
import enums.MotivoBloqueo;
import modelo.Proceso;

/**
 *
 * @author ispam
 */
public class Semaforo {
    private final String nombre;
    private int valor;
    private final Cola<Proceso> colaBloqueados;

    public Semaforo(String nombre, int valorInicial) {
        this.nombre = nombre;
        this.valor = valorInicial;
        this.colaBloqueados = new Cola<>();
    }

    public synchronized boolean semWait(Proceso proceso, MotivoBloqueo motivo) {
        valor--;
        if (valor < 0) {
            if (proceso != null) {
                proceso.setEstado(EstadoProcesos.BLOQUEADO);
                proceso.setMotivoBloqueo(motivo);
                colaBloqueados.encolar(proceso);
            }
            return false;
        }
        return true;
    }

    public synchronized Proceso semSignal() {
        valor++;
        Proceso desbloqueado = null;

        if (valor <= 0 && !colaBloqueados.estaVacia()) {
            desbloqueado = colaBloqueados.desencolar();
            if (desbloqueado != null && desbloqueado.getEstado() != EstadoProcesos.TERMINADO) {
                desbloqueado.setEstado(EstadoProcesos.LISTO);
                desbloqueado.setMotivoBloqueo(MotivoBloqueo.NINGUNO);
                return desbloqueado;
            }
        }
        return null;
    }

    public synchronized boolean eliminarProceso(Proceso p) {
        return colaBloqueados.eliminar(p);
    }
    
    public String getNombre() {
        return nombre;
    }
    public synchronized int getValor() {
        return valor;
    }

    public synchronized ListaEnlazada<Proceso> obtenerCopiaColaBloqueados() {
        ListaEnlazada<Proceso> copia = new ListaEnlazada<>();
        ListaEnlazada<Proceso> interna = colaBloqueados.getListaInterna();
        interna.paraCadaElemento(copia::agregarAlFinal);
        return copia;
    }
}
