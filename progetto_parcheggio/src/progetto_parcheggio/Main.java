package progetto_parcheggio;

public class Main {

	public static void main(String[] args) {
		
		// inizializzo il menu manager
		MenuManager menu_manager = new MenuManager();			

		// resto nel loop finchè il menu manager non va in stato ESCI
		while(!menu_manager.get_stato().equals("ESCI")){
			// mostro le opzioni dispnibili
			menu_manager.print_options();
			// leggo input utente
			menu_manager.read_input();			
		}
		
		System.out.println("Arrivederci");
	}

}
