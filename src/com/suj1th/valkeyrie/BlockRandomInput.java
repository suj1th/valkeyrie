package com.suj1th.valkeyrie;

import java.io.IOException;

public class BlockRandomInput implements IBlockRandomInput {
	private final ReadOnlyMemoryMap readOnlyMemoryMap;
	
	
	BlockRandomInput(ReadOnlyMemoryMap readOnlyMemoryMap){
		this.readOnlyMemoryMap = readOnlyMemoryMap;
	}

	@Override
	public void seek(long pos) throws IOException {
		this.readOnlyMemoryMap.seek(pos);

	}

	@Override
	public int readUnSignedByte() throws IOException {
		return this.readOnlyMemoryMap.readUnsignedbyte();
		
	}

	@Override
	public void close() throws IOException {
		this.readOnlyMemoryMap.close();

	}

	@Override
	public void read(byte[] buffer, int offset, int length)
			throws IOException {
		this.readOnlyMemoryMap.read(buffer, offset, length);

	}

	@Override
	public void skipBytes(long amount) throws IOException {
		this.readOnlyMemoryMap.skipBytes(amount);

	}

	@Override
	public IBlockRandomInput duplicate() {
		return new BlockRandomInput(readOnlyMemoryMap.duplicate());
	}

}
