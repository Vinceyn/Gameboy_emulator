package ch.epfl.gameboj.component;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

/**
 * Classe chargée de représenter le minuteur du Game Boy
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class Timer implements Component, Clocked {
    private Cpu cpu;
    private int mainTimer = 0;
    private int TIMA = 0;
    private int TMA = 0;
    private int TAC = 0;
    private boolean s0;
    private static final int BIT_1 = 0;
    private static final int BIT_3 = 2;
    private static final int BIT_4 = 3;
    private static final int BIT_6 = 5;
    private static final int BIT_8 = 7;
    private static final int BIT_9 = 8;
    private static final int BIT_10 = 9;
    private static final int LENGTH_1 = 1;
    private static final int LENGTH_2 = 2;
    private static final int LENGTH_8 = 8;
    private static final int LENGTH_16 = 16;
    private static final int MAX8BITS = 0xFF;
    private static final int NB_OF_TIC_IN_A_CYCLE = 4;

    /**
     * Construit un minuteur associé au processeur donné
     * @param cpu : processeur donné
     * @throws NullPointerException si le processeur donné est nul
     */
    public Timer(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;     
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) { 
        s0 = state();
        mainTimer = Bits.clip(LENGTH_16, (mainTimer + NB_OF_TIC_IN_A_CYCLE));
        incIfChange(s0);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        checkBits16(address);
        switch (address) {
        case AddressMap.REG_DIV : {
            return Bits.extract(mainTimer, BIT_9, LENGTH_8);
        }
        case AddressMap.REG_TAC : {
            return TAC;
        }
        case AddressMap.REG_TIMA : {
            return TIMA;
        }
        case AddressMap.REG_TMA : {
            return TMA;
        }
        }
        return NO_DATA;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        checkBits8(data);
        checkBits16(address);
        switch (address) {
        case AddressMap.REG_DIV : {
            boolean s0 = state();
            mainTimer = 0;
            incIfChange(s0);
        } break;
        case AddressMap.REG_TIMA : {
            TIMA = data;
        } break;
        case AddressMap.REG_TMA : {
            TMA = data;
        } break;
        case AddressMap.REG_TAC : {
            boolean s0 = state();
            TAC = data;
            incIfChange(s0);
        }   break;
        }
        cpu.write(address, data);
    }


    /**
     * Donne l'etat du minuteur, qui va servir à incrémenter le compteur secondaire
     * @return l'etat du minuteur
     */
    private boolean state() {
        return Bits.test(TAC, BIT_3) && Bits.test(bitToTestExtracter(), BIT_1);
    }

    /**
     * Donne la valeur du bit du compteur principal désigné par les 2 bits de poids faible du registre TAC
     * @return la valeur du bit du compteur principal
     */
    private int bitToTestExtracter() {
        int bitIndex = BIT_1;
        int indexOfTAC = Bits.clip(LENGTH_2, TAC);
        switch (indexOfTAC) {
        case 0b00 : {
            bitIndex = BIT_10;
        } break;
        case 0b01: {
            bitIndex = BIT_4;
        } break;
        case 0b10 : {
            bitIndex = BIT_6;
        } break ;
        case 0b11 : {
            bitIndex = BIT_8;
        } break;
        }
        return Bits.extract(mainTimer, bitIndex, LENGTH_1);
    }

    /**
     * Selon les changements opérés dans cycle, cette méthode peut incrémenter le compteur secondaire.
     * Lorsque le compteur secondaire est à sa valeur maximale (FF16) et qu'il est incrémenté,
     * le minuteur lève l'interruption TIMER du processeur et le compteur secondaire est réinitialisé à la valeur stockée dans son registre TMA
     * @param previousState l'etat du mainTimer avant le cycle
     */
    private void incIfChange(boolean previousState) {
        if (previousState && !state()) {
            TIMA++;
            if (TIMA > MAX8BITS) {
                TIMA = TMA;
                cpu.requestInterrupt(Cpu.Interrupt.TIMER);
            }
        }
    }
}


