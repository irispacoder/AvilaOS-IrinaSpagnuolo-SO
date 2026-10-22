/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos.estructuras;

import java.util.Comparator;
import interfaces.IConsumidor;

/**
 *
 * @author ispam
 * @param <T> Tipo de dato almacenado
 */
public class ListaEnlazada<T> {
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;

    public ListaEnlazada() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }

    public synchronized boolean estaVacia() {
        return cabeza == null;
    }

    public synchronized int getTamano() {
        return tamano;
    }

    public synchronized void agregarAlFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            cola = nuevo;
        }
        tamano++;
    }
    
    public synchronized void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.setSiguiente(cabeza);
        cabeza = nuevo;
        if (cola == null) {
            cola = nuevo;
        }
        tamano++;
    }

    public synchronized T obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getDato();
    }
    
    public synchronized T eliminarPrimero() {
        if (estaVacia()) return null;
        T dato = cabeza.getDato();
        cabeza = cabeza.getSiguiente();
        if (cabeza == null) {
            cola = null;
        }
        tamano--;
        return dato;
    }

    public synchronized boolean eliminar(T dato) {
        if (estaVacia()) return false;

        if (cabeza.getDato().equals(dato)) {
            cabeza = cabeza.getSiguiente();
            if (cabeza == null) {
                cola = null;
            }
            tamano--;
            return true;
        }

        Nodo<T> actual = cabeza;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getDato().equals(dato)) {
                if (actual.getSiguiente() == cola) {
                    cola = actual;
                }
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                tamano--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }
    
    public synchronized void vaciar() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }
    
    
    public synchronized void paraCadaElemento(IConsumidor<T> accion) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            accion.aceptar(actual.getDato());
            actual = actual.getSiguiente();
        }
    }

    public synchronized void ordenar(Comparator<T> comparador) {
    if (tamano <= 1) return;

        Nodo<T> ordenada = null;
        Nodo<T> actual = cabeza;

        while (actual != null) {
            Nodo<T> siguiente = actual.getSiguiente();
            if (ordenada == null || comparador.compare(actual.getDato(), ordenada.getDato()) < 0) {
                actual.setSiguiente(ordenada);
                ordenada = actual;
            } else {
                Nodo<T> temp = ordenada;
                while (temp.getSiguiente() != null && comparador.compare(temp.getSiguiente().getDato(), actual.getDato()) <= 0) {
                    temp = temp.getSiguiente();
                }
                actual.setSiguiente(temp.getSiguiente());
                temp.setSiguiente(actual);
            }
            actual = siguiente;
        }

        cabeza = ordenada;
        Nodo<T> temp = cabeza;
        while (temp != null && temp.getSiguiente() != null) {
            temp = temp.getSiguiente();
        }
        cola = temp;
    }    
}
