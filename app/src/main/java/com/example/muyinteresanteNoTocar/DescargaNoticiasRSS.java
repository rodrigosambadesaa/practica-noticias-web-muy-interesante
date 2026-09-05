package com.example.muyinteresanteNoTocar;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.example.muyinteresante.util.ConnectivityAndInternetAccess;
import com.example.muyinteresante.util.RemoteOperationPolicy;

/* Parsea un canal RSS y devuelve sus items en un ArrayList */

public class DescargaNoticiasRSS extends AsyncTask<String,Integer,ArrayList<NoticiaRSS>>{

	private Context contexto=null;
	private iNoticiaRSS objetoReceptor=null;
	private ProgressDialog pd=null;
	private boolean mostrarProgreso=true;
	private RemoteOperationPolicy.FailureAction failureAction;
	private Throwable failureCause;
	
	private static final String MENSAJE_PD="Descargando noticias...";
	
	
	public DescargaNoticiasRSS(Context contexto, iNoticiaRSS objetoReceptor){
		this(contexto, objetoReceptor, true);
	}

	/**
	 * Permite reutilizar el descargador para paginación/infinite scroll sin abrir
	 * un ProgressDialog modal cada vez que se solicitan noticias antiguas.
	 */
	public DescargaNoticiasRSS(Context contexto, iNoticiaRSS objetoReceptor, boolean mostrarProgreso){
		this.contexto = contexto;
		this.objetoReceptor = objetoReceptor;
		this.mostrarProgreso = mostrarProgreso;
	}


	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		if (contexto != null) {
			// Registramos inicio de intento de conexión para seguimiento de estado
			ConnectivityAndInternetAccess.beginConnectionAttempt(contexto);
		}
		
		if (mostrarProgreso && contexto != null) {
			pd = new ProgressDialog(contexto);
			pd.setMessage(MENSAJE_PD);
			pd.setCancelable(true);
			pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					DescargaNoticiasRSS.this.cancel(true);
				}
			});
			
			pd.show();
		}
	}

	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		
		// Finalizamos intento de conexión
		ConnectivityAndInternetAccess.endConnectionAttempt();
		
		if (pd!=null) pd.dismiss();
	}
	
	 
	@Override							// Recibe URL y nombre Canal RSS.
	protected ArrayList<NoticiaRSS> doInBackground(String... params) {
		
		InputStream entrada = null;
		
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(true);
			dbf.setCoalescing(true);
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			
			 // Creamos objeto URL a partir de la direccion web para conectarnos con el servidor
			URL url = new URL(params[0]);
			URLConnection conex = url.openConnection(); // La petición real es la prueba del feed.
			conex.setConnectTimeout(10000);
			conex.setReadTimeout(10000);
			conex.setUseCaches(false); // Evitamos la cache de datos.
			conex.setRequestProperty("accept", "application/rss+xml, application/xml, text/xml, */*");
			conex.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) practica-noticias-web-muy-interesante/1.0");
			 
			if (conex instanceof HttpURLConnection) {
				int statusCode = ((HttpURLConnection) conex).getResponseCode();
				if (RemoteOperationPolicy.classifyHttpResponse(statusCode)
						== RemoteOperationPolicy.FailureAction.FEED_UNAVAILABLE) {
					failureAction = RemoteOperationPolicy.FailureAction.FEED_UNAVAILABLE;
					Log.w("DescargaNoticiasRSS", "El feed respondió con HTTP " + statusCode);
					return null;
				}
			}

			// Abrimos el fichero para su lectura/descarga; redirects los gestiona HttpURLConnection.
			entrada = conex.getInputStream();	

			Document arbolXML =db.parse(entrada);
			entrada.close();
			Element raiz = arbolXML.getDocumentElement(); 
			raiz.normalize(); 
			
			ArrayList<NoticiaRSS> noticias = new ArrayList<NoticiaRSS>();
			
			NodeList listaItems = raiz.getElementsByTagName("item");
			
			for (int i=0;i<listaItems.getLength();i++){
				try {
					Element item = (Element)listaItems.item(i);
					noticias.add(new NoticiaRSS(item, params[1]));
					
					publishProgress(noticias.size());
				}
				catch(Exception e){ e.printStackTrace();}
			}
			
			return noticias;
		}
		catch (Exception e){
			failureCause = e;
			if (RemoteOperationPolicy.isAmbiguousConnectivityFailure(e)) {
				failureAction = RemoteOperationPolicy.FailureAction.CONNECTIVITY_PROBLEM;
			} else {
				failureAction = RemoteOperationPolicy.FailureAction.FEED_UNAVAILABLE;
			}
			Log.w("DescargaNoticiasRSS", "Error descargando el feed RSS", e);
			return null;
		}
		finally {
			if (entrada != null) {
				try {
					entrada.close();
				} catch (Exception ignored) { }
			}
		}

	}
	
	
	@Override
	protected void onPostExecute(ArrayList<NoticiaRSS> result) {
		super.onPostExecute(result);
		
		// Finalizamos el intento de la petición real antes de clasificar un fallo.
		ConnectivityAndInternetAccess.endConnectionAttempt();
		
		if (pd!=null) pd.dismiss();
		if (result != null) {
			if (objetoReceptor!=null) objetoReceptor.onRecibeNoticiasRSS(result);
			return;
		}

		if (failureAction == RemoteOperationPolicy.FailureAction.CONNECTIVITY_PROBLEM
				&& contexto != null && failureCause != null) {
			// Solo aquí, después de un fallo ambiguo sin respuesta HTTP válida.
			ConnectivityAndInternetAccess.checkInternetAsyncDefault(contexto,
					generalResult -> {
						RemoteOperationPolicy.FailureAction action =
								RemoteOperationPolicy.classifyAmbiguousFailure(
										failureCause,
										generalResult != null && generalResult.isReachable());
						if (objetoReceptor != null) objetoReceptor.onFalloNoticiasRSS(action);
					});
		} else if (objetoReceptor != null) {
			objetoReceptor.onFalloNoticiasRSS(
					failureAction != null ? failureAction : RemoteOperationPolicy.FailureAction.FEED_UNAVAILABLE);
		}
	}


	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (pd != null && values != null && values.length > 0) {
			pd.setMessage(MENSAJE_PD + " " + values[0]);
		}
	}
}
