package progetto_parcheggio;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

// questa classe si occupa dell'interfaccia con l'utente e della comunicazione con il DB manager
public class MenuManager {
	// lo stato indica in che punto ci troviamo del menu (vd. dopo)
	private String stato;
	// lo scanner legge l'input utente
	private Scanner scanner = new Scanner(System.in);
	// uso il DB manager per tutte le operazioni su DB
	DBManager db_manager;
	
	// questa variabile mappa uno stato a una seconda mappa. La seconda mappa collega una scelta utente a un nuovo stato
	private HashMap<String, HashMap<Integer, String>> transizioni;
	
	public MenuManager() {
		this.db_manager = new DBManager();
		this.stato = "START";
		
		// inizializzo transizioni
		this.transizioni = new HashMap<String, HashMap<Integer, String>>();
		// questa variabile mappa una selezione a uno stato di destinazione quando ci troviamo in START
		HashMap<Integer, String> start_trans = new HashMap<Integer, String>();
		//collega scelta 1 a AMMINISTRATORE HOME
		start_trans.put(1, "AMMINISTRATORE_HOME");
		//collega scelta 2 a CLIENTE HOME
		start_trans.put(2, "CLIENTE_HOME");
		//collega scelta 3 a ESCI
		start_trans.put(3, "ESCI");
		// aggiungo questa mappa nella mappa generale, collegandola allo stato cui fa riferimento cioè START
		transizioni.put("START", start_trans);
		
		// faccio la stessa cosa per tutti gli altri stati
		HashMap<Integer, String> amministratore_home_trans = new HashMap<Integer, String>();
		amministratore_home_trans.put(1, "AMMINISTRATORE_CREA_PARCHEGGIO");
		amministratore_home_trans.put(2, "AMMINISTRATORE_AGGIUNGI_POSTO_AUTO");
		amministratore_home_trans.put(3, "AMMINISTRATORE_VISUALIZZA_PRENOTAZIONI");
		amministratore_home_trans.put(4, "START");
		amministratore_home_trans.put(5, "ESCI");
		transizioni.put("AMMINISTRATORE_HOME", amministratore_home_trans);
		
		HashMap<Integer, String> amministratore_crea_parcheggio_trans = new HashMap<Integer, String>();
		amministratore_crea_parcheggio_trans.put(1, "AMMINISTRATORE_CREA_PARCHEGGIO");
		amministratore_crea_parcheggio_trans.put(2, "AMMINISTRATORE_HOME");
		amministratore_crea_parcheggio_trans.put(3, "ESCI");
		transizioni.put("AMMINISTRATORE_CREA_PARCHEGGIO", amministratore_crea_parcheggio_trans);
		
		HashMap<Integer, String> amministratore_crea_posto_auto_trans = new HashMap<Integer, String>();
		amministratore_crea_posto_auto_trans.put(1, "AMMINISTRATORE_AGGIUNGI_POSTO_AUTO");
		amministratore_crea_posto_auto_trans.put(2, "AMMINISTRATORE_HOME");
		amministratore_crea_posto_auto_trans.put(3, "ESCI");
		transizioni.put("AMMINISTRATORE_AGGIUNGI_POSTO_AUTO", amministratore_crea_posto_auto_trans);
		
		HashMap<Integer, String> amministratore_visualizza_prenotazioni_trans = new HashMap<Integer, String>();
		amministratore_visualizza_prenotazioni_trans.put(1, "AMMINISTRATORE_HOME");
		amministratore_visualizza_prenotazioni_trans.put(2, "ESCI");
		transizioni.put("AMMINISTRATORE_VISUALIZZA_PRENOTAZIONI", amministratore_visualizza_prenotazioni_trans);
		
		HashMap<Integer, String> cliente_home_trans = new HashMap<Integer, String>();
		cliente_home_trans.put(1, "CLIENTE_PRENOTA_PARCHEGGIO");
		cliente_home_trans.put(2, "CLIENTE_VISUALIZZA_PRENOTAZIONI");
		cliente_home_trans.put(3, "ESCI");
		transizioni.put("CLIENTE_HOME", cliente_home_trans);
		
		HashMap<Integer, String> cliente_prenota_parcheggio_trans = new HashMap<Integer, String>();
		cliente_prenota_parcheggio_trans.put(1, "CLIENTE_PRENOTA_PARCHEGGIO");
		cliente_prenota_parcheggio_trans.put(2, "CLIENTE_HOME");
		cliente_prenota_parcheggio_trans.put(3, "ESCI");
		transizioni.put("CLIENTE_PRENOTA_PARCHEGGIO", cliente_prenota_parcheggio_trans);
		
		HashMap<Integer, String> cliente_visualizza_prenotazioni_trans = new HashMap<Integer, String>();
		cliente_visualizza_prenotazioni_trans.put(1, "CLIENTE_HOME");
		cliente_visualizza_prenotazioni_trans.put(2, "ESCI");
		transizioni.put("CLIENTE_VISUALIZZA_PRENOTAZIONI", cliente_visualizza_prenotazioni_trans);
	}
	
