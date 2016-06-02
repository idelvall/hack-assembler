/*
 * Copyright 2016 Ignacio del Valle Alles idelvall@brutusin.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brutusin.hackasm;

import java.util.HashMap;
import java.util.Map;
import org.brutusin.commons.Pair;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class CInstruction extends Instruction {

    private static final Map<String, Pair<String, Boolean>> COMP_MAP;
    private static final Map<String, String> DEST_MAP;
    private static final Map<String, String> JUMP_MAP;

    static {
        COMP_MAP = new HashMap<String, Pair<String, Boolean>>();
        COMP_MAP.put("0", new Pair("101010", false));
        COMP_MAP.put("1", new Pair("111111", false));
        COMP_MAP.put("-1", new Pair("111010", false));
        COMP_MAP.put("D", new Pair("001100", false));
        COMP_MAP.put("A", new Pair("110000", false));
        COMP_MAP.put("!D", new Pair("001101", false));
        COMP_MAP.put("!A", new Pair("110001", false));
        COMP_MAP.put("-D", new Pair("001111", false));
        COMP_MAP.put("-A", new Pair("001111", false));
        COMP_MAP.put("D+1", new Pair("011111", false));
        COMP_MAP.put("A+1", new Pair("110111", false));
        COMP_MAP.put("D-1", new Pair("001110", false));
        COMP_MAP.put("A-1", new Pair("110010", false));
        COMP_MAP.put("D+A", new Pair("000010", false));
        COMP_MAP.put("D-A", new Pair("010011", false));
        COMP_MAP.put("A-D", new Pair("000111", false));
        COMP_MAP.put("D&A", new Pair("000000", false));
        COMP_MAP.put("D|A", new Pair("010101", false));
        COMP_MAP.put("M", new Pair("110000", true));
        COMP_MAP.put("!M", new Pair("110001", true));
        COMP_MAP.put("-M", new Pair("001111", true));
        COMP_MAP.put("M+1", new Pair("110111", true));
        COMP_MAP.put("M-1", new Pair("110010", true));
        COMP_MAP.put("D+M", new Pair("000010", true));
        COMP_MAP.put("D-M", new Pair("010011", true));
        COMP_MAP.put("M-D", new Pair("000111", true));
        COMP_MAP.put("D&M", new Pair("000000", true));
        COMP_MAP.put("D|M", new Pair("010101", true));

        DEST_MAP = new HashMap<String, String>();
        DEST_MAP.put(null, "000");
        DEST_MAP.put("M", "001");
        DEST_MAP.put("D", "010");
        DEST_MAP.put("MD", "011");
        DEST_MAP.put("A", "100");
        DEST_MAP.put("AM", "101");
        DEST_MAP.put("AD", "110");
        DEST_MAP.put("AMD", "111");

        JUMP_MAP = new HashMap<String, String>();
        JUMP_MAP.put(null, "000");
        JUMP_MAP.put("JGT", "001");
        JUMP_MAP.put("JEQ", "010");
        JUMP_MAP.put("JGE", "011");
        JUMP_MAP.put("JLT", "100");
        JUMP_MAP.put("JNE", "101");
        JUMP_MAP.put("JLE", "110");
        JUMP_MAP.put("JMP", "111");

    }

    private final String asm;
    private final String machineCode;

    public CInstruction(String dest, String comp, String jump) {
        if (comp == null) {
            throw new IllegalArgumentException("C-instruction comp field can not be null");
        }
        this.asm = getAsm(dest, comp, jump);
        this.machineCode = getMachineCode(dest, comp, jump);
    }

    private static String getAsm(String dest, String comp, String jump) {
        StringBuilder sb = new StringBuilder(comp);
        if (dest != null) {
            sb.insert(0, "=");
            sb.insert(0, dest);
        }
        if (jump != null) {
            sb.append(";");
            sb.append(jump);
        }
        return sb.toString();
    }

    private static String getMachineCode(String dest, String comp, String jump) {
        String binDest = DEST_MAP.get(dest);
        if (binDest == null) {
            throw new IllegalArgumentException("Invalid dest field value: '" + dest + "'");
        }
        Pair<String, Boolean> binComp = COMP_MAP.get(comp);
        if (binComp == null) {
            throw new IllegalArgumentException("Invalid comp field value: '" + comp + "'");
        }
        String binJump = JUMP_MAP.get(jump);
        if (binJump == null) {
            throw new IllegalArgumentException("Invalid jump field value: '" + jump + "'");
        }
        StringBuilder sb = new StringBuilder("111");
        if (binComp.getElement2()) {
            sb.append("1");
        } else {
            sb.append("0");
        }
        sb.append(binComp.getElement1());
        sb.append(binDest);
        sb.append(binJump);
        return sb.toString();
    }

    @Override
    public String getAsm() {
        return this.asm;
    }

    @Override
    public String getMachineCode() {
        return machineCode;
    }
}
