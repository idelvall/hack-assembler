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

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class AInstruction extends Instruction {

    private short address;

    public void setAddress(short address) {
        if (address < 0) {
            throw new IllegalArgumentException("Invalid address value: " + address);
        }
        this.address = address;
    }

    public short getAddress() {
        return address;
    }

    @Override
    public String getMachineCode() {
        StringBuilder sb = new StringBuilder(Integer.toBinaryString(address));
        while (sb.length() < 16) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    @Override
    public String getAsm() {
        return "@" + address;
    }
}
