package com.acme.productreader;

import com.acme.productreader.database.ProductReaderDba;

public class PrManager {

	private static PrManager mManager;
	private ProductReaderDba mDba;
	
	public static PrManager getManager() {
		if(mManager == null)
			mManager = new PrManager();
		return mManager;
	}
	
	public PrManager(){
		mDba = new ProductReaderDba();
	}
	
	public ProductReaderDba getDB() {
		return mDba;
	}
}
