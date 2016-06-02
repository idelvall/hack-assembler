package org.brutusin.hackasm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.brutusin.commons.Bean;
import org.brutusin.commons.io.LineReader;

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
/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class Parser {

    private final File f;
    private final Map<String, Short> symTable = new HashMap<>();
    private final Map<String, List<Integer>> unresolvedVarPositions = new LinkedHashMap<>();
    private final ArrayList<Instruction> instructions = new ArrayList<>();
    private int varAddress = 16;

    public Parser(File f) {
        if (!f.exists()) {
            throw new IllegalArgumentException("File doesn't exist: '" + f.getAbsolutePath() + "'");
        }
        this.f = f;
        preloadSymTable();
    }

    private void preloadSymTable() {
        for (short r = 0; r < 16; r++) {
            symTable.put("R" + r, r);
        }
        symTable.put("SCREEN", (short) 16384);
        symTable.put("KBD", (short) 24576);
        symTable.put("SP", (short) 0);
        symTable.put("LCL", (short) 1);
        symTable.put("ARG", (short) 2);
        symTable.put("THIS", (short) 3);
        symTable.put("THAT", (short) 4);
    }

    private AInstruction parseAInstruction(String line) {
        if (line.length() < 2) {
            throw new IllegalArgumentException("Missing address field in A-instruction");
        }
        String address = line.substring(1);
        AInstruction aInst = new AInstruction();
        try {
            aInst.setAddress(Short.parseShort(address));
        } catch (NumberFormatException nfe) {
            Short s = symTable.get(address);
            if (s != null) {
                aInst.setAddress(s);
            } else {
                List<Integer> list = unresolvedVarPositions.get(address);
                if (list == null) {
                    list = new ArrayList<>();
                    unresolvedVarPositions.put(address, list);
                }
                list.add(instructions.size());
            }
        }
        return aInst;
    }

    private CInstruction parseCInstruction(String line) {
        String[] split = line.split("=");
        String dest;
        String comp;
        String jump;
        String remaining;
        if (split.length == 1) {
            dest = null;
            remaining = split[0];
        } else if (split.length == 2) {
            dest = split[0];
            remaining = split[1];
        } else {
            throw new IllegalArgumentException("Invalid C-instruction. Only a '=' is allowed");
        }
        split = remaining.split(";");
        if (split.length == 1) {
            comp = split[0];
            jump = null;
        } else if (split.length == 2) {
            comp = split[0];
            jump = split[1];
        } else {
            throw new IllegalArgumentException("Invalid C-instruction. Only a ';' is allowed");
        }
        return new CInstruction(dest, comp, jump);
    }

    private Instruction parse(String line) {
        line = line.split("//")[0].replaceAll("\\s", "");
        if (line.isEmpty()) { //comments
            return null;
        }
        if (line.startsWith("(") && line.endsWith(")")) { //label declaration
            if (line.length() < 3) {
                throw new IllegalArgumentException("Missing label value");
            }
            symTable.put(line.substring(1, line.length() - 1), (short) instructions.size());
            return null;
        }
        if (line.startsWith("@")) {
            return parseAInstruction(line);
        } else {
            return parseCInstruction(line);
        }
    }

    private void resolveSyms() {
        for (Map.Entry<String, List<Integer>> entrySet : unresolvedVarPositions.entrySet()) {
            String sym = entrySet.getKey();
            List<Integer> instructionNumbers = entrySet.getValue();
            Short address = symTable.get(sym);
            if (address == null) {
                address = (short) varAddress++;
            }
            for (Integer instructionNumber : instructionNumbers) {
                AInstruction inst = (AInstruction) instructions.get(instructionNumber);
                inst.setAddress(address);
            }
        }
    }

    private void generateOutput() {

        String name;
        if (f.getName().endsWith(".asm")) {
            name = f.getName().substring(0, f.getName().length() - 4);
        } else {
            name = f.getName();
        }
        name += ".hack";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(f.getParentFile(), name));
            for (int i = 0; i < instructions.size(); i++) {
                Instruction inst = instructions.get(i);
                fos.write(inst.getMachineCode().getBytes());
                fos.write("\n".getBytes());
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void parse() {
        try {
            final Bean<Boolean> errorsFound = new Bean();
            LineReader lr = new LineReader(new FileInputStream(f)) {
                @Override
                protected void processLine(String line) throws Exception {
                    Instruction inst = parse(line);
                    if (inst != null) {
                        instructions.add(inst);
                    }
                }

                @Override
                protected void onExceptionFound(Exception ex) {
                    errorsFound.setValue(true);
                    System.err.println("Error found on line " + getLine() + ": " + ex.getMessage());
                }
            };
            lr.run();
            resolveSyms();
            generateOutput();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args == null || args.length == 0) {
            System.err.println("An .asm file is required");
            System.exit(1);
        }
        if (args.length > 1) {
            System.err.println("Only one parameter (asm file) is supported");
            System.exit(1);
        }
        try {
            Parser p = new Parser(new File(args[0]));
            p.parse();
        } catch (Exception e) {
            System.err.println("An error has occurred: " + e.getMessage());
            System.exit(1);
        }
    }
}
