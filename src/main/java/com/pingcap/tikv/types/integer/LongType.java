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

package com.pingcap.tikv.types.integer;


import com.pingcap.tikv.codec.CodecDataInput;
import com.pingcap.tikv.meta.Row;
import com.pingcap.tikv.meta.TiColumnInfo;

public class LongType extends IntegerBaseType<Long> {
    public static final int TYPE_CODE = 3;

    public LongType(TiColumnInfo.InternalTypeHolder holder) {
        super(holder);
    }

    @Override
    protected void decodeValueNoNullToRow(Row row, int pos, Long value) {
        row.setLong(pos, value);
    }

    @Override
    public Long decodeNotNull(int flag, CodecDataInput cdi) {
        return decodeNotNullInternal(flag, cdi);
    }

    protected LongType(boolean unsigned) {
        super(unsigned);
    }

    @Override
    public int getTypeCode() {
        return TYPE_CODE;
    }

    public final static LongType DEF_SIGNED_TYPE = new LongType(false);
    public final static LongType DEF_UNSIGNED_TYPE = new LongType(true);
}