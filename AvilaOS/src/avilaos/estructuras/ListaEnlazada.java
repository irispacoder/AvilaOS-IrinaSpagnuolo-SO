/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos.estructuras;
import modelo.Proceso;

/**
 *
 * @author ispam
 * @param <T> Tipo de dato almacenado
 */
public class ListaEnlazada<T> {
    private Nodo<T> cabeza;
    private int tamano;

    public ListaEnlazada() {
        this.cabeza = null;
        this.tamano = 0;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public int getTamano() {
        return tamano;
    }

    public void agregarAlFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevo);
        }
        tamano++;
    }
    
    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.setSiguiente(cabeza);
        cabeza = nuevo;
        tamano++;
    }

    public T obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getDato();
    }
    
    public T eliminarPrimero() {
        if (estaVacia()) return null;
        T dato = cabeza.getDato();
        cabeza = cabeza.getSiguiente();
        tamano--;
        return dato;
    }

    public boolean eliminar(T dato) {
        if (estaVacia()) return false;

        if (cabeza.getDato().equals(dato)) {
            cabeza = cabeza.getSiguiente();
            tamano--;
            return true;
        }

        Nodo<T> actual = cabeza;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getDato().equals(dato)) {
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                tamano--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }
    
    public void vaciar() {
        cabeza = null;
        tamano = 0;
    }
    
    // Algoritmos de Ordenamiento Específicos para Proceso
    
    //ordena la lista por prioridad de menor a mayor
    
    public void ordenarPorPrioridad() {
        if (tamano <= 1) return;

        for (Nodo<T> i = cabeza; i != null; i = i.getSiguiente()) {
            for (Nodo<T> j = i.getSiguiente(); j != null; j = j.getSiguiente()) {
                if (i.getDato() instanceof Proceso p1 && j.getDato() instanceof Proceso p2) {
                    if (p2.getPrioridad() < p1.getPrioridad()) {
                        T temp = i.getDato();
                        i.setDato(j.getDato());
                        j.setDato(temp);
                    }
                }
            }
        }
    }
    
    // ordena la lista por menor deadline (EDF)
    
    public void ordenarPorDeadline() {
        if (tamano <= 1) return;

        for (Nodo<T> i = cabeza; i != null; i = i.getSiguiente()) {
            for (Nodo<T> j = i.getSiguiente(); j != null; j = j.getSiguiente()) {
                if (i.getDato() instanceof Proceso p1 && j.getDato() instanceof Proceso p2) {
                    if (p2.getDeadline() < p1.getDeadline()) {
                        T temp = i.getDato();
                        i.setDato(j.getDato());
                        j.setDato(temp);
                    }
                }
            }
        }
    }
}
