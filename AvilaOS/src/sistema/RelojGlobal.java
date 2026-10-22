/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistema;
import avilaos.estructuras.ListaEnlazada;
import interfaces.IClockListener;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author ispam
 */
public class RelojGlobal implements Runnable{
    private final ListaEnlazada<Computador> computadores;
    private int cicloActual;
    private int duracionCicloMs;
    private volatile boolean enEjecucion;
    private volatile boolean pausado;
    private Thread hiloReloj;
    private IClockListener suscriptorClusterManager;

    public RelojGlobal(int duracionCicloMs) {
        this.computadores = new ListaEnlazada<>();
        this.cicloActual = 0;
        this.duracionCicloMs = duracionCicloMs;
        this.enEjecucion = false;
        this.pausado = false;
    }

    public synchronized void agregarComputador(Computador comp) {
        if (comp != null) {
            computadores.agregarAlFinal(comp);
        }
    }

    public synchronized void setSuscriptorClusterManager(IClockListener listener) {
        this.suscriptorClusterManager = listener;
    }

    public synchronized void iniciar() {
        if (!enEjecucion) {
            enEjecucion = true;
            pausado = false;
            computadores.paraCadaElemento(Computador::iniciarHilo);
            hiloReloj = new Thread(this, "Reloj Global Thread");
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
        computadores.paraCadaElemento(Computador::detenerHilo);
        if (hiloReloj != null) {
            hiloReloj.interrupt();
            this.cicloActual = 0;
        }
    }

    @Override
    public void run() {
        while (enEjecucion) {
            try {
                Thread.sleep(duracionCicloMs);
                if (!pausado) {
                    cicloActual++;
                    ejecutarCicloParalelo();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                enEjecucion = false;
            }
        }
    }

    private void ejecutarCicloParalelo() {
        int numComputadores = computadores.getTamano();
        if (numComputadores == 0) return;

        CountDownLatch latch = new CountDownLatch(numComputadores);
        computadores.paraCadaElemento(comp -> comp.dispararCicloParalelo(cicloActual, latch));

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (suscriptorClusterManager != null) {
            suscriptorClusterManager.onClockTick(cicloActual);
        }
    }

    public int getCicloActual() { return cicloActual; }
    public int getDuracionCicloMs() { return duracionCicloMs; }
    public synchronized void setDuracionCicloMs(int duracionCicloMs) {
        if (duracionCicloMs > 0) {
            this.duracionCicloMs = duracionCicloMs;
        }
    }
    public boolean isEnEjecucion() { return enEjecucion; }
    public boolean isPausado() { return pausado; }
}