	// mostra all'amministratore le prenotazioni nel DB
	private void amministratore_visualizza_prenotazioni() {
		
		System.out.println("Seleziona parcheggio:");
		// chiedo al DB manager l'elenco parcheggi e li mostro
		ArrayList<String> parcheggi = db_manager.elenco_parcheggi();
		int N = parcheggi.size();
		
		for(int i = 0; i<parcheggi.size(); i++) {
			System.out.printf("%d: %s\n", i, parcheggi.get(i));
		}
		// leggo la scelta e seleziono il parcheggio corrispondente dall'arraylist
		String tmp = this.scanner.nextLine();
		
		while(tmp.isEmpty()) {
			System.out.println("Nessuna scelta effettuata. Scegliere un parcheggio.");
			tmp = this.scanner.nextLine();
		}
		int scelta = Integer.parseInt(tmp);
		
		while(scelta < 0 || scelta >= N) {
			System.out.println("Scelta errata. Riprovare.");
			scelta = Integer.parseInt(this.scanner.nextLine());
		}
		
		String nome = parcheggi.get(scelta);
		
		// chiamo l'apposita funzione (vd. implementazione per dettagli)
		this.db_manager.visualizza_prenotazioni("amministratore", nome);
	}
	
	// mostra al cliente le sue prenotazioni
	private void cliente_visualizza_prenotazioni() {
		
		System.out.println("Inserisci il tuo nome:");
		String nome_cliente = this.scanner.nextLine();
		
		while(nome_cliente.isEmpty()) {
			System.out.println("Inserisci il tuo nome:");
			nome_cliente = this.scanner.nextLine();
		}
		
		this.db_manager.visualizza_prenotazioni("cliente", nome_cliente);
	}

	
	
	// questa funzione crea un nuovo parcheggio
	private void crea_parcheggio() {
		System.out.println("Inserisci nome parcheggio.");
		String nome = this.scanner.nextLine();
		
		// verifico che non esista già un parcheggio con questo nome e nel caso continuo a chiederne uno nuovo
		while(db_manager.parcheggio_esiste(nome) || nome.isEmpty()) {
			System.out.println("Nome già presente oppure non valido. Inserisci nome parcheggio.");
			nome = this.scanner.nextLine();
		}

		// chiedo altri dati
		System.out.println("Inserisci Indirizzo");
		String indirizzo = this.scanner.nextLine();
		
		while(indirizzo.isEmpty()) {
			System.out.println("Inserisci Indirizzo");
			indirizzo = this.scanner.nextLine();
		}

		System.out.println("Inserisci costo giornaliero in euro (formato 0.00)");
		String tmp = this.scanner.nextLine();
		
		// controllo formato input: o un solo numero o un decimale con 2 cifre dopo il punto
		while(!tmp.matches("^\\d$|\\d\\.\\d{2}$")){
			System.out.println("Input errato. Inserisci costo giornaliero in euro (formato 0.00)");
			tmp = this.scanner.nextLine();
		}
		
		
		float costo = Float.parseFloat(tmp);
		
			
		System.out.println("Inserisci capienza massima");
		tmp = this.scanner.nextLine();
		
		while(tmp.isEmpty()) {
			System.out.println("Inserisci capienza massima");
			tmp = this.scanner.nextLine();
		}
		
		int capienza_massima = Integer.parseInt(tmp);
		
		while(capienza_massima <= 0) {
			System.out.println("Per favore inserisci un numero > 0");
			capienza_massima = Integer.parseInt(this.scanner.nextLine());
		}
		
		// chiamo la funzione apposita per creare parcheggio
		if(this.db_manager.crea_parcheggio(nome, indirizzo, costo, capienza_massima))
			System.out.println("Parcheggio creato.");
		else
			System.out.println("Si è verificato un problema nella creazione del parcheggio.");
	}
	
	
	
