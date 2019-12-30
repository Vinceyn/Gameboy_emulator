package ch.epfl.gameboj.component;
/**
 * Interface chargée de représenter un composant piloté par l'horloge du système.
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public interface Clocked {
    
    /**
    * Demande au composant d'évoluer en exécutant toutes les opérations qu'il doit
    * exécuter durant le cycle d'index donné en argument.
    * @param cycle : le cycle d'index donné
    */
    void cycle(long cycle);
}

