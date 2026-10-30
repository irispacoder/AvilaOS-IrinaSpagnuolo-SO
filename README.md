# AvilaOS-IrinaSpagnuolo-SO

ÁvilaOS es un simulador de sistema operativo distribuido que modela la coordinación síncrona de un clúster heterogéneo de computadores en tiempo real.

El sistema abstrae la gestión de procesos monohilo mediante un reloj global unificado, conmutación de privilegios de CPU (Modo Usuario vs. Modo Kernel) y cuatro políticas de planificación de CPU a corto plazo (FCFS, Round Robin con quantum configurable, EDF y Prioridades apropiativas). La comunicación entre nodos implementa el algoritmo del productor-consumidor con buffers acotados y semáforos de Stallings, simulación de latencia de red para accesos remotos y control de admisión según la RAM disponible. Diseñado sin bibliotecas externas de colecciones (java.util), todo el motor utiliza estructuras dinámicas propias e integra una interfaz gráfica interactiva con métricas y gráficas de rendimiento en tiempo real.

Se utilizaron las librerias JFreeChart, que aparecen en repo como documentos añadidos.

Irina Spagnuolo 