	// funzione per creare posto auto (funzionamento del tutto simile alle ufnzioni precedenti)
	private void crea_posto_auto() {
		System.out.println("Scegli parcheggio.");
		ArrayList<String> parcheggi = db_manager.elenco_parcheggi();
		int N = parcheggi.size();
		
		for(int i = 0; i<parcheggi.size(); i++) {
			System.out.printf("%d: %s\n", i, parcheggi.get(i));
		}
		
		String tmp = this.scanner.nextLine();
		
		while(tmp.isEmpty()) {
			System.out.println("Nessuna scelta effettuata. Scegliere un parcheggio.");
			tmp = this.scanner.nextLine();
		}
		int scelta = Integer.parseInt(tmp);
		
		while(scelta < 0 || scelta >= N) {
			System.out.println("Scelta errata. Riprovare.");
			scelta = Integer.parseInt(this.scanner.nextLine());
		}
		
		String nome = parcheggi.get(scelta);	
		
		if(this.db_manager.posti_da_creare_per_parcheggio(nome) > 0) {
			// chiedo di scegliere tra coperto e scoperto e converto in boolean
			System.out.println("Seleziona coperto (1) o scoperto (2)");
			tmp = this.scanner.nextLine();
			
			while(tmp.isEmpty()) {
				System.out.println("Seleziona coperto (1) o scoperto (2)");
				tmp = this.scanner.nextLine();
			}
			
			int coperto_int = Integer.parseInt(tmp);
			
			while(coperto_int != 1 && coperto_int != 2) {
				System.out.println("Input errato. Seleziona coperto (1) o scoperto (2)");
				coperto_int = Integer.parseInt(this.scanner.nextLine());
			}
			
			boolean coperto = coperto_int == 1;
			
			System.out.println("Seleziona la posizione (numero > 0)");
			tmp = this.scanner.nextLine();
			
			while(tmp.isEmpty()) {
				System.out.println("Seleziona la posizione (numero > 0)");
				tmp = this.scanner.nextLine();
			}
			
			int posizione = Integer.parseInt(tmp);
			
			// verifico che la posizione sia un numero positivo e che non esista già un posto auto con quella posizione
			while(posizione < 0 || db_manager.posto_auto_esiste(nome, posizione)) {
				System.out.println("Input errato oppure posto auto già esistente. Inserire altra posizione (numero > 0)");
				posizione = Integer.parseInt(this.scanner.nextLine());
			}
			
			// chiamo la funzione apposita
			this.db_manager.crea_posto_auto(nome, coperto, posizione);
		}else {
			System.out.println("Sono già stati creati tutti i posti possibili per questo parcheggio.");
			return;
		}

		
		
		
	}
	
	// funzione per prenotare un parcheggio. Anche qui si seleziona un parcheggio tra quelli disponibili, si specificano giorno di inizio e fine e se coperto o scoperto
	private void prenota_parcheggio() {
		System.out.println("Inserisci il tuo nome.");
		String nome_cliente = this.scanner.nextLine();
		
		while(nome_cliente.isEmpty()) {
			System.out.println("Inserisci il tuo nome.");
			nome_cliente = this.scanner.nextLine();
		}
		
		System.out.println("Seleziona parcheggio:");
		ArrayList<String> parcheggi = db_manager.elenco_parcheggi();
		int N = parcheggi.size();
		
		for(int i = 0; i<parcheggi.size(); i++) {
			System.out.printf("%d: %s\n", i, parcheggi.get(i));
		}
		
		String tmp = this.scanner.nextLine();
		
		while(tmp.isEmpty()) {
			System.out.println("Nessuna scelta effettuata. Scegliere un parcheggio.");
			tmp = this.scanner.nextLine();
		}
		int scelta = Integer.parseInt(tmp);
		
		
		while(scelta < 0 || scelta >= N) {
			System.out.println("Scelta errata. Riprovare.");
			scelta = Integer.parseInt(this.scanner.nextLine());
		}
		
		String nome = parcheggi.get(scelta);
		
		System.out.println("Seleziona coperto (1) o scoperto (2)");
		tmp = this.scanner.nextLine();
		
		while(tmp.isEmpty()) {
			System.out.println("Seleziona coperto (1) o scoperto (2)");
			tmp = this.scanner.nextLine();
		}
		
		int coperto_int = Integer.parseInt(tmp);
		
		while(coperto_int != 1 && coperto_int != 2) {
			System.out.println("Input errato. Seleziona coperto (1) o scoperto (2)");
			coperto_int = Integer.parseInt(this.scanner.nextLine());
		}
		
		boolean coperto = coperto_int == 1;
		
		System.out.println("Seleziona data inizio (aaaa-mm-gg)");
		String data_inizio = this.scanner.nextLine();
		
		// verifico che la data inserita sia nel formato giusto: 4 cifre - 2 cifre - 2 cifre	
		while(!data_inizio.matches("\\d{4}-\\d{2}-\\d{2}")) {
			System.out.println("Input errato. Seleziona data inizio (aaaa-mm-gg)");
			data_inizio = this.scanner.nextLine();
		}
		
		// converto in tipo Date e lo confronto con la data odierna
		Date d1 = Date.valueOf(data_inizio);
		Date today = Date.valueOf(LocalDate.now());	
		
		while(d1.compareTo(today) < 0) {
			System.out.println("Non si può inserire una data precedente a oggi. Seleziona data inizio (aaaa-mm-gg)");
			data_inizio = this.scanner.nextLine();
			d1 = Date.valueOf(data_inizio);
		}
		
		System.out.println("Seleziona data fine (aaaa-mm-gg)");
		String data_fine = this.scanner.nextLine();
		
		while(!data_fine.matches("\\d{4}-\\d{2}-\\d{2}")) {
			System.out.println("Input errato. Seleziona data fine (aaaa-mm-gg)");
			data_fine = this.scanner.nextLine();
		}
		
		// converto in Date e confronto con la data di inizio inserita prima
		Date d2 = Date.valueOf(data_fine);		
		
		while(d1.compareTo(d2) >= 0) {
			System.out.println("La data fine deve essere successiva alla data inizio. Riprovare.");
			data_fine = this.scanner.nextLine();
			d2 = Date.valueOf(data_fine);
		}
		
		// vrifico disponibilità in base a questi criteri. In caso positivo la fuzione ritorna l'ID di un posto che li soddisfi
		int[] result = this.db_manager.verifica_disponibilita(nome, coperto, d1, d2);
		int id_posto_libero = result[0];
		int posizione_posto_libero = result[1];
		
		if(id_posto_libero >= 0) {
			// calcolo costo
			float costo = this.db_manager.calcola_costo(nome, d1, d2);
			// chiedo conferma
			System.out.println(String.format("Costo totale: %.2f euro. Confermare? (S=sì, N=no)", costo));
			String conferma = this.scanner.nextLine();
			
			while(conferma.isEmpty() || !(conferma.equals("S") || conferma.contentEquals("N"))) {
				System.out.println("Input errato. Confermare? (S=sì, N=no)");
				conferma = this.scanner.nextLine();
			}
			
			if(conferma.equals("S")) {
				// creo la prenotazione
				if(this.db_manager.crea_prenotazione(nome_cliente, id_posto_libero, d1, d2, costo)) {
					System.out.println(String.format("Prenotazione avvenuta con successo. Costo totale: %.2f euro, posizione posto assegnato: %d", costo, posizione_posto_libero));
				}				
				else
					System.out.println("Si è verificato un errore nella prentoazione.");
			}			
		}else {
			System.out.println("Purtroppo non ci sono posti che soddisfino questi requisiti.");
		}
	}
	
