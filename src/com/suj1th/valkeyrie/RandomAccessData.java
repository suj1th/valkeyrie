package com.suj1th.valkeyrie;

/**
 * @author suj1th
 *
 */
public interface RandomAccessData {
	
	public static final long CHUNK_SIZE = 1L<<32;
	public static final long BITMASK_32 = ((1L<<32)-1);
	
	public int readUnsignedbyte() throws Exception;

}
