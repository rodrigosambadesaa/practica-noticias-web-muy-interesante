package com.example.muyinteresanteNoTocar;

import java.io.InputStream;
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

/* Parsea un canal RSS y devuelve sus items en un ArrayList */

public class DescargaNoticiasRSS extends AsyncTask<String,Integer,ArrayList<NoticiaRSS>>{

	private Context contexto=null;
	private iNoticiaRSS objetoReceptor=null;
	private ProgressDialog pd=null;
	
	private static final String MENSAJE_PD="Descargando noticias...";
	
	
	public DescargaNoticiasRSS(Context contexto, iNoticiaRSS objetoReceptor){
		this.contexto = contexto;
		this.objetoReceptor = objetoReceptor;
	}


	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
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

	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		
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
			URLConnection conex = url.openConnection(); // Abrimos la conexion
			conex.setUseCaches(false); // Evitamos la cache de datos.
			conex.setRequestProperty("accept", "text/xml"); // Indicamos formato a recibir
			 
			 // Abrimos el fichero para su lectura/descarga
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
			e.printStackTrace();
			return null;
		}

	}
	
	
	@Override
	protected void onPostExecute(ArrayList<NoticiaRSS> result) {
		super.onPostExecute(result);
		
		if (pd!=null) pd.dismiss();
		if (objetoReceptor!=null ) objetoReceptor.onRecibeNoticiasRSS(result);
	}


	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		pd.setMessage(MENSAJE_PD + values[0]);
	}





}
