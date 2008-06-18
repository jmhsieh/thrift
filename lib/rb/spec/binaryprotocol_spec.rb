require File.dirname(__FILE__) + '/spec_helper'
require 'thrift/protocol/binaryprotocol'

class ThriftSpec < Spec::ExampleGroup
  include Thrift

  describe BinaryProtocol do
    before(:each) do
      @trans = mock("MockTransport", :null_object => true)
      @prot = BinaryProtocol.new(@trans)
    end

    it "should define the proper VERSION_1 and VERSION_MASK" do
      BinaryProtocol::VERSION_MASK.should == 0xffff0000
      BinaryProtocol::VERSION_1.should == 0x80010000
    end

    it "should write the message header" do
      @prot.should_receive(:write_i32).with(BinaryProtocol::VERSION_1 | MessageTypes::CALL).ordered
      @prot.should_receive(:write_string).with('testMessage').ordered
      @prot.should_receive(:write_i32).with(17).ordered
      @prot.write_message_begin('testMessage', MessageTypes::CALL, 17)
    end

    # message footer is a noop

    it "should write the field header" do
      @prot.should_receive(:write_byte).with(Types::DOUBLE).ordered
      @prot.should_receive(:write_i16).with(3).ordered
      @prot.write_field_begin('foo', Types::DOUBLE, 3)
    end

    # field footer is a noop

    it "should write the STOP field" do
      @prot.should_receive(:write_byte).with(Types::STOP)
      @prot.write_field_stop
    end

    it "should write the map header" do
      @prot.should_receive(:write_byte).with(Types::STRING).ordered
      @prot.should_receive(:write_byte).with(Types::LIST).ordered
      @prot.should_receive(:write_i32).with(17).ordered
      @prot.write_map_begin(Types::STRING, Types::LIST, 17)
    end

    # map footer is a noop

    it "should write the list header" do
      @prot.should_receive(:write_byte).with(Types::I16).ordered
      @prot.should_receive(:write_i32).with(42).ordered
      @prot.write_list_begin(Types::I16, 42)
    end

    # list footer is a noop

    it "should write the set header" do
      @prot.should_receive(:write_byte).with(Types::BOOL).ordered
      @prot.should_receive(:write_i32).with(2).ordered
      @prot.write_set_begin(Types::BOOL, 2)
    end

    it "should write a bool" do
      @prot.should_receive(:write_byte).with(1).ordered
      @prot.write_bool(true)
      @prot.should_receive(:write_byte).with(0).ordered
      @prot.write_bool(false)
    end

    it "should write a byte" do
      # byte is small enough, let's check -128..255
      (-128..255).each do |i|
        # do the verify/clear after each round because negative values
        # will double-up the same args as positive values
        @trans.should_receive(:write).with([i].pack('c'))
        @prot.write_byte(i)
        @trans.rspec_verify
        @trans.rspec_clear
      end
      # now try out of range
      lambda { @prot.write_byte(512) }.should raise_error(RangeError)
    end

    it "should write an i16" do
      # try a random scattering of values
      # include the signed i16 minimum and the unsigned i16 maximum
      @trans.should_receive(:write).with("\200\000").ordered
      @trans.should_receive(:write).with("\374\000").ordered
      @trans.should_receive(:write).with("\000\021").ordered
      @trans.should_receive(:write).with("\000\000").ordered
      @trans.should_receive(:write).with("\330\360").ordered
      @trans.should_receive(:write).with("\006\273").ordered
      @trans.should_receive(:write).with("\377\377").ordered
      [-2**15, -1024, 17, 0, -10000, 1723, 2**16-1].each do |i|
        @prot.write_i16(i)
      end
      # and try something out of range
      lambda { @prot.write_i16(2**18) }.should raise_error(RangeError)
    end

    it "should write an i32" do
      # try a random scattering of values
      # include the signed i32 minimum and the unsigned i32 maximum
      @trans.should_receive(:write).with("\200\000\000\000").ordered
      @trans.should_receive(:write).with("\377\376\037\r").ordered
      @trans.should_receive(:write).with("\377\377\366\034").ordered
      @trans.should_receive(:write).with("\377\377\377\375").ordered
      @trans.should_receive(:write).with("\000\000\000\000").ordered
      @trans.should_receive(:write).with("\000#\340\203").ordered
      @trans.should_receive(:write).with("\000\0000+").ordered
      @trans.should_receive(:write).with("\377\377\377\377").ordered
      [-2**31, -123123, -2532, -3, 0, 2351235, 12331, 2**32-1].each do |i|
        @prot.write_i32(i)
      end
      # try something out of range
      lambda { @prot.write_i32(2 ** 34) }.should raise_error(RangeError)
    end

    it "should write an i64" do
      # try a random scattering of values
      # include the signed i64 minimum and the unsigned i64 maximum
      @trans.should_receive(:write).with("\200\000\000\000\000\000\000\000").ordered
      @trans.should_receive(:write).with("\377\377\364\303\035\244+]").ordered
      @trans.should_receive(:write).with("\377\377\377\377\376\231:\341").ordered
      @trans.should_receive(:write).with("\377\377\377\377\377\377\377\026").ordered
      @trans.should_receive(:write).with("\000\000\000\000\000\000\000\000").ordered
      @trans.should_receive(:write).with("\000\000\000\000\000\000\004\317").ordered
      @trans.should_receive(:write).with("\000\000\000\000\000#\340\204").ordered
      @trans.should_receive(:write).with("\000\000\000\002\340\311~\365").ordered
      @trans.should_receive(:write).with("\377\377\377\377\377\377\377\377").ordered
      [-2**63, -12356123612323, -23512351, -234, 0, 1231, 2351236, 12361236213, 2**64-1].each do |i|
        @prot.write_i64(i)
      end
      # try something out of range
      lambda { @prot.write_i64(2 ** 72) }.should raise_error(RangeError)
    end

    it "should write a double" do
      # try a random scattering of values
      @trans.should_receive(:write).with("\000\020\000\000\000\000\000\000").ordered
      @trans.should_receive(:write).with("\300\223<\234\355\221hs").ordered
      @trans.should_receive(:write).with("\300\376\0173\256\024z\341").ordered
      @trans.should_receive(:write).with("\3007<2\336\372v\324").ordered
      @trans.should_receive(:write).with("\000\000\000\000\000\000\000\000").ordered
      @trans.should_receive(:write).with("@\310\037\220\365\302\217\\").ordered
      @trans.should_receive(:write).with("@\200Y\327\n=p\244").ordered
      @trans.should_receive(:write).with("\177\357\377\377\377\377\377\377").ordered
      [Float::MIN, -1231.15325, -123123.23, -23.23515123, 0, 12351.1325, 523.23, Float::MAX].each do |f|
        @prot.write_double(f)
      end
    end

    it "should write a string" do
      str = "hello world"
      @prot.should_receive(:write_i32).with(str.length).ordered
      @trans.should_receive(:write).with(str).ordered
      @prot.write_string(str)
    end
  end
end