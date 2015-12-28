package eval;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class TRECFileFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		String lowercaseName = name.toLowerCase();
		if (lowercaseName.endsWith(".trec")) {
			return true;
		} else {
			return false;
		}
	}
}

public class Eval {

	/**
	 * Main-Methode zum Berechnen der Evaluationskennzahl.
	 * 
	 * @param args
	 *            nicht noetig.
	 */
	public static void main(String[] args) {

		// key: Anfrage; value: Menge der relevanten DokumentIDs
		Map<String, Set<String>> groundtruth = readGroundtruth("data/cacm-new-63.qrel");

		String[] filenames = new File("./logs").list(new TRECFileFilter());
		for (String filename : filenames) {
			double map = evaluateMAP("./logs/" + filename, groundtruth);
			System.out.println(filename + "\t MAP=" + Math.round(map * 1000.0) / 1000.0);
		}
	}

	/*
	 * Berechnet MAP.
	 */
	protected static double evaluateMAP(String filename, Map<String, Set<String>> groundtruth) {

		// TODO Hier bitte implementieren und korrekten Wert zurueckgeben.
		// TODO 8.2 wird mit filename eingelesen
		List<String> lines = null;
		try {
			lines = Files.readAllLines(new File(filename).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<Double> averagePrecision = new LinkedList<>();
		List<Double> recall = new LinkedList<>();
		String lastQueryId = "1";
		String queryId = "-1";
		String docId = "-1";
		String rank = "-1";
		for (String line : lines) {
			String[] parts = line.split(" ");
			if (parts.length != 6)
				throw new RuntimeException("Fehler" + parts.length);

			queryId = parts[0];
			docId = parts[2];
			rank = parts[3];
			Set<String> truth = groundtruth.get(lastQueryId);

			if (truth != null) {

				// Prüfe, ob bereits alle nötigen Dokumente gefunden sind und
				// die
				// Query noch die selbe ist
				if (lastQueryId.equals(queryId)) {
					if (truth.contains(docId)) {
						recall.add(Double.parseDouble(rank));
						//System.out.println("Found @: "+rank);

					}
				} else {
					// AP berechnen
					double ap = 0D;
					int i = 0;
					if (recall.size() > 0){
					for (double pos : recall) {
						ap += (1+i)/pos;
						i++;
					}
					ap = ap / recall.size();
					System.out.println("AP "+ap);
					averagePrecision.add(ap);

					// liste leeren
					recall.clear();
					}
				}
			}

			lastQueryId = queryId;
		}
		double map = 0D;
		for (double ap : averagePrecision) {
			map += ap;
		}
		map = map / averagePrecision.size();
		return map;
	}

	/*
	 * Liefert die Relevanzurteile: key: Anfrage ID; value: Menge mit relevanten
	 * Dokument IDs.
	 */
	private static Map<String, Set<String>> readGroundtruth(String filename) {
		Map<String, Set<String>> groundtruth = new HashMap<String, Set<String>>();
		String oldQueryId = "1";
		Set<String> relIdsPerQuery = new HashSet<String>();

		List<String> lines = null;
		try {
			lines = Files.readAllLines(new File(filename).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : lines) {
			String[] parts = line.split(" ");
			if (parts.length != 4)
				throw new RuntimeException("Fehler" + parts.length);

			String queryId = parts[0];
			String docId = parts[2];

			if (!queryId.equals(oldQueryId)) {
				groundtruth.put(oldQueryId, relIdsPerQuery);
				relIdsPerQuery = new HashSet<String>();
				oldQueryId = queryId;
			}
			relIdsPerQuery.add(docId);
		}
		groundtruth.put(oldQueryId, relIdsPerQuery);
		return groundtruth;
	}

}
