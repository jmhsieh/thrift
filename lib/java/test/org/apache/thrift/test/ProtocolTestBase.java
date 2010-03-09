/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.thrift.test;

import java.util.Arrays;
import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.transport.TMemoryBuffer;

import thrift.test.CompactProtoTestStruct;
import thrift.test.HolyMoley;
import thrift.test.Nesting;
import thrift.test.OneOfEach;
import thrift.test.Srv;

public abstract class ProtocolTestBase {
  
  protected abstract TProtocolFactory getFactory();

  public void main() throws Exception {
    testNakedByte();
    for (int i = 0; i < 128; i++) {
      testByteField((byte)i);
      testByteField((byte)-i);
    }
    
    for (int s : Arrays.asList(0, 1, 7, 150, 15000, 0x7fff, -1, -7, -150, -15000, -0x7fff)) {
      testNakedI16((short)s);
      testI16Field((short)s);
    }

    for (int i : Arrays.asList(0, 1, 7, 150, 15000, 31337, 0xffff, 0xffffff, -1, -7, -150, -15000, -0xffff, -0xffffff)) {
      testNakedI32(i);
      testI32Field(i);
    }

    testNakedI64(0);
    testI64Field(0);
    for (int i = 0; i < 62; i++) {
      testNakedI64(1L << i);
      testNakedI64(-(1L << i));
      testI64Field(1L << i);
      testI64Field(-(1L << i));
    }

    testDouble();

    for (String s : Arrays.asList("", "short", "borderlinetiny", "a bit longer than the smallest possible")) {
      testNakedString(s);
      testStringField(s);
    }

    for (byte[] b : Arrays.asList(new byte[0], new byte[]{0,1,2,3,4,5,6,7,8,9,10}, new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14}, new byte[128])) {
      testNakedBinary(b);
      testBinaryField(b);
    }

    testSerialization(OneOfEach.class, Fixtures.oneOfEach);
    testSerialization(Nesting.class, Fixtures.nesting);
    testSerialization(HolyMoley.class, Fixtures.holyMoley);
    testSerialization(CompactProtoTestStruct.class, Fixtures.compactProtoTestStruct);

    testMessage();

    testServerRequest();

