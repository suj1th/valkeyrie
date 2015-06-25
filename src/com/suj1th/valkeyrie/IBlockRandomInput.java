package com.suj1th.valkeyrie;

import java.io.IOException;

/**
 * @author suj1th
 *
 */
public interface IBlockRandomInput {
	
	public void seek(long pos) throws IOException;
	
	public int readUnSignedByte() throws IOException;
	
	public void close() throws IOException;
	
	public void read(byte[] buffer, int offset, int length) throws IOException;

	public void skipBytes(long amount) throws IOException;

	public IBlockRandomInput duplicate();


}
