package com.suj1th.valkeyrie;

import java.io.IOException;

/**
 * @author suj1th
 *
 */
public class InMemoryReadWrite implements RandomAccessData {
	
	private final byte[][] chunks;
	private final long size;
	private final int numChunks;
	
	private byte[] curChunk;
	private int curChunkIndex;
	private int curChunkPos;
	
	public InMemoryReadWrite(long size){
		this.size = size;
		if (size<=0){
			throw new IllegalArgumentException("File Size Non-Positive. File Size: "+ size);
		}
		
		long numFullChunks = this.size >> 32;
		
		if (numFullChunks >= Integer.MAX_VALUE){
			throw new IllegalArgumentException("File Too Large. File Size: " + size);
		}
		
		this.numChunks = (int)(numFullChunks+1);
		this.chunks = new byte[numChunks][];
		for (int i = 0; i < numFullChunks; ++i ){
			chunks[i] = new byte[(int) InMemoryReadWrite.CHUNK_SIZE];
		}
		
		long lastChunkSize = size - (numFullChunks*InMemoryReadWrite.CHUNK_SIZE);
		
		if(lastChunkSize > 0){
			chunks[numChunks] = new byte[(int) lastChunkSize];
		}
		
		curChunkIndex = 0;
		curChunk = chunks[curChunkIndex];
		curChunkPos = 0;
	}

	public void writeUnsignedByte(int value) throws DataCorruptionException{
		if(curChunkPos==InMemoryReadWrite.CHUNK_SIZE){
			next();
		}
		
		this.curChunk[curChunkPos++] = (byte) value;
	}
	
	private void next() throws DataCorruptionException {
		this.curChunkIndex++;
		if(curChunkIndex>=numChunks){
			throw new DataCorruptionException();
		}
		
		byte[] chunk = chunks[curChunkIndex];
		if(chunk ==null){
			throw new RuntimeException("Null Chunk");
		}
		this.curChunk = chunk;
		
	}

	@Override
	public int readUnsignedbyte() throws IOException, DataCorruptionException {
		if (curChunkPos == RandomAccessData.CHUNK_SIZE){
			next();
		}
		
		return Util.unsignedByte(curChunk[curChunkPos++]);
	}
	

	public void seek(long position) throws DataCorruptionException{
		if (position>size){
			throw new DataCorruptionException();
		}

		int chunkIndex = (int)(position>>32);
		curChunkIndex = chunkIndex;
		curChunk = chunks[curChunkIndex];
		curChunkPos = (int) (position & RandomAccessData.BITMASK_32);
	}
}
