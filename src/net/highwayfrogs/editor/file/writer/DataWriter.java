package net.highwayfrogs.editor.file.writer;

import lombok.Getter;
import lombok.Setter;
import net.highwayfrogs.editor.Constants;
import net.highwayfrogs.editor.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Allows writing data to a receiver.
 * Created by Kneesnap on 8/10/2018.
 */
@Getter
public class DataWriter {
    @Setter private ByteOrder endian = ByteOrder.LITTLE_ENDIAN;
    private DataReceiver output;

    private static final ByteBuffer INT_BUFFER = ByteBuffer.allocate(Constants.INTEGER_SIZE);

    public DataWriter(DataReceiver output) {
        this.output = output;
    }

    /**
     * Write a given amount of null bytes.
     * @param amount The amount of null bytes to write.
     */
    public void writeNull(int amount) {
        writeBytes(new byte[amount]);
    }

    /**
     * Jump to a given write offset, leaving null-bytes in between.
     * @param address The address to jump to.
     */
    public void jumpTo(int address) {
        int index = getIndex();
        Utils.verify(address >= index, "Tried to jump to %s, which is before the current writer address (%s).", Integer.toHexString(address), Integer.toHexString(index));
        writeNull(address - index);
    }

    /**
     * Gets the writer index.
     * @return index
     */
    public int getIndex() {
        try {
            return output.getIndex();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to get writer index.", ex);
        }
    }

    /**
     * Write a single byte to the receiver.
     * @param value The value to write.
     */
    public void writeByte(byte value) {
        try {
            output.writeByte(value);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write a byte to the receiver.", ex);
        }
    }

    /**
     * Write a byte array to the receiver.
     * @param bytes The array of values to write.
     */
    public void writeBytes(byte... bytes) {
        try {
            output.writeBytes(bytes);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write a byte-array to the receiver.", ex);
        }
    }

    /**
     * Write an integer to the receiver.
     * @param value The integer to write.
     */
    public void writeInt(int value) {
        INT_BUFFER.clear();
        writeBytes(INT_BUFFER.order(getEndian()).putInt(value).array());
    }

    /**
     * Writes a string to the receiver, terminated with a null byte.
     * @param str The string to write.
     */
    public void writeTerminatorString(String str) {
        writeTerminatorString(str, Constants.NULL_BYTE);
    }

    /**
     * Writes a string to the receiver, using a byte as the terminator.
     * @param str        The string to write.
     * @param terminator The terminator. Usually a null byte.
     */
    public void writeTerminatorString(String str, byte terminator) {
        writeBytes(str.getBytes());
        writeByte(terminator);
    }
}
