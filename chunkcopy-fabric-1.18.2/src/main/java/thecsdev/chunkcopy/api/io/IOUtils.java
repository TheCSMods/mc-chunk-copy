package thecsdev.chunkcopy.api.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import thecsdev.chunkcopy.api.ChunkCopyAPI;

/**
 * A utility class for {@link ChunkCopyAPI}'s IO based operations.
 */
public class IOUtils
{
	// ==================================================
	/**
	 * Reads an LEB-128 Variable Length Quantity integer from a stream.<br/>
	 * More info can be found here: https://wiki.vg/Protocol#VarInt_and_VarLong
	 * @param stream The {@link InputStream} to read an integer from.
	 * @throws IOException If an {@link IOException} occurs while reading an integer.
	 */
	public static int readVarInt(InputStream stream) throws IOException
	{
	    int value = 0;
	    int length = 0;
	    byte currentByte;

	    while (true)
	    {
	    	int nextByte = stream.read();
	    	if(nextByte < 0) break; //throw new IOException("End of stream."); --nah
	    	
	        currentByte = (byte)nextByte;
	        value |= (currentByte & 0x7F) << (length * 7);
	        
	        length += 1;
	        if (length > 5) { throw new IOException("VarInt is too big"); }

	        if ((currentByte & 0x80) != 0x80) { break; }
	    }
	    return value;
	}
	//---------------------------------------------------
	/**
	 * Writes an LEB-128 Variable Length Quantity integer to a stream.<br/>
	 * More info can be found here: https://wiki.vg/Protocol#VarInt_and_VarLong
	 * @throws IOException If an {@link IOException} occurs while writing an integer.
	 */
	public static void writeVarInt(OutputStream stream, int value) throws IOException
	{
		while (true)
		{
			if ((value & ~0x7F) == 0)
			{
				stream.write(value);
				return;
			}

			stream.write((value & 0x7F) | 0x80);
			// Note: >>> means that the sign bit is shifted with the rest of the number
			// rather than being left alone
			value >>>= 7;
		}
	}
	// ==================================================
	/**
	 * Reads a {@link String} from an {@link InputStream}.
	 * @param stream The {@link InputStream} to read a {@link String} from.
	 */
	public static String readString(InputStream stream) throws IOException
	{
		int length = readVarInt(stream);
		return new String(stream.readNBytes(length), "UTF-8");
	}
	//---------------------------------------------------
	/**
	 * Writes a {@link String} to an {@link OutputStream}.
	 * @param stream The {@link OutputStream} to write a {@link String} to.
	 * @param value The {@link String} to write.
	 */
	public static void writeString(OutputStream stream, String value) throws IOException
	{
		byte[] bytes = value.getBytes("UTF-8");
		writeVarInt(stream, bytes.length);
		stream.write(bytes);
	}
	// ==================================================
	public static byte[] readByteArray(InputStream stream) throws IOException
	{
		int len = readVarInt(stream);
		return stream.readNBytes(len);
	}
	// --------------------------------------------------
	public static void writeByteArray(OutputStream stream, byte[] bytes) throws IOException
	{
		writeVarInt(stream, bytes.length);
		stream.write(bytes);
	}
	// ==================================================
	/**
	 * Compresses a byte array using the GZip compression algorithm.
	 * @param bytes The bytes (data) to compress.
	 * @throws IOException If an {@link IOException} occurs while compressing the bytes.
	 */
	public static byte[] gzipCompressBytes(byte[] bytes) throws IOException
	{
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(bytesOut);
		gzip.write(bytes);
		gzip.close();
		bytes = bytesOut.toByteArray();
		bytesOut.close();
		return bytes;
	}
	// --------------------------------------------------
	/**
	 * Decompresses a byte array using the GZip compression algorithm.
	 * @param bytes The bytes (data) to decompress.
	 * @throws IOException If an {@link IOException} occurs while decompressing the bytes.
	 */
	public static byte[] gzipDecompressBytes(byte[] bytes) throws IOException
	{
		ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
		GZIPInputStream gzipIn = new GZIPInputStream(bytesIn);
		bytes = gzipIn.readAllBytes();
		gzipIn.close();
		bytesIn.close();
		return bytes;
	}
	// ==================================================
	/**
	 * Returns true if a class has a constructor with no parameters.
	 */
	public static boolean classHasParameterlessConstructor(Class<?> clazz)
	{
	    return Stream.of(clazz.getConstructors()).anyMatch((c) -> c.getParameterCount() == 0);
	}
	// ==================================================
}
