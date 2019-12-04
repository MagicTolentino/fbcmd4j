package Final;

import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.Post;
import facebook4j.ResponseList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fbcmd4j.utils.Utils;



public class Main {

	static final Logger login = LogManager.getLogger(Main.class);

	private static final String CON_D = "configuración";
	private static final String CON_A = "fbcmd4j.properties";

	public static void main (String[] args){

		login.info("Se comenzará la aplicación");

		Facebook fb = null;
		Properties p = null;

		try{
			p = utils.loadConfigFile(CON_D,CON_A);
		} catch (IOException ex){
			login.error(ex);
		}
		int op = 1;

		try{
			Scanner scan = new Scanner(System.in);
			while(true){

				fb = utils.configFacebook(p);
				System.out.print("Comando de Cliente en Línea por Tolentino\n");
				System.out.print("Lista de Opciones\n");
				System.out.print("Opción 0 - Configurar el Cliente");
				System.out.print("Opción 1 - Obtener NewsFeed");
				System.out.print("Opción 2 - Obtener Wall");
				System.out.print("Opción 3 - Publicar estado en Wall");
				System.out.print("Opción 4 - Publicar Link En Wall");
				System.out.print("Opción 5 - Salir");
		try{

			op = scan.nextInt();
			scan.nextLine();

			switch (op){

			case 0:

				Utils.configTokens(CON_D,CON_A,p,scan);
				p= Utils.loadConfigFile(CON_D,CON_A);
				break;

			case 1:
				System.out.print("NewsFeed en proceso");
				ResponseList<Post> nF = fb.getFeed();

				for (Post po : nF){

					Utils.printPost(po);
				}

				askToSaveFile("NewsFeed",nF,scan);
				break;

			case 2:
				System.out.print("Mostrar Wall");

				ResponseList<Post> wall = fb.getPosts();
				saveFile("Wall",wall,scan);
				break;
				break;

			case 3:
				System.out.print("Estado");
				String dato = scan.nextLine();
				fb.postStatusMessage(dato);
				break;

			case 4:
				System.out.print("Link");
				String l = scan.nextLine();
				Utils.postLink(l, fb);
				break;

			case 5:
				System.out.print("Cliente Terminado");
				System.exit(0);
				break;

			default:
				break;
			}
		} catch (InputMismatchException ex){

			System.out.print("Error de entrada");
			login.error("Inválido", ex.getClass());
		} catch (Exception ex) {

			System.out.print("Error");
			login.error(ex);
		}

		System.out.printIn();
			}
		} catch (Exception e){
			login.error(e);
		}
	}


public static void ToSavefile (String FN, ResponseList <Post> p, Scanner scan){

	System.out.print("Guardar? s/n");
	String option = scan.nextLine();

	if(option == "s"){
		List<Post> ps = new ArrayList<>();
		int f=0;

		while (f<=0){
			try{
				System.out.print("Favor de teclear cuantos post quieres guardar");
				f = Integer.parseInt(scan.nextLine());

				if (f<=0){
					System.out.print("Error");

				} else {

					for (int i=0;i<f;i++){
						if(i>p.size()-1) break;
						ps.add(p.get(i));
					}
				}
			} catch(NumerFormatexception e){
				login.error(e);
			}
		}
		Utils.savePostsToFile(FN,ps);
	}
}
}
