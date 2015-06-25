package com.suj1th.valkeyrie;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
/**
 * A collection of MappedByteBuffers which store memory-mapped 
 * chunks of the valkeyrie log-file. Each chunk maps a 4 GB region 
 * of the log-file in memory. This class also provides methods
 * to read data from the MappedByteBuffers.
 * 
 * 
 * @author suj1th 
 *
 */
public class ReadOnlyMemoryMap implements RandomAccessData {
  private static final ScheduledExecutorService CLEANER = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r);
      thread.setName(ReadOnlyMemoryMap.class.getSimpleName() + "-cleaner");
      thread.setDaemon(true);
      return thread;
    }
  });
	private final File file;
	
	private volatile MappedByteBuffer[] chunks;
	private final RandomAccessFile randomAccessFile;
	private final long size;
	private final int numChunks;
	
	private int curChunkIndex;
	private volatile MappedByteBuffer curChunk;
	
	private final List<ReadOnlyMemoryMap> allInstances;

	public ReadOnlyMemoryMap(File file) throws IOException {
		this.file = file;
		this.allInstances = new ArrayList<ReadOnlyMemoryMap>();
		this.allInstances.add(this);
		
		this.randomAccessFile = new RandomAccessFile(file, "r");
		this.size = file.length();
		if(size<=0){
			throw new IllegalArgumentException("File Size Non-Positive. File Size: "+ size);
		}
		
		long numFullChunks = ((size-1)>>32);
		if (numFullChunks >= Integer.MAX_VALUE){
			throw new IllegalArgumentException("File Too Large. File Size: " + size);
		}
		
		numChunks = (int) (numFullChunks +1);
		
		int offset = 0;
		for (int i = 0; i<numFullChunks; ++i){
			chunks[i]= randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, offset, CHUNK_SIZE);
			offset+=CHUNK_SIZE;
		}

		long lastChunkSize = size - (numFullChunks*CHUNK_SIZE);
		if(lastChunkSize>0){
			chunks[numChunks-1] = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, offset, lastChunkSize);
		}
		
		curChunkIndex = 0;
		curChunk = chunks[0];
		curChunk.position(0);
	}
	
	public void seek(long position) throws IOException{
		if(position > this.size){
			throw new IOException("Data Referenced Outside of Range");
		}
		
		this.curChunkIndex = (int) (position >>> 32);
		MappedByteBuffer[] chunksArray = this.getChunks();
		MappedByteBuffer chunk = chunksArray[this.curChunkIndex];
		chunk.position((int) (position & BITMASK_32));
		this.curChunk = chunk;
	
	}
	
	public MappedByteBuffer[] getChunks() {
		return chunks;
	}

	@Override
	public int readUnsignedbyte() throws IOException {
		MappedByteBuffer chunk = this.getCurChunk();
		if(chunk.remaining() == 0){
			this.next();
			chunk = this.getCurChunk();
		}
		return ((int)curChunk.get()) & 0xFF;
	}

	private MappedByteBuffer getCurChunk() {
		return this.curChunk;
	}

	private void next() throws IOException {
		MappedByteBuffer[] chunksArray = getChunks();
		this.curChunkIndex++;
		
		if(this.curChunkIndex >= chunksArray.length){
			throw new IOException("Data Referenced Outside of Range");
		}
		
		MappedByteBuffer chunk = chunksArray[this.curChunkIndex];
		if (chunk == null){
			throw new IOException("Null Chunk");
		}
		chunk.position(0);
		this.curChunk = chunk;
	}
	
	public void read(byte[] buffer, int offset, int length) throws IOException{
		this.seek(offset);
		MappedByteBuffer chunk = getCurChunk();
		
		if (chunk == null){
			throw new IOException("Null Chunk");
		}
		
		int remaining = chunk.remaining();
		if(remaining >= length){
			chunk.get(buffer, offset, length);
			
		}else{
			chunk.get(buffer, offset, remaining);
			int leftoverLength = length - remaining;
			int pushedOffset = offset + remaining;
			next();
			read(buffer, pushedOffset, leftoverLength);
			
		}
	}
	
	public void close(){
		final MappedByteBuffer[] chunks;

		synchronized(allInstances){
			if (this.chunks==null){
				return;
			}	

			chunks = this.chunks;
			for(ReadOnlyMemoryMap map: allInstances){
				map.chunks = null;
				map.curChunk = null;
				try{
					map.randomAccessFile.close();
				}catch(IOException e){
					e.printStackTrace(System.err);
				}
			}	
		}

		CLEANER.schedule(new Runnable(){
			@Override
			public void run(){
				for(MappedByteBuffer chunk : chunks){
					ByteBufferCleaner.cleanMapping(chunk);
				}
			}
		},1000,TimeUnit.MILLISECONDS);
	}


}
