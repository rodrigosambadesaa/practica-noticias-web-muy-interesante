package com.example.muyinteresanteNoTocar;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Scanner;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import android.text.Html;


public class NoticiaRSS {
	/* Datos del item noticia RSS */
	private String titulo="";
	private String descripcion="";
	private String enlace="";
	private String strFecha="";
	private String urlImagen="";
	
	private Date fecha; // == strFecha

	// Constantes
	public static final String RSS_MUY_INTERESANTE="Muy interesante"; 				// Formato fecha item RSS estandard

	public static final SimpleDateFormat formateadorPubDate = new SimpleDateFormat("EEE,dd MMM yyyy HH:mm:ss", Locale.US);
	public static final SimpleDateFormat formateadorDate = new SimpleDateFormat("EEEE, dd 'de' MMMM 'del' yyyy, HH:mm", Locale.getDefault()); // Para mostrar fecha de la noticia en formato legible para el usuario
	
	
	private Date convierteFechaRSSaDate() throws ParseException {
		Date d=null;
		ParseException e;
		try {
			d = formateadorPubDate.parse(strFecha); // String a Date
			e=null;
		}
		catch (ParseException e1){
			e=e1;
			try {
				SimpleDateFormat formateadorPubDate2 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
				d = formateadorPubDate2.parse(strFecha); // String a Date
				e=null;
			}
			catch (ParseException e2) {
				e=e2;
				try {
					SimpleDateFormat formateadorPubDate3 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
					d = formateadorPubDate3.parse(strFecha); // String a Date
					e=null;
				}
				catch(ParseException e3){
					e=e3;
				}
			}
		}
		if (e!=null) throw e;
		return  d;
	}
	
	
	public NoticiaRSS() {}

	public NoticiaRSS(String titulo, String descripcion, String enlace, String urlImagen, Date fecha) {
		this.titulo = titulo != null ? titulo : "";
		this.descripcion = descripcion != null ? descripcion : "";
		this.enlace = enlace != null ? enlace : "";
		this.urlImagen = urlImagen != null ? urlImagen : "";
		this.fecha = fecha;
		if (fecha != null) {
			Calendar c = GregorianCalendar.getInstance();
			c.setTime(fecha);
			this.strFecha = NoticiaRSS.creaStrPubDate(c);
		}
	}

    /* Crea una noticia a partir del itemXML y el nombre del canal */
	public NoticiaRSS(Element itemXML, String canal) throws ParserConfigurationException, SAXException, IOException, ParseException{
		titulo=itemXML.getElementsByTagName("title").item(0).getFirstChild().getNodeValue().trim();
		descripcion=itemXML.getElementsByTagName("description").item(0).getFirstChild().getNodeValue().trim();
		enlace=itemXML.getElementsByTagName("link").item(0).getFirstChild().getNodeValue().trim();
		strFecha=itemXML.getElementsByTagName("pubDate").item(0).getFirstChild().getNodeValue().trim();
		
		fecha = convierteFechaRSSaDate();
		
		if (canal.equalsIgnoreCase(RSS_MUY_INTERESANTE)){ // Particularidades de este canal para extraer el URL de la imagen
		  try{  

			  Scanner s = new Scanner(descripcion); // Extraemos imagen y descripcion
			  s.useDelimiter("\\s*>\\s*");
			  String imagen="";
			  if (s.hasNext()) {
			     imagen = s.next();
			  }
			  s.close();
			  if (imagen.length()>0) {
				  Scanner s2 = new Scanner(imagen);
				  s2.useDelimiter("\\s*'|\"\\s*"); // Extraemos url
				  String str;
				  while(s2.hasNext()){
					  str=s2.next();
					  if (str.contains("http://") || str.contains("https://")){
						  urlImagen=str;
						  break;
					  }
				  }
				  s2.close();
				  
				  descripcion=descripcion.substring(imagen.length()+1);
				  descripcion=Html.fromHtml(descripcion.replaceAll("<[^>]*>","").replaceAll("\t","")).toString(); // Se elimina el codigo html
			  }
			 
			 
		  } catch (Exception e) { e.printStackTrace(); }
		    
		
		}

	}
	

	public Date getFechaNoticia() {
		return fecha;
	}

	public String getStrFecha() {
		return strFecha;
	}
	
	public void setFechaNoticia(Date fechaNoticia) {
		fecha = fechaNoticia;
		Calendar c =  GregorianCalendar.getInstance();
		c.setTime(fechaNoticia);
		strFecha = NoticiaRSS.creaStrPubDate(c);
	}


	public String getTitulo() {
		return titulo;
	}
	
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	
	public String getDescripcion() {
		return descripcion;
	}
	
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	public String getEnlace() {
		return enlace;
	}
	
	
	public void setEnlace(String enlace) {
		this.enlace = enlace;
	}
		
	public String getUrlImagen() {
		return urlImagen;
	}


	public void setUrlImagen(String urlImagen) {
		this.urlImagen = urlImagen;
	}

	/* Algunos canales RSS, introducen etiquetas HTML en la descripci�n de la noticia */
	public String getDescripcionSinHTML() {
		       //caracteres especiales
		return Html.fromHtml(descripcion.replaceAll("<[^>]*>","")).toString();
	}


	public String getFechaComoStringFormateado() {	
		try {
		  // Extraemos hora, minuto y segundos de la fecha y creamos un String con el formato h:m:s, haciendo la conversion horaria a local.
	      return formateadorDate.format(fecha); // Date a String
		}
		catch(Exception e){
		  return "";
		}
		
	}
	
	public static String creaStrPubDate(Calendar fecha){
		// A partir de la fecha recibida se crea un String en formato fecha "pubDate" RSS
		Date d = fecha.getTime();

	    return formateadorPubDate.format(d);
	}
	

	
	@Override
	public String toString() {
		
		return "Titulo: " +this.getTitulo() + "\n" + "Enlace: " + this.getEnlace() + "\n" + "Fecha: " +this.getFechaComoStringFormateado() + "\n" +
				"Descripcion:" + this.getDescripcion() + "\n" + "UrlImagen:" + this.getUrlImagen();
	}

}
