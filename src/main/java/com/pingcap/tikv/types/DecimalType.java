/*
 *
 * Copyright 2017 PingCAP, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.pingcap.tikv.types;

import com.pingcap.tikv.codec.CodecDataInput;
import com.pingcap.tikv.codec.CodecDataOutput;
import com.pingcap.tikv.codec.InvalidCodecFormatException;
import com.pingcap.tikv.codec.MyDecimal;
import com.pingcap.tikv.row.Row;
import com.pingcap.tikv.meta.TiColumnInfo;
import gnu.trove.list.array.TIntArrayList;

public class DecimalType extends DataType {

    static DecimalType of(int tp) {
       return new DecimalType(tp);
    }

    private DecimalType(int tp) {
        super(tp);
    }

    /**
     *  decode a decimal value to Row from Cdi.
     * @param cdi source of data.
     * @param row destination of data
     * @param pos position of row.
     */
    @Override
    public void decode(CodecDataInput cdi, Row row, int pos) {
        int flag = cdi.readUnsignedByte();
        if (flag != DECIMAL_FLAG) {
            throw new InvalidCodecFormatException("Invalid Flag type for decimal type: " + flag);
        }
        double val = readDecimalFully(cdi);
        row.setDouble(pos, val);
    }

    /**
     * Encode a Decimal to Byte String.
     * @param cdo destination of data.
     * @param encodeType Key or Value.
     * @param value need to be encoded.
     */
    @Override
    public void encode(CodecDataOutput cdo, EncodeType encodeType, Object value) {
        double val;
        if (value instanceof Number) {
             val = ((Number)value).doubleValue();
        } else {
            throw new UnsupportedOperationException("can not cast non Number type to Double");
        }
        writeDecimalFully(cdo, val);
    }

    /** read a decimal value from CodecDataInput
     * @param cdi cdi is source data.
     * */
    public static double readDecimalFully(CodecDataInput cdi) {
        if (cdi.available() < 3) {
            throw new IllegalArgumentException("insufficient bytes to read value");
        }

        MyDecimal dec = new MyDecimal();
        // 64 should be larger enough for avoiding unnecessary growth.
        TIntArrayList data = new TIntArrayList(64);
        int precision = cdi.readUnsignedByte();
        int frac = cdi.readUnsignedByte();
        int length = precision + frac;
        int curPos = cdi.size() - cdi.available();
        for(int i = 0; i < length; i++) {
            if (cdi.eof()){
                break;
            }
            data.add(cdi.readUnsignedByte());
        }

        int binSize = dec.fromBin(precision, frac, data.toArray());
        cdi.mark(curPos+binSize);
        cdi.reset();
        return dec.toDecimal();
    }

    /** write a decimal value from CodecDataInput
     * @param cdo cdo is destination data.
     * @param lvalue is decimal value that will be written into cdo.
     * */
    public static void writeDecimalFully(CodecDataOutput cdo, double lvalue) {
        MyDecimal dec = new MyDecimal();
        dec.fromDecimal(lvalue);
        int[] data = dec.toBin(dec.precision(), dec.frac());
        cdo.writeByte(dec.precision());
        cdo.writeByte(dec.frac());
        for (int aData : data) {
            cdo.writeByte(aData & 0xFF);
        }
    }

    /**
     * Decode as float
     * @param cdi source of data
     * @return decoded unsigned long value
     */
    public static double readDouble(CodecDataInput cdi) {
        long u = IntegerType.readULong(cdi);
        if (u < 0) {
            u &= Long.MAX_VALUE;
        } else {
            u = ~u;
        }
        return Double.longBitsToDouble(u);
    }


    /**
     * Encoding a double value to byte buffer
     * @param cdo For outputting data in bytes array
     * @param val The data to encode
     */
    public static void writeDouble(CodecDataOutput cdo, double val) {
        throw new UnsupportedOperationException();
    }

    /**
     * Encoding a float value to byte buffer
     * @param cdo For outputting data in bytes array
     * @param val The data to encode
     */
    public static void writeFloat(CodecDataOutput cdo, float val) {
        throw new UnsupportedOperationException();
    }

}