	// questa funzione mostra le opzioni disponibili a seconda dello stato in cui ci troviamo
	public void print_options() {
		switch(this.stato) {
			case "START":
				System.out.println("Scegliere modalità\n1: Amministratore\n2: Cliente\n3: Esci");
				break;
			case "AMMINISTRATORE_HOME":
				System.out.println("1: Crea nuovo parcheggio\n2: Aggiungi posto auto\n3: Visualizza prenotazioni\n4:Indietro\n5:Esci");
				break;
			case "AMMINISTRATORE_CREA_PARCHEGGIO":
				crea_parcheggio();
				System.out.println("1: Crea nuovo parcheggio\n2: Indietro\n3: Esci");
				break;
			case "AMMINISTRATORE_AGGIUNGI_POSTO_AUTO":
				crea_posto_auto();
				System.out.println("Posto auto creato.\n1: Crea nuovo posto auto\n2: Indietro\n3: Esci");
				break;
			case "AMMINISTRATORE_VISUALIZZA_PRENOTAZIONI":
				amministratore_visualizza_prenotazioni();
				System.out.println("1: Indietro\n2: Esci");
				break;
			case "CLIENTE_HOME":
				System.out.println("1: Prenota un parcheggio\n2: Visualizza le tue prenotazioni\n3: Esci");
				break;
			case "CLIENTE_PRENOTA_PARCHEGGIO":
				prenota_parcheggio();
				System.out.println("1: Prenota un altro parcheggio\n2: Indietro\n3: Esci");
				break;
			case "CLIENTE_VISUALIZZA_PRENOTAZIONI":
				cliente_visualizza_prenotazioni();
				System.out.println("1: Indietro\n2: Esci");
				
		}
		
	}
	
	
	// questa funzione legge l'opzione dell'utente, verifica che sia ammessa e cambia lo stato corrente a seconda qi quello che è specificato nelle transizioni
	public void read_input() {
		// leggo input utente
		String tmp = this.scanner.nextLine();
		
		while(tmp.isEmpty()) {
			System.out.println("Scegliere un'opzione.");
			tmp = this.scanner.nextLine();
		}
		int input_int = Integer.parseInt(tmp);
		// cambio stato a seconda di quello corrente e dell'input usando la variabile transizioni (vd. sopra)
		HashMap<Integer, String> possibili_transizioni = this.transizioni.get(this.stato);
		if(possibili_transizioni.containsKey(input_int))
			this.stato = possibili_transizioni.get(input_int);
		else
			System.out.println("Selezione errata");		
	}
	
	// getter per variabile stato
	public String get_stato() {
		return this.stato;
	}
}
