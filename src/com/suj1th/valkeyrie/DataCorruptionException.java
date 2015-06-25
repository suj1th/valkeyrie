package com.suj1th.valkeyrie;

/**
 * @author suj1th
 *
 */
public class DataCorruptionException extends Exception {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7677187281640767468L;
	
	/**
	 * Default Constructor
	 */
	public DataCorruptionException(){
		super();
	}
	
	/**
	 * @param message Message to be logged.
	 */
	public DataCorruptionException(String message){
		super(message);
	}
	
	/**
	 * @param throwable  a {@link Throwable}
	 */
	public DataCorruptionException(Throwable throwable){
		super(throwable);
	}

}
