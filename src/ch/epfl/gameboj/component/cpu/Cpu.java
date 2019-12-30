package ch.epfl.gameboj.component.cpu;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.memory.Ram;
/**
 * Classe chargée de représenter le processeur de la gameboy
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class Cpu implements Component, Clocked {
    private Ram highRam = new Ram(AddressMap.HIGH_RAM_SIZE);
    private int PC;
    private int SP;
    private long nextNonIdleCycle;
    private Bus bus;
    private int IE;
    public int IF;
    private boolean IME;
    private RegisterFile<Register> regFile = new RegisterFile<>(Reg.values());
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.PREFIXED);
    private static final int BIT_1 = 0;
    private static final int BIT_4 = 3;
    private static final int BIT_5 = 4;
    private static final int BIT_7 = 6;
    private static final int BIT_9 = 8;
    private static final int LENGTH_2 = 2;
    private static final int LENGTH_3 = 3;
    private static final int LENGTH_4 = 4;
    private static final int LENGTH_8 = 8;
    private static final int LENGTH_16 = 16;
    private static final int NB_OF_OPCODE = 256;
    private static final int PREFIXED_OPCODE_ENCODING = 0xCB;
    private static final int NNIC_INCREMENTATION_IF_INTERRUPTION = 5;
    enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }
    private enum Reg16 implements Register {
        AF(Reg.A, Reg.F), BC(Reg.B, Reg.C), DE(Reg.D, Reg.E), HL(Reg.H, Reg.L);
        Reg firstReg;
        Reg secondReg;
        private Reg16(Reg firstReg, Reg secondReg) {
            this.firstReg = firstReg;
            this.secondReg = secondReg;
        }
    }
    private enum FlagSrc {
        V0, V1, ALU, CPU
    }
    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
    }

    /**
     * Construit un tableau de familles indexé par les 256 opcodes possibles
     * @param kind : la famille voulue pour le tableau créé
     * @return opcodeTable : un tableau de familles indexé par les 256 opcodes possibles
     */
    private static Opcode[] buildOpcodeTable(Opcode.Kind kind) {
        Opcode[] opcodeTable = new Opcode[NB_OF_OPCODE];
        for (Opcode o : Opcode.values()) {
            if (o.kind.equals(kind)) {
                opcodeTable[o.encoding] = o;
            }
        }
        return opcodeTable;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     * Détermine si oui ou non le processeur doit faire quelque chose durant ce cycle
     */
    @Override
    public void cycle(long cycle) {
        if (nextNonIdleCycle == Long.MAX_VALUE && ((IE & IF) != 0)) {
            nextNonIdleCycle = cycle;
            reallyCycle();
        }
        if (cycle == nextNonIdleCycle) {
            reallyCycle();
        }
        return;
    }
    
    /**
     * Regarde si les interruptions sont activées et si une interruption est en attente,
     * auquel cas elle la gère ; sinon, elle exécute normalement la prochaine instruction.
     */
    private void reallyCycle() {
        Opcode opcode;
        if (toInterrupt()) {
            IME = false;
            int i = getInterruptIndex();
            IF = Bits.set(IF, i, false);
            push16(PC);
            PC = AddressMap.INTERRUPTS[i];
            nextNonIdleCycle += NNIC_INCREMENTATION_IF_INTERRUPTION;                                                                          
        }
        else {
            int pcVal = read8(PC);
            if (pcVal == PREFIXED_OPCODE_ENCODING) {
                opcode = PREFIXED_OPCODE_TABLE[read8AfterOpcode()];
            }
            else {
                opcode = DIRECT_OPCODE_TABLE[read8(PC)];
            }
            dispatch(opcode);
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        checkBits16(address);
        if (address == AddressMap.REG_IE) {
            return IE;
        }
        if (address == AddressMap.REG_IF) {
            return IF;
        }
        if (toUseHighRamCondition(address)) {
            return highRam.read(address - AddressMap.HIGH_RAM_START);
        }
        return NO_DATA;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        if (address == AddressMap.REG_IE) {
            IE = data;
            return;
        }
        if (address == AddressMap.REG_IF) {
            IF = data;
            return;
        }
        if (toUseHighRamCondition(address)) {
            highRam.write(address - AddressMap.HIGH_RAM_START, data);
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }

    /**
     * methode dont le seul but est de faciliter les tests
     * @return un tableau contenant, dans l'ordre, la valeur des registres PC, SP, A, F, B, C, D, E, H et L.
     */
    public int[] _testGetPcSpAFBCDEHL() {
        int[] regValTable = new int[10];
        regValTable[0] = PC;
        regValTable[1] = SP;
        regValTable[2] = regFile.get(Reg.A);
        regValTable[3] = regFile.get(Reg.F);
        regValTable[4] = regFile.get(Reg.B);
        regValTable[5] = regFile.get(Reg.C);
        regValTable[6] = regFile.get(Reg.D);
        regValTable[7] = regFile.get(Reg.E);
        regValTable[8] = regFile.get(Reg.H);
        regValTable[9] = regFile.get(Reg.L);
        return regValTable;
    }

    /**
     * Etant donné un octet contenant un opcode, exécute l'instruction correspondante
     * @param opcode : un opcode portant une instruction
     */
    private void dispatch(Opcode opcode) {
        int nextPC = PC + opcode.totalBytes;
        nextNonIdleCycle += opcode.cycles;
        switch(opcode.family) {
        case NOP: { //No operation
        } break;
        // instructions de chargement
        case LD_R8_HLR : {
            Reg opcodeReg = extractReg(opcode, 3);
            regFile.set(opcodeReg, read8AtHl());
        } break;
        case LD_A_HLRU : {
            regFile.set(Reg.A, read8AtHl());
            setReg16(Reg16.HL, Bits.clip(LENGTH_16, reg16(Reg16.HL) + extractHlIncrement(opcode)));
        } break;
        case LD_A_N8R: {
            regFile.set(Reg.A, read8(AddressMap.REGS_START + read8AfterOpcode()));
        } break;
        case LD_A_CR: {
            regFile.set(Reg.A, read8(AddressMap.REGS_START + regFile.get(Reg.C)));
        } break;
        case LD_A_N16R: {
            regFile.set(Reg.A, read8(read16AfterOpcode()));
        } break;
        case LD_A_BCR: {
            regFile.set(Reg.A, read8(reg16(Reg16.BC)));
        } break;
        case LD_A_DER: {
            regFile.set(Reg.A, read8(reg16(Reg16.DE)));
        } break;
        case LD_R8_N8: {
            Reg opcodeReg = extractReg(opcode, 3);
            regFile.set(opcodeReg, read8AfterOpcode());
        } break;
        case LD_R16SP_N16: {
            Reg16 opcodeReg = extractReg16(opcode);
            setReg16SP(opcodeReg, read16AfterOpcode());
        } break;
        case POP_R16: {
            Reg16 opcodeReg = extractReg16(opcode);
            setReg16(opcodeReg, pop16());
        } break;
        // instructions de stockage
        case LD_HLR_R8: {   
            Reg opcodeReg = extractReg(opcode, BIT_1);
            write8AtHl(regFile.get(opcodeReg));
        } break;
        case LD_HLRU_A: {
            write8AtHl(regFile.get(Reg.A));
            setReg16(Reg16.HL, Bits.clip(LENGTH_16, reg16(Reg16.HL) + extractHlIncrement(opcode)));
        } break;
        case LD_N8R_A: {
            write8(AddressMap.REGS_START + read8AfterOpcode(), regFile.get(Reg.A)); 
        } break;
        case LD_CR_A: {
            write8(AddressMap.REGS_START + regFile.get(Reg.C), regFile.get(Reg.A));
        } break;
        case LD_N16R_A: {   
            write8(read16AfterOpcode(), regFile.get(Reg.A));
        } break;
        case LD_BCR_A: {
            write8(reg16(Reg16.BC), regFile.get(Reg.A));
        } break;
        case LD_DER_A: {
            write8(reg16(Reg16.DE), regFile.get(Reg.A));
        } break;
        case LD_HLR_N8: {
            write8AtHl(read8AfterOpcode());
        } break;
        case LD_N16R_SP: {
            write16(read16AfterOpcode(), SP);
        } break;
        case PUSH_R16 : {
            Reg16 opcodeReg = extractReg16(opcode);
            push16(reg16(opcodeReg));
        } break;
        // instructions de copie
        case LD_R8_R8 : {
            Reg opcodeRegS = extractReg(opcode, BIT_1);
            Reg opcodeRegR = extractReg(opcode, BIT_4);
            if (opcodeRegS != opcodeRegR) {
                regFile.set(opcodeRegR, regFile.get(opcodeRegS));
            }
        } break;
        case LD_SP_HL: {
            setReg16SP(Reg16.AF, reg16(Reg16.HL));  
        } break;
        // Add
        case ADD_A_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(Reg.A, Alu.add(regFile.get(Reg.A), regFile.get(opcodeReg), carryInitVal(opcode)));
        } break;
        case ADD_A_N8: {
            setRegFlags(Reg.A, Alu.add(regFile.get(Reg.A), read8AfterOpcode(), carryInitVal(opcode)));
        } break;
        case ADD_A_HLR: {
            setRegFlags(Reg.A, Alu.add(regFile.get(Reg.A), read8AtHl(), carryInitVal(opcode)));
        } break;
        case INC_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_4);
            int valueFlags = Alu.add(regFile.get(opcodeReg), 1);
            setRegFromAlu(opcodeReg, valueFlags);
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
        } break;
        case INC_HLR: {
            int valueFlags = Alu.add(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
        } break;
        case INC_R16SP: {
            Reg16 opcodeReg = extractReg16(opcode);
            int value = Alu.add16H(getReg16Sp(opcodeReg), 1);
            setReg16SP(opcodeReg, Alu.unpackValue(value));
        } break;
        case ADD_HL_R16SP: {    
            Reg16 opcodeReg = extractReg16(opcode);         
            int value = Alu.add16H(reg16(Reg16.HL), getReg16Sp(opcodeReg));
            setReg16(Reg16.HL, Alu.unpackValue(value));
            combineAluFlags(value, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
        } break;
        case LD_HLSP_S8: {
            addForLD_HLSP_S8(opcode);
        } break;
        // Subtract
        case SUB_A_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(Reg.A, Alu.sub(regFile.get(Reg.A), regFile.get(opcodeReg), carryInitVal(opcode)));
        } break;
        case SUB_A_N8: {
            setRegFlags(Reg.A, Alu.sub(regFile.get(Reg.A), read8AfterOpcode(), carryInitVal(opcode)));
        } break;
        case SUB_A_HLR: {
            setRegFlags(Reg.A, Alu.sub(regFile.get(Reg.A), read8AtHl(), carryInitVal(opcode)));
        } break;
        case DEC_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_4);
            int valueFlags = Alu.sub(regFile.get(opcodeReg), 1);
            setRegFromAlu(opcodeReg, valueFlags);
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
        } break;
        case DEC_HLR: {
            int valueFlags = Alu.sub(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
        } break;
        case CP_A_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setFlags(Alu.sub(regFile.get(Reg.A), regFile.get(opcodeReg)));
        } break;
        case CP_A_N8: {
            setFlags(Alu.sub(regFile.get(Reg.A), read8AfterOpcode()));
        } break;
        case CP_A_HLR: {
            setFlags(Alu.sub(regFile.get(Reg.A), read8AtHl()));
        } break;
        case DEC_R16SP: {
            Reg16 opcodeReg = extractReg16(opcode);
            setReg16SP(opcodeReg, Bits.clip(LENGTH_16, toDecrementForDEC_R16SP(opcode)));
        } break;
        // And, or, xor, complement
        case AND_A_N8: {
            setRegFlags(Reg.A, Alu.and(regFile.get(Reg.A), read8AfterOpcode()));
        } break;
        case AND_A_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(Reg.A, Alu.and(regFile.get(Reg.A), regFile.get(opcodeReg)));
        } break;
        case AND_A_HLR: {
            setRegFlags(Reg.A, Alu.and(regFile.get(Reg.A), read8AtHl()));
        } break;
        case OR_A_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(Reg.A, Alu.or(regFile.get(Reg.A), regFile.get(opcodeReg)));
        } break;
        case OR_A_N8: {
            setRegFlags(Reg.A, Alu.or(regFile.get(Reg.A), read8AfterOpcode()));
        } break;
        case OR_A_HLR: {
            setRegFlags(Reg.A, Alu.or(regFile.get(Reg.A), read8AtHl()));
        } break;
        case XOR_A_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(Reg.A, Alu.xor(regFile.get(Reg.A), regFile.get(opcodeReg)));
        } break;
        case XOR_A_N8: {
            setRegFlags(Reg.A, Alu.xor(regFile.get(Reg.A), read8AfterOpcode()));
        } break;
        case XOR_A_HLR: {
            setRegFlags(Reg.A, Alu.xor(regFile.get(Reg.A), read8AtHl()));
        } break;
        case CPL: {
            regFile.set(Reg.A, Bits.complement8(regFile.get(Reg.A)));
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1, FlagSrc.CPU);
        } break;
        // Rotate, shift
        case ROTCA: {
            int valueFlags = Alu.rotate(rotDir(opcode), regFile.get(Reg.A));
            regFile.set(Reg.A, Alu.unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
        } break;
        case ROTA: {
            int valueFlags = Alu.rotate(rotDir(opcode), regFile.get(Reg.A), extractFlag(Alu.Flag.C));
            regFile.set(Reg.A, Alu.unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
        } break;
        case ROTC_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(opcodeReg, Alu.rotate(rotDir(opcode), regFile.get(opcodeReg)));
        } break;
        case ROT_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(opcodeReg, Alu.rotate(rotDir(opcode), regFile.get(opcodeReg), extractFlag(Alu.Flag.C)));
        } break;
        case ROTC_HLR: {
            write8AtHlAndSetFlags(Alu.rotate(rotDir(opcode), read8AtHl()));
        } break;
        case ROT_HLR: {
            write8AtHlAndSetFlags(Alu.rotate(rotDir(opcode), read8AtHl(), extractFlag(Alu.Flag.C)));
        } break;
        case SWAP_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(opcodeReg, Alu.swap(regFile.get(opcodeReg)));
        } break;
        case SWAP_HLR: {
            write8AtHlAndSetFlags(Alu.swap(read8AtHl()));
        } break;
        case SLA_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(opcodeReg, Alu.shiftLeft(regFile.get(opcodeReg)));
        } break;
        case SRA_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(opcodeReg, Alu.shiftRightA(regFile.get(opcodeReg)));
        } break;
        case SRL_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            setRegFlags(opcodeReg, Alu.shiftRightL(regFile.get(opcodeReg)));
        } break;
        case SLA_HLR: {
            int value = Alu.shiftLeft(read8AtHl());
            write8AtHlAndSetFlags(value);
        } break;
        case SRA_HLR: {
            int value = Alu.shiftRightA(read8AtHl());
            write8AtHlAndSetFlags(value);
        } break;
        case SRL_HLR: {
            int value = Alu.shiftRightL(read8AtHl());
            write8AtHlAndSetFlags(value);
        } break;
        // Bit test and set
        case BIT_U3_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            combineAluFlags(Alu.testBit(regFile.get(opcodeReg), extractBitIndex(opcode)), FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
        } break;
        case BIT_U3_HLR: {
            combineAluFlags(Alu.testBit(read8AtHl(), extractBitIndex(opcode)), FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
        } break;
        //peut etre optimisee je pense
        case CHG_U3_R8: {
            Reg opcodeReg = extractReg(opcode, BIT_1);
            if (isSET(opcode)) {
                regFile.set(opcodeReg, regFile.get(opcodeReg) | valToAttribute(opcode, isSET(opcode)) );
            }
            else {
                regFile.set(opcodeReg, regFile.get(opcodeReg) & valToAttribute(opcode, isSET(opcode)) );
            }
        } break;
        case CHG_U3_HLR: {
            if (isSET(opcode)) {
                write8AtHl( read8AtHl() | valToAttribute(opcode, isSET(opcode)) );
            }
            else {
                write8AtHl( read8AtHl() & valToAttribute(opcode, isSET(opcode)) );
            }
        } break;
        case DAA: {
            int daa = Alu.bcdAdjust(regFile.get(Reg.A), extractFlag(Alu.Flag.N), extractFlag(Alu.Flag.H), extractFlag(Alu.Flag.C));
            setRegFromAlu(Reg.A , daa);
            combineAluFlags(daa, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU);
        } break;
        case SCCF: {
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, cFlagForSCCF(opcode));
        } break;
        // Jumps
        case JP_HL: {
            nextPC = reg16(Reg16.HL);
        } break;
        case JP_N16: {
            nextPC = read16AfterOpcode();
        } break;
        case JP_CC_N16: {
            if (testOpcodeCondition(opcode)) {
                nextPC = read16AfterOpcode();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        } break;
        case JR_E8: {
            byte signedNextVal = (byte) read8AfterOpcode();
            nextPC += signedNextVal;
        } break;
        case JR_CC_E8: {
            if (testOpcodeCondition(opcode)) {
                byte signedNextVal = (byte) read8AfterOpcode();
                nextPC += signedNextVal;
                nextNonIdleCycle += opcode.additionalCycles;
            }
        } break;
        // Calls and returns
        case CALL_N16: {
            push16(PC + opcode.totalBytes);
            nextPC = read16AfterOpcode();
        } break;
        case CALL_CC_N16: {
            if (testOpcodeCondition(opcode)) {
                push16(PC + opcode.totalBytes);
                nextPC = read16AfterOpcode();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        } break;
        case RST_U3: {
            push16(PC + opcode.totalBytes);
            nextPC =  AddressMap.RESETS[extractBitIndex(opcode)];
        } break;
        case RET: {
            nextPC = pop16();
        } break;
        case RET_CC: {
            if (testOpcodeCondition(opcode)) {
                nextPC = pop16();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        } break;
        // Interrupts
        case EDI: {
            IME = Bits.test(opcode.encoding, BIT_4);
        } break;
        case RETI: {
            IME = true;
            nextPC = pop16();
        } break;
        // Misc control
        case HALT: {
            nextNonIdleCycle = Long.MAX_VALUE;
        } break;
        case STOP:
            throw new Error("STOP is not implemented");
        default :
            throw new IllegalArgumentException();
        }
        PC = nextPC;
    }

    /**
     * Lit depuis le bus la valeur 8 bits à l'adresse donnée
     * @param address: adresse donnée
     * @return valeur 8 bits du bus à l'adresse donnée
     */
    private int read8(int address) {
        return bus.read(address);
    }

    /**
     * Lit depuis le bus la valeur 8 bits à l'adresse contenue dans la paire de registre HL
     * @return la valeur 8 bits à l'adresse contenue dans la paire de registre HL
     */
    private int read8AtHl() {
        return Bits.clip(LENGTH_8, read8(reg16(Reg16.HL)));
    }

    /**
     * Lit depuis le bus la valeur 8 bits à l'adresse suivant celle contenue dans le compteur de programme
     * c'est-à-dire celle contenue à l'adresse PC + 1
     * @return la valeur 8 bits à l'adresse suivant celle contenue dans le compteur de programme
     */
    private int read8AfterOpcode() {
        return Bits.clip(LENGTH_8, read8(PC + 1));
    }

    /**
     * Lit depuis le bus la valeur 16 bits à l'adresse donée
     * @param address: adresse donnée
     * @return valeur 16 bits du bus à l'adresse donnée
     * @throws IllegalArgumentException si la valeur ne peut pas être representée en 16 bits
     */
    private int read16(int address) {
        checkBits16(address + 1);
        int LSBs = read8(address);
        int MSBs = read8(Bits.clip(LENGTH_16, address+1));
        return Bits.make16(MSBs, LSBs);
    }

    /**
     * Lit depuis le bus la valeur 16 bits à l'adresse suivant celle contenue dans le compteur de programme
     * c'est-à-dire celle contenue à l'adresse PC + 1
     * @return la valeur 16 bits à l'adresse suivant celle contenue dans le compteur de programme
     */
    private int read16AfterOpcode() {
        return read16(PC + 1);
    }

    /**
     * Ecrit sur le bus, à l'adresse donnée, la valeur 8 bits donnée
     * @param address: adresse donnée
     * @param v: valeur 8 bits donnée
     */
    private void write8(int address, int v) {
        bus.write(address, Bits.clip(LENGTH_8, v));
    }
    /**
     * Ecrit sur le bus à l'adresse donnée la valeur 16 bits donnée
     * @param address: adresse donnée
     * @param v: valeur 8 bits donnée
     */
    private void write16(int address, int v) {
        bus.write(address, getLSBs(v));
        bus.write(Bits.clip(LENGTH_16, address + 1), getMSBs(v));
    }
    /**
     * Ecrit sur le bus à l'adresse contenue dans la paire de registres HL la valeur 8 bits donnée
     * @param v: valeur 8 bits donnée
     */
    private void write8AtHl(int v) {
        write8(reg16(Reg16.HL), v);
    }
    /**
     * décrémente l'adresse contenue dans le pointeur de pile de 2 unités
     * puis écrit à cette nouvelle adresse la valeur 16 bits donnée
     * @param v: valeur 16 bits donnée
     */
    private void push16(int v) {
        SP = Bits.clip(LENGTH_16, SP - 2);
        write16(SP,v);
    }
    /**
     * Lit depuis le bus la valeur 16 bits à l'adresse contenue dans le pointeur de la pile
     * puis l'incrémente de 2 unités
     * @return valeur 16 bits à l'adresse contenue dans le pointeur de la pile
     */
    private int pop16() {
        int readVal = read16(SP);
        SP = Bits.clip(LENGTH_16, SP + 2);
        return readVal;
    }
    /**
     * Retourne la valeur contenue dans la paire de registre donnée
     * @param r: paire de registre donnée
     * @return valeur contenue dans la paire de registre donnée
     */
    private int reg16(Reg16 r) {
        int MSR = regFile.get(r.firstReg);
        int LSR = regFile.get(r.secondReg);
        return Bits.make16(MSR, LSR);
    }

    /**
     * Modifie la valeur dans la paire de registre donnée
     * Met à 0 les LSB si la paire est AF
     * @param r: paire de registre donnée
     * @param newV: valeur à mettre dans la paire de registre donnée
     */
    private void setReg16(Reg16 r, int newV) {
        switch(r) {
        case AF: {
            regFile.set(r.firstReg, newV >> LENGTH_8);
            regFile.set(r.secondReg, Bits.extract(newV, BIT_5, LENGTH_4) << LENGTH_4);
        } break;
        default : {
            regFile.set(r.firstReg, Bits.extract(newV, BIT_9, LENGTH_8));
            regFile.set(r.secondReg, Bits.clip(LENGTH_8, newV));
        } break;
        }
    }

    /**
     * Modifie la valeur dans la paire de registre donnée
     * modifie le registre SP si la paire de registre donnée est AF
     * @param r: paire de registre donnée
     * @param newV: valeur à mettre dans la paire de registre donnée
     */
    private void setReg16SP(Reg16 r, int newV) {
        if (r == Reg16.AF) {
            SP = newV;
        }
        else {
            setReg16(r, newV);
        }
    }
    /**
     * Extrait et retourne l'idendité d'un registre 8 bits de l'encodage de l'opcode donné, à partir du bit d'index donné
     * @param opcode: opcode dont on veut extraire le registre 8 bits
     * @param startBit: index du bit à partir duquel on extrait le registre
     * @return identité du registre 8 bits
     */
    private Reg extractReg(Opcode opcode, int startBit) {
        int encoding = Bits.extract(opcode.encoding, startBit, LENGTH_3);
        switch(encoding) {
        case 0b000 : {
            return Reg.B;
        }
        case 0b001 : {
            return Reg.C;
        }
        case 0b010 : {
            return Reg.D;
        }
        case 0b011 : {
            return Reg.E;
        }
        case 0b100 : {
            return Reg.H;
        }
        case 0b101 : {
            return Reg.L;
        }
        case 0b110 : {
            return null;
        }
        case 0b111 : {
            return Reg.A;
        }
        default : return null;
        }
    }
    /**
     * Extrait et retourne l'idendité d'une paire de registre de l'encodage de l'opcode donné, à partir du bit d'index donné
     * @param opcode: opcode dont on veut extraire le registre 8 bits
     * @return idendité du registre 16 bits
     */
    private Reg16 extractReg16(Opcode opcode) {
        int encoding = Bits.extract(opcode.encoding, BIT_5, LENGTH_2);
        switch(encoding) {
        case 0b00 : {
            return Reg16.BC;
        }
        case 0b01 : {
            return Reg16.DE;
        }
        case 0b10 : {
            return Reg16.HL;
        }
        case 0b11 : {
            return Reg16.AF;
        }
        default : return null;
        }
    }

    /**
     * Encode l'incrémentation ou la décrementation de la paire HL selon les différentes situations
     * @param opcode: indique l'instruction donnée
     * @return +1 ou -1 selon l'opcode
     */
    private int extractHlIncrement(Opcode opcode) {
        int encoding = opcode.encoding;
        int HlIncrement = (Bits.test(encoding, BIT_5)) ? -1 : 1;
        return HlIncrement;
    }
    /**
     * Retourne les 8 LessSignificantBits d'une valeur 16bits
     * @param v : une valeur 16 bits
     * @return les 8 LSBs d'une valeur 16bits
     */
    private int getLSBs(int v) {
        return Bits.clip(LENGTH_8, v);
    }


    /**
     * Retourne les 8 MostSignificantBits d'une valeur 16bits
     * @param v : une valeur 16 bits
     * @return les 8 MSBs d'une valeur 16bits
     */
    private int getMSBs(int v) {
        return Bits.extract(v, BIT_9, LENGTH_8);
    }

    /**
     * Extrait la valeur stockée dans la paire donnée et la place dans le registre donnée
     * @param r : le registre donné
     * @param vf : la paire donnée
     */
    private void setRegFromAlu(Reg r, int vf) {
        regFile.set(r, Alu.unpackValue(vf));
    }

    /**
     * Extrait les fanions stockés dans la paire donnée et les place dans le registre F
     * @param valueFlags : la paire donnée
     */
    private void setFlags(int valueFlags) {
        regFile.set(Reg.F, Alu.unpackFlags(valueFlags));
    }

    /**
     * Combine les effets de setRegFromAlu et setFlags
     * @param r : le registre donné
     * @param vf : la paire donnée
     */
    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }

    /**
     *Extrait la valeur stockée dans la paire donnée puis l'écrit sur le bus à l'adresse contenue dans la paire de registre HL
     *Puis extrait les fanions stockés dans la paire et les place dans le registre F
     * @param vf : la paire donnée
     */
    private void write8AtHlAndSetFlags(int vf) {
        write8AtHl(Alu.unpackValue(vf));
        setFlags(vf);
    }


    /**
     * Donne l'index du bit à extraire
     * @param opcode : l'opcode donné en argument dans dispatch
     * @return l'index du bit à extraire
     */
    private int extractBitIndex(Opcode opcode){
        return Bits.extract(opcode.encoding, BIT_4, LENGTH_3);
    }

    /**
     * Cherche s'il doit y avoir ou non présence de carry selon le fanion C et l'instruction
     * On détermine l'instruction à l'aide du bit d'index 3 de l'opcode
     * @param opcode
     * @return la valeur du carry
     */
    private boolean carryInitVal(Opcode opcode) {
        boolean thirdOpcodeBit = Bits.test(opcode.encoding, BIT_4);
        return (thirdOpcodeBit & extractFlag(Alu.Flag.C));
    }

    /**
     * Effectue l'instruction LD_HLSP_S8
     * @param opcode : l'opcode passé en argument à dispatch
     */
    private void addForLD_HLSP_S8(Opcode opcode) {
        int r = Bits.signExtend8(read8AfterOpcode());
        int sum = Alu.add16L(SP, Bits.clip(LENGTH_16, r));
        setFlags(sum);
        if (Bits.test(opcode.encoding, BIT_5)) {
            setReg16(Reg16.HL, Alu.unpackValue(sum));
        } else {
            SP = Alu.unpackValue(sum);
        }
    }

    /**
     * Choisi SET ou RES pour les instructions CHG
     * @param opcode : l'opcode passé en argument à dispatch
     * @return true pour SET, false pour RES
     */
    private boolean isSET(Opcode opcode) {
        return Bits.test(opcode.encoding, BIT_7);
    }

    /**
     * Combine les fanions stockés dans le registre F avec ceux contenus dans la paire vf
     * @param vf : ValueFlags, un entier contenant une paire valeur/fanions retournée par l'une des méthodes de la classe Alu
     * @param z : le fanion Z
     * @param n : le fanion N
     * @param h : le fanion H
     * @param c : le fanion C
     */
    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        int vectV1 = describeSourceValues(FlagSrc.V1, z, n, h, c);
        int vectALU = describeSourceValues(FlagSrc.ALU, z, n, h, c);
        int vectCPU = describeSourceValues(FlagSrc.CPU, z, n, h, c);
        int combineVfandAlu = vectALU & vf;
        int combineFandCPU = vectCPU & regFile.get(Reg.F);
        int combineAll = combineVfandAlu | combineFandCPU | vectV1;
        setFlags(combineAll);
    }

    /**
     * Methode utilitaire pour combineAluFlags
     * @param toGenVectFor : la valeur de fanion que l'on essaye de "forcer"
     * @param z : le fanion Z
     * @param n : le fanion N
     * @param h : le fanion H
     * @param c : le fanion C
     * @return Un masque forcant à la valeur toGenVectFor les fanions z,n,h,c choisis
     */
    private int describeSourceValues(FlagSrc toGenVectFor, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        return Alu.maskZNHC(toGenVectFor == z, toGenVectFor == n, toGenVectFor == h, toGenVectFor == c);
    }

    /**
     * Donne la direction de rotation pour les rotations du dispatch
     * @param opcode : l'opcode passé en argument à dispatch
     * @return la direction de rotation pour les rotations du dispatch
     */
    private RotDir rotDir(Opcode opcode) {
        return Bits.test(opcode.encoding, BIT_4) ? RotDir.RIGHT : RotDir.LEFT;
    }

    /**
     * Donne la valeur à attribuer pour l'instruction CHG
     * @param opcode : l'opcode donné en argument à dispatch
     * @param isRES : vrai si l'instruction à effectuer est RES, faux si SET
     * @return la valeur à attribuer pour l'instruction CHG
     */
    private int valToAttribute(Opcode opcode, boolean isRES) {
        return (isRES) ? (1 << extractBitIndex(opcode)) : ~(1 << extractBitIndex(opcode));
    }

    /**
     * Si le opcodeReg est AF, tire la valeur de SP au lieu de AF pour les instructions R16_SP
     * @param opcodeReg : le Reg extrait du opcode donné en argument à dispatch
     * @return la valeur du Reg désigné, ou SP si AF est désigné
     */
    private int getReg16Sp(Reg16 opcodeReg) {
        if (opcodeReg == Reg16.AF) {
            return SP;
        } else {
            return reg16(opcodeReg);
        }
    }

    /**
     * Determine si la condition associee a une instruction conditionnelle est vraie ou fausse
     * @param opcode ; l'instruction
     * @return la condition de l'opcode associe
     */
    private boolean testOpcodeCondition(Opcode opcode) {
        int encoding = Bits.extract(opcode.encoding, BIT_4, LENGTH_2);
        switch(encoding) {
        case 0b00 : {
            return !extractFlag(Alu.Flag.Z);
        }
        case 0b01 : {
            return extractFlag(Alu.Flag.Z);
        }
        case 0b10 : {
            return !extractFlag(Alu.Flag.C);
        }
        case 0b11 : {
            return extractFlag(Alu.Flag.C);
        }
        default : return false;
        }
    }

    /**
     * retourne la valeur du fanion donné en argument tiree du registre F
     * @param flag, le fanion duquel on tire la valeur
     * @return vrai pour 1, faux pour 0
     */
    private boolean extractFlag(Alu.Flag flag) {
        return regFile.testBit(Reg.F, flag);
    }

    /**
     * Leve l'interruption donnee
     * @param i ; l'interruption
     */
    public void requestInterrupt(Interrupt i) { 
        int activatedIF = Bits.set(IF, i.index(), true);
        write(AddressMap.REG_IF, activatedIF);
    }

    /**
     * Donne le numéro de l'interruption à gérer
     * @return le numéro de l'interruption à gérer
     */
    private int getInterruptIndex() {
        int lowestBitValue = Integer.lowestOneBit(IF & IE);
        int numberOfTrailingZeros = Integer.numberOfTrailingZeros(lowestBitValue);
        return numberOfTrailingZeros;
    }

    /**
     * Donne l'opcodeReg à décrementer lors de l'instruction DEC_R16SP
     * @param opcode : l'opcode passé en argument à dispatch
     * @return l'opcodeReg à décrementer lors de l'instruction DEC_R16SP
     */
    private int toDecrementForDEC_R16SP(Opcode opcode) {
        return (Bits.extract(opcode.encoding, BIT_5, LENGTH_2) == 0b11) ? (SP-1) :  (reg16(extractReg16(opcode)) - 1);
    }

    /**
     * Donne la valeur à attribuer au fanion C lors de l'instruction SCCF
     * @param opcode : l'opcode passé en argument à dispatch
     * @return la valeur à attribuer au fanion C lors de l'instruction SCCF
     */
    private FlagSrc cFlagForSCCF(Opcode opcode) {
        return !carryInitVal(opcode) ? FlagSrc.V1 : FlagSrc.V0;
    }

    /**
     * Determine si une interruption doit etre gérée
     * @return vrai si une interruption doit etre gérée
     */
    private boolean toInterrupt() {
        return ((IME) && ((IE & IF) != 0));
    }

    /**
     * Determine si on doit lire ou écrire à l'addresse
     * @param address : l'addresse à laquelle on cherche à lire ou à écrire
     * @return vrai si elle est dans l'intervalle correspondant au highRam
     */
    private boolean toUseHighRamCondition(int address) {
        return (AddressMap.HIGH_RAM_START <= address) && (address < AddressMap.HIGH_RAM_END);
    }
}


