package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;
import ch.epfl.gameboj.component.Component;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

/**
 * Représente les bus d'adresses et de données connectant les composants du GameBoy entre eux
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class Bus {

    private final ArrayList<Component> attachedComponent = new ArrayList<Component>();
    private static final int DEFAULTVALUE = 0xFF;
    
    /**
     * Attache le composant donnée au bus     
     * @param component: composant qu'on souhaite attacher au bus
     * @throws NullPointerException si le composant vaut null
     */
    public void attach(Component component) {
        Objects.requireNonNull(component);
        attachedComponent.add(component);
    }
    
    /**
     * Retourne la valeur stockée à l'adresse donnée si au moins un des composants attaché au bus
     * possède une valeur à cette adresse, ou 0xFF sinon
     * @param address: addresse à laquelle on veut lire la valeur
     * @throws IllegalArgumentException si l'adresse n'est pas une valeur 16 bits
     * @return la valeur stockée à l'adresse donnée si elle existe, sinon NO_DATA
     */
    public int read(int address) {
        checkBits16(address);
        for (Component component: attachedComponent) {
            if (component.read(address) != Component.NO_DATA) {
                return component.read(address);
            }
        }
        return DEFAULTVALUE;               
    }
    
    /**
     * Ecrit la valeur à l'adresse donnée dans tous les composant connectés au bus
     * @param address: adresse à laquelle on veut écrire la valeur
     * @param data: valeur qu'on veut éccrire à l'adresse
     * @throws IllegalArgumentException si l'adresse n'est pas une valeur 16 bits ou si la donnée n'est pas une valeur 8 bits
     */
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        for (Component component : attachedComponent) {
            component.write(address, data);
        }
    }

 }
