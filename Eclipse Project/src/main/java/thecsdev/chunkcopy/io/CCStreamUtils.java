package thecsdev.chunkcopy.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Provides Stream and IO related utility methods for the {@link ChunkCopyClient} mod.
 */
public final class CCStreamUtils
{
	// ==================================================
	/**
	 * Reads an LEB-128 Variable Length Quantity integer from a stream.<br/>
	 * More info can be found here: https://wiki.vg/Protocol#VarInt_and_VarLong
	 * @throws IOException 
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
	        if (length > 5) { throw new RuntimeException("VarInt is too big"); }

	        if ((currentByte & 0x80) != 0x80) { break; }
	    }
	    return value;
	}
	//---------------------------------------------------
	/**
	 * Writes an LEB-128 Variable Length Quantity integer to a stream.<br/>
	 * More info can be found here: https://wiki.vg/Protocol#VarInt_and_VarLong
	 * @throws IOException 
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
	 * Compresses a byte array using the GZip compression algorithm.
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
}














