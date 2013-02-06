package com.milissa.appengine;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class AppEngineInterface {

	public boolean enviarArquivo(Descricao desc) throws
				UnsupportedEncodingException{
		
		try {
			URL url = new URL("http://localhost:8888/upload");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			OutputStreamWriter writer = new
					OutputStreamWriter(connection.getOutputStream());
			writer.write("id=" + desc.getId() + "&descricao=" + desc.getDescricao());
			writer.close();
			if (connection.getResponseCode() ==	HttpURLConnection.HTTP_OK) {
			// OK
				return true;
			} else {
			// Server returned HTTP error code.
			}
		} catch (MalformedURLException e) {
			// ...
				System.out.println("MalformedURLException");
		} catch (IOException e) {
			// ...
				System.out.println("IOException");
		}
		return false;
	}
	
	public Descricao receberArquivo(String getId) throws UnsupportedEncodingException{
		Descricao desc = null;
		try {
			URL url = new URL("http://localhost:8888/server?get=" + getId);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			String id;
			String descS;
			id = connection.getHeaderField("id");
			descS = connection.getHeaderField("descricao");
			//System.out.println(id);
			//System.out.println(descS);
			desc = new Descricao(id, descS);
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			} else {
				// Server returned HTTP error code.
			}
		} catch (MalformedURLException e) {
		// ...
		} catch (IOException e) {
		// ...
		}
		return desc;
	}
}
