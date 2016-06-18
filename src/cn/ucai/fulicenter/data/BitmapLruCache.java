package cn.ucai.fulicenter.data;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
	public BitmapLruCache(int maxSize) {
		super(maxSize);
	}

	@Override
	protected int sizeOf(String key, Bitmap bitmap) {
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	@Override
	public Bitmap getBitmap(String url) {
		Bitmap bitmap = get(url);
		if (bitmap == null) {
			bitmap = RequestManager.getBitmap(url);
			if (bitmap != null) {
				putBitmap(url,bitmap);
			}
		}
		return bitmap;
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		put(url, bitmap);
		RequestManager.putBitmap(url, bitmap);
	}

}
