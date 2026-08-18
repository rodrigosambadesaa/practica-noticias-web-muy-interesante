package com.example.muyinteresanteNoTocar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.example.muyinteresante.util.ConnectivityAndInternetAccess;

public class AsignaImagenDeURL extends AsyncTask<String,Void,Void> {
	ImageView img;
	Bitmap mapaDeBits;
	File f;
	Context contexto;
	
	private String currentUrl;

	public AsignaImagenDeURL(ImageView img, Context c){
		this.img = img;
		contexto = c;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mapaDeBits = null;
		f = null;
		if (contexto != null) {
			ConnectivityAndInternetAccess.beginConnectionAttempt(contexto);
		}
	}

	@Override						// Recibe la url de la imagen a descargar
	protected Void doInBackground(String... param) {
		if (param == null || param.length == 0 || param[0] == null || param[0].isEmpty()) return null;
		currentUrl = param[0];
        try {
            URL url = new URL(currentUrl);
			f = Utilidades.getDiretorioCache(contexto);
            if (f != null) {  										
            	f = new File(f,"imagenes");
            	f.mkdirs(); // Crea las carpetas
     
            	// hasCode proporciona un valor numerico unico dado el texto de un URL, que a su vez es unico
            	f = new File(f, String.valueOf(url.hashCode()));
            
	            if (f.exists()){
	            	mapaDeBits = BitmapFactory.decodeStream(new FileInputStream(f));
	            }
	            else {
		            if (contexto != null && !ConnectivityAndInternetAccess.isConnectedOrConnecting(contexto)) {
		            	return null;
		            }
		            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
		            conexion.setConnectTimeout(8000);
		            conexion.setReadTimeout(8000);
		            conexion.connect();
		            InputStream entrada = conexion.getInputStream();
		            mapaDeBits = BitmapFactory.decodeStream(entrada);
					if (mapaDeBits != null) {
						try { // Guardamos la imagen para evitar posteriores descargas
							mapaDeBits.compress(CompressFormat.PNG, 95, new FileOutputStream(f));
						} catch (Exception e) { e.printStackTrace(); try {f.delete();} catch(Exception ex){} }
					}
		            entrada.close();
		        }
            }
            else {
				mapaDeBits = null;
			}

        } catch (Exception e) {
        	e.printStackTrace();
        	mapaDeBits = null;
        }
        
        return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		ConnectivityAndInternetAccess.endConnectionAttempt();
		if (img != null) {
			Object tag = img.getTag();
			if (mapaDeBits != null && (tag == null || tag.equals(currentUrl))) {
				img.setImageBitmap(mapaDeBits);
			}
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		ConnectivityAndInternetAccess.endConnectionAttempt();
		if (f != null && f.exists()) {
			try { f.delete(); } catch(Exception ex){} 
		}
	}
}
