/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos.estructuras;

/**
 *
 * @author ispam
 * @param <T> Tipp de dato almacenado
 */
public class Cola<T> {
    private final ListaEnlazada<T> lista;

    public Cola() {
        this.lista = new ListaEnlazada<>();
    }

    public boolean estaVacia() {
        return lista.estaVacia();
    }

    public int getTamano() {
        return lista.getTamano();
    }

    public void encolar(T dato) {
        lista.agregarAlFinal(dato);
    }

    public T desencolar() {
        return lista.eliminarPrimero();
    }

    public T frente() {
        if (estaVacia()) return null;
        return lista.obtener(0);
    }

    public boolean eliminar(T dato) {
        return lista.eliminar(dato);
    }
}
