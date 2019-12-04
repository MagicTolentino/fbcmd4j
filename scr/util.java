package Final;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;
import facebook4j.Post;
import facebook4j.internal.org.json.JSONObject;



public class util {
	private static final Logger login = LogManager.getLogger(Main.class);
	
	public static Properties loadConfigFile(String folder, String FN) throws IOException {
		Properties p = new Properties();
		Path configFolder = Paths.get(folder);
		Path configFile = Paths.get(folder, FN);
		if (!Files.exists(configFile)) {
			login.info("Archivo de Configuración.");
			
			if (!Files.exists(configFolder))
				Files.createDirectory(configFolder);
			
			Files.copy(utils.class.getResourceAsStream("MagicMike.propiedades"), configFile);
		}

		p.load(Files.newInputStream(configFile));
		BiConsumer<Object, Object> emptyProperty = (k, v) -> {
			if(((String)v).isEmpty())
				login.info("La propiedad '" + k + "' está vacía");
		};
		p.forEach(emptyProperty);

		return p;
	}
	
	public static void configTokens(String folder, String FN, Properties p, Scanner scanner) {
		if (p.getProperty("oauth.appId").isEmpty() || p.getProperty("oauth.appSecret").isEmpty()) {
			System.out.println("Por favor ingrese appId:");
			p.setProperty("oauth.appId", scanner.nextLine());
			System.out.println("Por favor ingrese appSecret:");
			p.setProperty("oauth.appSecret", scanner.nextLine());
			System.out.println("Por favor ingrese accessToken:");
			p.setProperty("oauth.accessToken", scanner.nextLine());
		}

		try {
			URL url = new URL("https://graph.facebook.com/v4.0/device/login");
	        Map<String,Object> params = new LinkedHashMap<>();
	        params.put("access_token", p.getProperty("oauth.appId")+"|"+p.getProperty("oauth.clientToken"));
	        params.put("scope", p.getProperty("oauth.permissions"));

	        StringBuilder postData = new StringBuilder();
	        for (Map.Entry<String,Object> param : params.entrySet()) {
	            if (postData.length() != 0) postData.append('&');
	            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	            postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	        }
	        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);

	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        StringBuilder sb = new StringBuilder();
	        for (int c; (c = in.read()) >= 0;)
	            sb.append((char)c);
	        String response = sb.toString();
	        
	        JSONObject obj = new JSONObject(response);
	        String code = obj.getString("code");
	        String userCode = obj.getString("user_code");
	        
			System.out.println("Ingresa a la página https://www.facebook.com/device con el código: " + userCode);

			String accessToken = "";
			while(accessToken.isEmpty()) {
		        try {
		            TimeUnit.SECONDS.sleep(15);
		        } catch (InterruptedException e) {
					login.error(e);
		        }

		        URL url1 = new URL("https://graph.facebook.com/v4.0/device/login_status");
		        params = new LinkedHashMap<>();
		        params.put("access_token", p.getProperty("oauth.appId")+"|"+p.getProperty("oauth.clientToken"));
		        params.put("code", code);
	
		        postData = new StringBuilder();
		        for (Map.Entry<String,Object> param : params.entrySet()) {
		            if (postData.length() != 0) postData.append('&');
		            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
		            postData.append('=');
		            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		        }
		        postDataBytes = postData.toString().getBytes("UTF-8");
	
		        HttpURLConnection conn1 = (HttpURLConnection)url1.openConnection();
		        conn1.setRequestMethod("POST");
		        conn1.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		        conn1.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		        conn1.setDoOutput(true);
		        conn1.getOutputStream().write(postDataBytes);

		        try {
		        	in = new BufferedReader(new InputStreamReader(conn1.getInputStream(), "UTF-8"));
			        sb = new StringBuilder();
			        for (int c; (c = in.read()) >= 0;)
			            sb.append((char)c);		        
			        response = sb.toString();
			        
			        obj = new JSONObject(response);
			        accessToken = obj.getString("access_token");
		        } catch(IOException ignore) {
		        }
		    }
			
	        p.setProperty("oauth.accessToken", accessToken);
	        
			saveProperties(folder, FN, p);
			System.out.println("Configuración guardada exitosamente.");
			login.info("Configuración guardada exitosamente.");
		} catch(Exception e) {
			login.error(e);
		}
	}

	public static void saveProperties(String folderName, String fileName, Properties props) throws IOException {
		Path configFile = Paths.get(folderName, fileName);
		props.store(Files.newOutputStream(configFile), "Generado por org.fbcmd4j.configTokens");
	}

	public static Facebook configFacebook(Properties props) {
		Facebook fb = new FacebookFactory().getInstance();
		fb.setOAuthAppId(props.getProperty("oauth.appId"), props.getProperty("oauth.appSecret"));
		fb.setOAuthPermissions(props.getProperty("oauth.permissions"));
		if(props.getProperty("oauth.accessToken") != null)
			fb.setOAuthAccessToken(new AccessToken(props.getProperty("oauth.accessToken"), null));
		
		return fb;
	}
	
	public static String savePostsToFile(String FN, List<Post> post) {
		File file = new File(FN + ".txt");

		try {
    		if(!file.exists()) {
    			file.createNewFile();
            }

    		FileOutputStream fos = new FileOutputStream(file);
			for (Post p : post) {
				String msg = "";
				if(p.getStory() != null)
					msg += "Story: " + p.getStory() + "\n";
				if(p.getMessage() != null)
					msg += "Mensaje: " + p.getMessage() + "\n";
				msg += "--------------------------------\n";
				fos.write(msg.getBytes());
			}
			fos.close();

			login.info("Posts guardados en el archivo '" + file.getName() + "'.");
			System.out.println("Posts guardados exitosamente en '" + file.getName() + "'.");
		} catch (IOException e) {
			login.error(e);
		}
        
        return file.getName();
	}	
}