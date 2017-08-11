package de.markuszopf.aiteacher.resources;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class ImageSearch {

	private static String SUBSCRIPTION_KEY = "ADD YOUR SUBSCRIPTION KEY HERE";

	public static String getImageURL(String searchTerm) {
		try {
			HttpClient httpclient = HttpClients.createDefault();
			URIBuilder builder = new URIBuilder("https://api.cognitive.microsoft.com/bing/v5.0/images/search");

			builder.setParameter("q", searchTerm);
			builder.setParameter("count", "1");

			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);
			request.setHeader("Content-Type", "multipart/form-data");
			request.setHeader("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY);

			// Request body
			StringEntity reqEntity = new StringEntity("{body}");
			request.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String json = EntityUtils.toString(entity);
				return json.split("\"contentUrl\": \"")[1].split("\",")[0]; // return URL of the image
			}

			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
