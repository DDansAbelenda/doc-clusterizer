package sistrecuperacioninformacion;

import java.util.ArrayList;
import ucar.ma2.Array;

/**
 *
 * @author Daniel Dans
 */
public class Linkage {

    public static ArrayList<ArrayList<Cluster>> historicalGroups = new ArrayList<>();

    /**
     * Crea la matriz de distancias Para cada documento analiza todos los
     * documentos y calcula la distancia con respecto a estos.
     *
     * @param documents
     * @return
     */
    public static double[][] calculateDistanceMatrix(ArrayList<DocumentDetails> documents) {
        int numDocuments = documents.size();
        double[][] distanceMatrix = new double[numDocuments][numDocuments];

        for (int i = 0; i < numDocuments; i++) {
            for (int j = 0; j < numDocuments; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0.0;
                } else {
                    double distance = calculateDocumentDistance(documents.get(i), documents.get(j));
                    distanceMatrix[i][j] = distance;
                }
            }
        }

        return distanceMatrix;
    }

    /**
     * Calcula la distancia de Jaccard entre dos documentos. Esta distancia se
     * calcula utilizando los listados de tokens. Primero se busca el nivel de
     * coincidencia entre los tokens de cada documento, esta sería la
     * intersección (I), luego la suma de ambos listados de tokens sería la
     * unión (U) y la formula de Jaccard es: d(doc, doc2) = 1 - [((doc1 U doc2)
     * - (doc1 I doc2))/(doc1 U doc2)]
     *
     * @param doc1
     * @param doc2
     * @return
     */
    private static double calculateDocumentDistance(DocumentDetails doc1, DocumentDetails doc2) {

        // compara la cantidad de tokens compartidos
        ArrayList<String> tokens1 = doc1.getToken();
        ArrayList<String> tokens2 = doc2.getToken();

        int intersectionSize = 0;
        for (String token : tokens1) {
            if (tokens2.contains(token)) {
                intersectionSize++;
            }
        }

        int unionSize = tokens1.size() + tokens2.size() - intersectionSize;
        if (unionSize == 0) {
            return 0;
        } else {
            return 1.0 - (double) intersectionSize / unionSize;
        }
    }

    /**
     * Retorna una lista de los clusters creados.
     *
     * @param documents
     * @param distanceMatrix
     * @return
     */
    public static ArrayList<Cluster> performLinkageClustering(ArrayList<DocumentDetails> documents, double[][] distanceMatrix) {
        int numDocuments = documents.size();
        // Inicializar una lista de clusters, donde cada documento se encuentra en un cluster individual
        ArrayList<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < numDocuments; i++) {
            clusters.add(new Cluster(i));
        }
        // Inicializar clusters histórico, primer grupo
        actualizarHistoricalGroups(clusters);

        /*  Realizar el proceso de clustering aglomerativo. Este proceso no para hasta que todos los 
         *  documentos esten dentro de un mismo grupo 
         */
        while (clusters.size() > 1) {
            double minDistance = Double.MAX_VALUE;
            int minI = -1;
            int minJ = -1;

            // Encontrar los dos clusters más cercanos
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double distance = calculateClusterDistance(clusters.get(i), clusters.get(j), distanceMatrix);
                    if (distance < minDistance) {
                        minDistance = distance;
                        minI = i;
                        minJ = j;
                    }
                }
            }

            // Fusionar los dos clusters más cercanos en un nuevo cluster
            Cluster mergedCluster = mergeClusters(clusters.get(minI), clusters.get(minJ));
            clusters.remove(minJ);
            clusters.remove(minI);
            clusters.add(mergedCluster);
            // Agregar los clusters al historico
            actualizarHistoricalGroups(clusters);
        }

        return clusters;
    }

    /**
     * Devuelve la distancia entre dos grupos. Este algoritmo recibe dos grupos
     * y busca la menor distancia entre dos pares de documentos en el grupo,
     * para determinar esa distancia se auxilia de la matriz de distancias
     *
     * @param cluster1
     * @param cluster2
     * @param distanceMatrix
     * @return
     */
    private static double calculateClusterDistance(Cluster cluster1, Cluster cluster2, double[][] distanceMatrix) {
        // Implementa aquí la medida de distancia con ejemplo básico que utiliza el enlace simple:
        double minDistance = Double.MAX_VALUE;
        // Calcula la distancia entre cada par de indices que pertenecen a los distintos grupos
        for (int index1 : cluster1.getIndices()) {
            for (int index2 : cluster2.getIndices()) {
                double distance = distanceMatrix[index1][index2];
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
        }
        return minDistance;
    }

    /**
     * Recibe dos grupos y los une. A partir de dos grupos crea un tercer grupo
     * que es la union de sos dos y lo devuelve
     *
     * @param cluster1
     * @param cluster2
     * @return
     */
    private static Cluster mergeClusters(Cluster cluster1, Cluster cluster2) {
        // Fusionar los dos clusters en uno nuevo
        ArrayList<Integer> mergedIndices = new ArrayList<>();
        mergedIndices.addAll(cluster1.getIndices());
        mergedIndices.addAll(cluster2.getIndices());
        return new Cluster(mergedIndices);
    }

    private static void actualizarHistoricalGroups(ArrayList<Cluster> clusters) {
        ArrayList<Cluster> newCluster = new ArrayList<>();
        newCluster.addAll(clusters);
        historicalGroups.add(newCluster);
    }
}