    testTDeserializer();
  }

  public void testNakedByte() throws Exception {
    TMemoryBuffer buf = new TMemoryBuffer(0);
    TProtocol proto = getFactory().getProtocol(buf);
    proto.writeByte((byte)123);
    byte out = proto.readByte();
    if (out != 123) {
      throw new RuntimeException("Byte was supposed to be " + (byte)123 + " but was " + out);
    }
  }

  public void testByteField(final byte b) throws Exception {
    testStructField(new StructFieldTestCase(TType.BYTE, (short)15) {
      public void writeMethod(TProtocol proto) throws TException {
        proto.writeByte(b);
      }
      
      public void readMethod(TProtocol proto) throws TException {
        byte result = proto.readByte();
        if (result != b) {
          throw new RuntimeException("Byte was supposed to be " + (byte)b + " but was " + result);
        }
      }
    });
  }

  public void testNakedI16(short n) throws Exception {
    TMemoryBuffer buf = new TMemoryBuffer(0);
    TProtocol proto = getFactory().getProtocol(buf);
    proto.writeI16(n);
    // System.out.println(buf.inspect());
    int out = proto.readI16();
    if (out != n) {
      throw new RuntimeException("I16 was supposed to be " + n + " but was " + out);
    }
  }

  public void testI16Field(final short n) throws Exception {
    testStructField(new StructFieldTestCase(TType.I16, (short)15) {
      public void writeMethod(TProtocol proto) throws TException {
        proto.writeI16(n);
      }

      public void readMethod(TProtocol proto) throws TException {
        short result = proto.readI16();
        if (result != n) {
          throw new RuntimeException("I16 was supposed to be " + n + " but was " + result);
        }
      }
    });
  }

  public void testNakedI32(int n) throws Exception {
    TMemoryBuffer buf = new TMemoryBuffer(0);
    TProtocol proto = getFactory().getProtocol(buf);
    proto.writeI32(n);
    // System.out.println(buf.inspect());
    int out = proto.readI32();
    if (out != n) {
      throw new RuntimeException("I32 was supposed to be " + n + " but was " + out);
    }
  }

  public void testI32Field(final int n) throws Exception {
    testStructField(new StructFieldTestCase(TType.I32, (short)15) {
      public void writeMethod(TProtocol proto) throws TException {
        proto.writeI32(n);
      }

      public void readMethod(TProtocol proto) throws TException {
        int result = proto.readI32();
        if (result != n) {
          throw new RuntimeException("I32 was supposed to be " + n + " but was " + result);
        }
      }
    });
  }

  public void testNakedI64(long n) throws Exception {
    TMemoryBuffer buf = new TMemoryBuffer(0);
    TProtocol proto = getFactory().getProtocol(buf);
    proto.writeI64(n);
    // System.out.println(buf.inspect());
    long out = proto.readI64();
    if (out != n) {
      throw new RuntimeException("I64 was supposed to be " + n + " but was " + out);
    }
  }

  public void testI64Field(final long n) throws Exception {
    testStructField(new StructFieldTestCase(TType.I64, (short)15) {
      public void writeMethod(TProtocol proto) throws TException {
        proto.writeI64(n);
      }

      public void readMethod(TProtocol proto) throws TException {
        long result = proto.readI64();
        if (result != n) {
          throw new RuntimeException("I64 was supposed to be " + n + " but was " + result);
        }
      }
    });
  }

  public void testDouble() throws Exception {
    TMemoryBuffer buf = new TMemoryBuffer(1000);
    TProtocol proto = getFactory().getProtocol(buf);
    proto.writeDouble(123.456);
    double out = proto.readDouble();
    if (out != 123.456) {
      throw new RuntimeException("Double was supposed to be " + 123.456 + " but was " + out);
    }
  }

  public void testNakedString(String str) throws Exception {
    TMemoryBuffer buf = new TMemoryBuffer(0);
    TProtocol proto = getFactory().getProtocol(buf);
    proto.writeString(str);
    // System.out.println(buf.inspect());
    String out = proto.readString();
    if (!str.equals(out)) {
      throw new RuntimeException("String was supposed to be '" + str + "' but was '" + out + "'");
    }
  }
  
  public void testStringField(final String str) throws Exception {
    testStructField(new StructFieldTestCase(TType.STRING, (short)15) {
      public void writeMethod(TProtocol proto) throws TException {
        proto.writeString(str);
      }
      
      public void readMethod(TProtocol proto) throws TException {
        String result = proto.readString();
        if (!result.equals(str)) {
          throw new RuntimeException("String was supposed to be " + str + " but was " + result);
        }
      }
    });
  }

  public void testNakedBinary(byte[] data) throws Exception {
    TMemoryBuffer buf = new TMemoryBuffer(0);
    TProtocol proto = getFactory().getProtocol(buf);
    proto.writeBinary(data);
    // System.out.println(buf.inspect());
    byte[] out = proto.readBinary();
    if (!Arrays.equals(data, out)) {
      throw new RuntimeException("Binary was supposed to be '" + data + "' but was '" + out + "'");
    }
  }

  public void testBinaryField(final byte[] data) throws Exception {
    testStructField(new StructFieldTestCase(TType.STRING, (short)15) {
      public void writeMethod(TProtocol proto) throws TException {
        proto.writeBinary(data);
      }
      
      public void readMethod(TProtocol proto) throws TException {
        byte[] result = proto.readBinary();
        if (!Arrays.equals(data, result)) {
          throw new RuntimeException("Binary was supposed to be '" + bytesToString(data) + "' but was '" + bytesToString(result) + "'");
        }
      }
    });
    
  }

  public <T extends TBase> void testSerialization(Class<T> klass, T obj) throws Exception {
    TMemoryBuffer buf = new TMemoryBuffer(0);
    TBinaryProtocol binproto = new TBinaryProtocol(buf);

    try {
      obj.write(binproto);
      // System.out.println("Size in binary protocol: " + buf.length());

      buf = new TMemoryBuffer(0);
      TProtocol proto = getFactory().getProtocol(buf);

      obj.write(proto);
      System.out.println("Size in " +  proto.getClass().getSimpleName() + ": " + buf.length());
      // System.out.println(buf.inspect());

      T objRead = klass.newInstance();
      objRead.read(proto);
      if (!obj.equals(objRead)) {
        System.out.println("Expected: " + obj.toString());
        System.out.println("Actual: " + objRead.toString());
        // System.out.println(buf.inspect());
        throw new RuntimeException("Objects didn't match!");
      }
    } catch (Exception e) {
      System.out.println(buf.inspect());
      throw e;
    }
  }

  public void testMessage() throws Exception {
    List<TMessage> msgs = Arrays.asList(new TMessage[]{
      new TMessage("short message name", TMessageType.CALL, 0),
      new TMessage("1", TMessageType.REPLY, 12345),
      new TMessage("loooooooooooooooooooooooooooooooooong", TMessageType.EXCEPTION, 1 << 16),
      new TMessage("Janky", TMessageType.CALL, 0),
    });

    for (TMessage msg : msgs) {
      TMemoryBuffer buf = new TMemoryBuffer(0);
      TProtocol proto = getFactory().getProtocol(buf);
      TMessage output = null;

      proto.writeMessageBegin(msg);
      proto.writeMessageEnd();

      output = proto.readMessageBegin();

      if (!msg.equals(output)) {
        throw new RuntimeException("Message was supposed to be " + msg + " but was " + output);
      }
    }
  }

  public void testServerRequest() throws Exception {
    Srv.Iface handler = new Srv.Iface() {
      public int Janky(int i32arg) throws TException {
        return i32arg * 2;
      }

      public int primitiveMethod() throws TException {
        return 0;
      }

      public CompactProtoTestStruct structMethod() throws TException {
        return null;
      }

      public void voidMethod() throws TException {
      }

      public void methodWithDefaultArgs(int something) throws TException {
      }
    };

    Srv.Processor testProcessor = new Srv.Processor(handler);

    TMemoryBuffer clientOutTrans = new TMemoryBuffer(0);
    TProtocol clientOutProto = getFactory().getProtocol(clientOutTrans);
    TMemoryBuffer clientInTrans = new TMemoryBuffer(0);
    TProtocol clientInProto = getFactory().getProtocol(clientInTrans);

    Srv.Client testClient = new Srv.Client(clientInProto, clientOutProto);

    testClient.send_Janky(1);
    // System.out.println(clientOutTrans.inspect());
    testProcessor.process(clientOutProto, clientInProto);
    // System.out.println(clientInTrans.inspect());
    int result = testClient.recv_Janky();
    if (result != 2) {
      throw new RuntimeException("Got an unexpected result: " + result);
    }
  }

  private void testTDeserializer() throws TException {
    TSerializer ser = new TSerializer(getFactory());
    byte[] bytes = ser.serialize(Fixtures.compactProtoTestStruct);

    TDeserializer deser = new TDeserializer(getFactory());
    CompactProtoTestStruct cpts = new CompactProtoTestStruct();
    deser.deserialize(cpts, bytes);

    if (!Fixtures.compactProtoTestStruct.equals(cpts)) {
      throw new RuntimeException(Fixtures.compactProtoTestStruct + " and " + cpts + " do not match!");
    }
  }

  //
  // Helper methods
  //

  private static String bytesToString(byte[] bytes) {
    String s = "";
    for (int i = 0; i < bytes.length; i++) {
      s += Integer.toHexString((int)bytes[i]) + " ";
    }
    return s;
  }

  private void testStructField(StructFieldTestCase testCase) throws Exception {
    TMemoryBuffer buf = new TMemoryBuffer(0);
    TProtocol proto = getFactory().getProtocol(buf);

    TField field = new TField("test_field", testCase.type_, testCase.id_);
    proto.writeStructBegin(new TStruct("test_struct"));
    proto.writeFieldBegin(field);
    testCase.writeMethod(proto);
    proto.writeFieldEnd();
    proto.writeStructEnd();

    // System.out.println(buf.inspect());

    proto.readStructBegin();
    TField readField = proto.readFieldBegin();
    // TODO: verify the field is as expected
    if (!field.equals(readField)) {
      throw new RuntimeException("Expected " + field + " but got " + readField);
    }
    testCase.readMethod(proto);
    proto.readStructEnd();
  }

  public static abstract class StructFieldTestCase {
    byte type_;
    short id_;
    public StructFieldTestCase(byte type, short id) {
      type_ = type;
      id_ = id;
    }

    public abstract void writeMethod(TProtocol proto) throws TException;
    public abstract void readMethod(TProtocol proto) throws TException;
  }
}