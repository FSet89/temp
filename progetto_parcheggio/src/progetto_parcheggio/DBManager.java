package progetto_parcheggio;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

public class DBManager {
	// credenziali
	private String DB_URL = "jdbc:mysql://localhost/";
	private String DB_NAME = "DB_parcheggio";
	private String DB_USER = "admin";
	private String DB_PASSWORD = "finalfantasy";
	private Connection con;
	
	public DBManager() {
			// connessione
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}

			try {
				this.con = DriverManager.getConnection(
				DB_URL+DB_NAME, DB_USER, DB_PASSWORD);				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// inizializza percheggio di esempio se non esiste, comodo per fare dei test
			boolean parcheggio_test_inizializzato = false;
			String query = "SELECT * FROM parcheggi WHERE nome='parcheggio_test'";
			Statement stmt = null;
			try {
				stmt = this.con.createStatement();		
				ResultSet rs = stmt.executeQuery(query);
				// se la query ritorna qualcosa vuol dire che c'è già
				while (rs.next()) {
					parcheggio_test_inizializzato = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if(!parcheggio_test_inizializzato) {
				System.out.println("Inizializzazione parcheggio di test...");
				inizializza_parcheggio_test();
			}
	}
	
	// questa funzione crea un parcheggio di test con qualche posto auto 
	private void inizializza_parcheggio_test() {
		// inserisco manualmente l'ID parcheggio così posso inserire subito anche i posti auto specificando quell'ID. In un caso reale non si farebbe
		String query1 = "INSERT INTO parcheggi (id, nome, indirizzo, costo_giornaliero, capienza_massima) VALUES (999, 'parcheggio_test', 'indirizzo_test', 5, 150)";
		String query2 = "INSERT INTO posti_auto (id_parcheggio, coperto, posizione) VALUES (999, 0, 1)";
		String query3 = "INSERT INTO posti_auto (id_parcheggio, coperto, posizione) VALUES (999, 0, 2)";
		String query4 = "INSERT INTO posti_auto (id_parcheggio, coperto, posizione) VALUES (999, 1, 3)";
		String query5 = "INSERT INTO posti_auto (id_parcheggio, coperto, posizione) VALUES (999, 1, 4)";
		Statement stmt = null;
		try {
			stmt = this.con.createStatement();		
			stmt.executeUpdate(query1);
			stmt.executeUpdate(query2);
			stmt.executeUpdate(query3);
			stmt.executeUpdate(query4);
			stmt.executeUpdate(query5);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	// dato un parcheggio e delle date calcola il costo totale
	public float calcola_costo(String nome_parcheggio, Date data_inizio, Date data_fine) {
		// sottraggo le due date e converto da millisecondi a giorni
		long durata = (data_fine.getTime() - data_inizio.getTime())/(1000*3600*24);
		// seleziono costo giornaliero parcheggio e moltiplico per durata
		String query = String.format("SELECT costo_giornaliero FROM parcheggi WHERE nome='%s'", nome_parcheggio);
		Statement stmt = null;
		try {
			stmt = this.con.createStatement();		
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				// prendo costo giornaliero e moltiplico per durata
				return durata*rs.getFloat("costo_giornaliero");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	// funzione per inserire un parcheggio nel DB
	public boolean crea_parcheggio(String nome, String indirizzo, float costo, int capienza_massima) {
		// il primo argomento di String.format serve a far sì che il separatore decimale sia punto e non virgola
		String query = String.format(Locale.ROOT, "INSERT INTO parcheggi (nome, indirizzo, costo_giornaliero, capienza_massima) VALUES ('%s', '%s', %.2f, %d)", nome, indirizzo, costo, capienza_massima);
		Statement stmt = null;
		try {
			stmt = this.con.createStatement();		
			int rs = stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	// funzione per associare un posto auto a un parcheggio nel DB
	public boolean crea_posto_auto(String nome_parcheggio, boolean coperto, int posizione) {
		// converto la variabile coperto in intero per inserirlo nel DB
		int coperto_int;
		
		if(coperto)
			coperto_int = 1;
		else
			coperto_int = 0;
		String query = String.format("INSERT INTO posti_auto (id_parcheggio, coperto, posizione) "
				+ "VALUES ((SELECT id FROM parcheggi WHERE nome='%s'), %d, %d)", nome_parcheggio, coperto_int, posizione);
		Statement stmt = null;
		try {
			stmt = this.con.createStatement();		
			int rs = stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	// funzione per inserire nuova prenotazione nel DB
	public boolean crea_prenotazione(String nome_cliente, int id_posto_auto, Date data_inizio, Date data_fine, float costo) {
		String query = String.format(Locale.ROOT, "INSERT INTO prenotazioni (nome_cliente, id_posto_auto, data_inizio, data_fine, costo) "
				+ "VALUES ('%s', %d, '%s', '%s', %.2f)", nome_cliente, id_posto_auto, data_inizio, data_fine, costo);
		Statement stmt = null;
		try {
			stmt = this.con.createStatement();		
			int rs = stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
			return false;
		}
		return true;
	}
	
	// restituisce l'elenco parcheggi esistenti in forma di arraylist
	public ArrayList<String> elenco_parcheggi() {
		ArrayList<String> parcheggi = new ArrayList<String>();
		
		String query = "SELECT nome FROM parcheggi";
		Statement stmt = null;
		try {
			stmt = this.con.createStatement();		
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				parcheggi.add(rs.getString("nome"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		return parcheggi;
	}
	
	// verifica che un dato parcheggio esista nel DB
	public boolean parcheggio_esiste(String nome) {
		String query = String.format("SELECT id FROM parcheggi WHERE nome='%s'", nome);
		Statement stmt = null;
		try {
			stmt = this.con.createStatement();		
			ResultSet rs = stmt.executeQuery(query);
			// se entriamo nel loop vuol dire che il parcheggio esiste
			while (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// restituisce il numero di posti auto non ancora inseriti nel DB per un dato parcheggio
	public int posti_da_creare_per_parcheggio(String nome_parcheggio) {
		String query = String.format("SELECT count(pa.id) AS N, p.capienza_massima AS N_tot "
									+ "FROM posti_auto AS pa RIGHT JOIN parcheggi AS p ON pa.id_parcheggio = p.id "
									+ "WHERE p.nome = '%s'", nome_parcheggio);
		Statement stmt = null;
		try {
			stmt = this.con.createStatement();		
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int N = Integer.parseInt(rs.getString("N"));
				int N_tot = Integer.parseInt(rs.getString("N_tot"));
				
				return N_tot - N;
			}
		} catch (SQLException e) {
			System.out.println(query);
			e.printStackTrace();
		}
		return 0;
	}
	
	// come prima ma con posto auto
	public boolean posto_auto_esiste(String nome_parcheggio, int posizione) {
		String query = String.format("SELECT id FROM posti_auto "
				+ "WHERE id_parcheggio=(SELECT id FROM parcheggi WHERE nome='%s') AND posizione=%d", nome_parcheggio, posizione);
		Statement stmt = null;
		try {
			stmt = this.con.createStatement();		
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// dato un parcheggio e dei criteri verifico che ci sia un posto auto che li soddisfi
	public int[] verifica_disponibilita(String nome_parcheggio, boolean coperto, Date data_inizio, Date data_fine) {
		// converto la variabile coperto in intero per inserirlo nel DB
		int coperto_int;
		
		if(coperto)
			coperto_int = 1;
		else
			coperto_int = 0;
		// verifico che ci sia un posto auto che soddisfi i requisiti e che non ci siano prenotazioni che si sovrappongono nei giorni indicati
		// significato della query: seleziona ID dei posti auto tali che appartengano al parcheggio X, siano coperti/scoperti e non esista una prenotazione in quei posti auto i cui giorno si sovrappongano con quelli desiderati adesso
		String query = String.format("SELECT p.id, p.posizione FROM posti_auto AS p "
									+ "WHERE p.id_parcheggio=(SELECT id FROM parcheggi WHERE nome='%s') "
									+ "AND p.coperto=%d "
									+ "AND NOT EXISTS (SELECT * FROM prenotazioni "
													+ "WHERE id_posto_auto=p.id "
													+ "AND ((data_inizio <= '%s' AND data_fine >= '%s') "
														+ "OR (data_inizio >= '%s' AND data_fine <= '%s')"
														+ "OR (data_inizio <= '%s' AND data_fine >= '%s')"
														+ "OR (data_inizio <= '%s' AND data_fine >= '%s')))", 
									nome_parcheggio, coperto_int, data_inizio, data_inizio, data_inizio, data_fine, data_fine, data_fine, data_inizio, data_fine);


		Statement stmt = null; 
		int[] result = new int[2];
		result[0] = -1;
		result[1] = -1;
		try {
			stmt = this.con.createStatement();		
			ResultSet rs = stmt.executeQuery(query);		
			
			// ritorno l'ID del primo posto auto disponibile
			while (rs.next()) {				
				result[0] = rs.getInt("id");
				result[1] = rs.getInt("posizione");			
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
		}
		return result;
	}
	
	public void visualizza_prenotazioni(String modalita, String nome) {
		// se modalita = amministratore mostra le prenotazioni per un dato parcheggio
		// se modalita = cliente mostra le prenotazini per un dato cliente
		String query;
		if(modalita.equals("amministratore")) {
			System.out.println(String.format("Elenco prenotazioni per il parcheggio %s", nome));
			query = String.format("SELECT pa.nome, po.posizione, po.coperto, pr.data_inizio, pr.data_fine, pr.costo "
										+ "FROM prenotazioni AS pr JOIN posti_auto AS po ON pr.id_posto_auto = po.id JOIN parcheggi AS pa ON po.id_parcheggio = pa.id "
										+ "WHERE pa.nome = '%s'", nome);
		}else {
			System.out.println(String.format("Elenco prenotazioni per il cliente %s", nome));
			query = String.format("SELECT pa.nome, po.posizione, po.coperto, pr.data_inizio, pr.data_fine, pr.costo "
										+ "FROM prenotazioni AS pr JOIN posti_auto AS po ON pr.id_posto_auto = po.id JOIN parcheggi AS pa ON po.id_parcheggio = pa.id "
										+ "WHERE pr.nome_cliente = '%s'", nome);
		}
		
		Statement stmt = null;
		// inizializzo una variabile per controllare che ci sia almeno una prenotazione
		boolean nessuna_prenotazione = true;
		try {			
			stmt = this.con.createStatement();		
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				// se entro nel loop ce n'è almeno una
				nessuna_prenotazione = false;
				int posizione = rs.getInt("posizione");
				int coperto = rs.getInt("coperto");
				Date data_inizio = rs.getDate("data_inizio");
				Date data_fine = rs.getDate("data_fine");
				float costo = rs.getFloat("costo");
				String nome_parcheggio = rs.getString("nome");
				
				String coperto_str;
				if(coperto == 0)
					coperto_str = "No";
				else
					coperto_str = "Sì";
				
				System.out.println(String.format("Parcheggio: %s, posizione: %d, coperto: %s, data inizio: %s, data fine: %s, costo: %.2f euro", nome_parcheggio, posizione, coperto_str, data_inizio.toString(), data_fine.toString(), costo));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// nel caso non ci sia nessuna prenotazione mostro un messaggio
		if(nessuna_prenotazione) {
			System.out.println("Nessuna prenotazione presente");
		}
		
	}
	
	
}
