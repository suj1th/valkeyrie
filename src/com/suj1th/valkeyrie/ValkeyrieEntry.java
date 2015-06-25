package com.suj1th.valkeyrie;


/**
 * @author suj1th
 *
 */
public class ValkeyrieEntry {

	private final int timestamp;
	private final int idFile;
	private final int offset;
	private final int total_size;
	
	
	
	public ValkeyrieEntry(int timestamp, int idFile, int offset, int total_size) {
		super();
		this.timestamp = timestamp;
		this.idFile = idFile;
		this.offset = offset;
		this.total_size = total_size;
	}
	
	
	boolean is_fresher_than( ValkeyrieEntry other ){
		return this.timestamp > other.timestamp ||
				((this.timestamp==other.timestamp) && (this.idFile> other.idFile || 
						((this.idFile == other.idFile) && (this.offset>other.offset))));
	}
	
}
