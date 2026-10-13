/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistema;
import avilaos.estructuras.ListaEnlazada;
import interfaces.IClockListener;

/**
 *
 * @author ispam
 */
public class RelojGlobal implements Runnable{
    private final ListaEnlazada<IClockListener> suscriptores;
    private int cicloActual;
    private int duracionCicloMs; // Tiempo en milisegundos por ciclo
    private volatile boolean enEjecucion;
    private volatile boolean pausado;
    private Thread hiloReloj;

    public RelojGlobal(int duracionCicloMs) {
        this.suscriptores = new ListaEnlazada<>();
        this.cicloActual = 0;
        this.duracionCicloMs = duracionCicloMs;
        this.enEjecucion = false;
        this.pausado = false;
    }

    public synchronized void agregarSuscriptor(IClockListener suscriptor) {
        if (suscriptor != null) {
            suscriptores.agregarAlFinal(suscriptor);
        }
    }

    public synchronized void removerSuscriptor(IClockListener suscriptor) {
        if (suscriptor != null) {
            suscriptores.eliminar(suscriptor);
        }
    }

    public synchronized void iniciar() {
        if (!enEjecucion) {
            enEjecucion = true;
            pausado = false;
            hiloReloj = new Thread(this, "RelojGlobalThread");
            hiloReloj.start();
        } else if (pausado) {
            pausado = false;
        }
    }

    public synchronized void pausar() {
        this.pausado = true;
    }

    public synchronized void detener() {
        this.enEjecucion = false;
        this.pausado = false;
        if (hiloReloj != null) {
            hiloReloj.interrupt();
        }
        this.cicloActual = 0;
    }

    @Override
    public void run() {
        while (enEjecucion) {
            try {
                Thread.sleep(duracionCicloMs);

                if (!pausado) {
                    cicloActual++;
                    notificarSuscriptores();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                enEjecucion = false;
            }
        }
    }

    private synchronized void notificarSuscriptores() {
        int total = suscriptores.getTamano();
        for (int i = 0; i < total; i++) {
            IClockListener suscriptor = suscriptores.obtener(i);
            if (suscriptor != null) {
                suscriptor.onClockTick(cicloActual);
            }
        }
    }

    // --- Getters y Setters ---

    public int getCicloActual() {
        return cicloActual;
    }

    public int getDuracionCicloMs() {
        return duracionCicloMs;
    }

    public synchronized void setDuracionCicloMs(int duracionCicloMs) {
        if (duracionCicloMs > 0) {
            this.duracionCicloMs = duracionCicloMs;
        }
    }

    public boolean isEnEjecucion() {
        return enEjecucion;
    }

    public boolean isPausado() {
        return pausado;
    }
}
