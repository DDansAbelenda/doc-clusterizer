package sistrecuperacioninformacion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

/**
 *
 * @author Aida Rosa
 */
public class TikaLuceneProcessing {

    public static Tika tika = new Tika();

    /**
     * Este método carga los campos de un documento recibido por parámetros y
     * define tokens con dichos campos. Cada campo es filtrado con un conjunto
     * de stop words de español e inglés. Luego de filtrado los tokens se
     * colocan en una lista y se devuelven
     *
     * @param documento
     * @return
     * @throws IOException
     */
    private static ArrayList getTokens(Document documento) throws IOException {
        /**
         * Extraccion de tokens*
         */
        // Se crea un analizador standar de Lucene
        Analyzer analizador = new StandardAnalyzer();

        // Se crea una lista con los campos del documento
        ArrayList<Field> fields = (ArrayList) documento.getFields();

        // guarda los campos en una cadena en el formato "nombre1: valor1 nombre2: valor2 ..."
        String doc = "";
        for (int i = 0; i < fields.size(); i++) {
            doc += fields.get(i).name() + ": " + fields.get(i).stringValue() + " ";
        }

        //Convierte la cadena en un string reader
        StringReader sr = new StringReader(doc);

        //Se crea un flujo de tokens con el StringReader
        TokenStream cadena = analizador.tokenStream(null, sr);

        // Crear un lista para almacenar las stop words
        ArrayList<String> str_sw = new ArrayList<>();

        /**
         * Cargar las stop words*
         */
        // Cargar el fichero txt que contiene las stop words
        File f = new File("./stopwords_en.txt");

        // Define un BufferReader para procesar el fichero texto
        BufferedReader bf = new BufferedReader(new FileReader(f));
        String line;
        // Guarda el contenido de cada linea del texto (una stop word) en la lista
        while ((line = bf.readLine()) != null) {
            str_sw.add(line);
        }
        bf.close();
        // Realiza el procedimiento anterior pero para las stop words en inglés
        File fs = new File("./stopwords_es.txt");
        BufferedReader bfs = new BufferedReader(new FileReader(fs));
        String line2;
        while ((line2 = bfs.readLine()) != null) {
            str_sw.add(line2);
        }
        bfs.close();

        // Intentar hacerlo asi String sss[] = (String []) str_sw.toArray();***********************        
        String stwords[] = new String[str_sw.size()];
        for (int i = 0; i < str_sw.size(); i++) {
            stwords[i] = str_sw.get(i);
        }

        /**
         * Filtra las stop words de todos los campos*
         */
        cadena = new StopFilter(cadena, stwords);

        /**
         * Crear una lista de tokens y devolverla*
         */
        ArrayList tokenList = new ArrayList();

        while (true) {
            Token token = cadena.next();
            if (token == null) {
                break;
            }
            tokenList.add(token.term());
        }
        return tokenList;
    }

    /**
     * Crea un documento lucene con sus respectivos campos a partir de un
     * directorio recibido. En el documento lucene se definen los campos:
     * nombre, dirección o ruta, autor y contenido. - En el caso de nombre y
     * dirección los extrae directamente del fichero - Para el autor utiliza un
     * objeto Metadata - Para el contenido utiliza el método parseToString de
     * tika
     *
     * @param file
     * @return
     * @throws Exception
     */
    private static Document getDocument(File file) throws Exception {
        Document document = new Document();
        String read = tika.parseToString(file);
        document.add(new Field("nombre", file.getName(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field("direccion", file.getAbsolutePath(), Field.Store.YES, Field.Index.TOKENIZED));

        Metadata met = new Metadata();
        InputStream is = new FileInputStream(file);
        tika.parse(is, met);
        try {
            document.add(new Field("autor", met.get(Metadata.CREATOR), Field.Store.YES, Field.Index.TOKENIZED));
        } catch (NullPointerException ex) {
            document.add(new Field("autor", "desconocido", Field.Store.YES, Field.Index.TOKENIZED));
        }
        document.add(new Field("contenido", read, Field.Store.NO, Field.Index.TOKENIZED));
        return document;
    }

    /**
     * Este método carga una ruta absoluta de un direcotrio recibida por
     * parámetro y extrae los ficheros que se encuentren dentro del directorio y
     * subdirectorios. Este método procesa cada una de las carpetas en búsqueda
     * de ficheros para crear los documentos con sus respectivos campos, luego
     * se utilizan estos campos para extraer los tokens y finalmente se crea un
     * listado de DocumentDetails donde cada uno tiene el nombre del documento y
     * el listado de tokens.Este procesamiento se hace con ayuda de las
     * librerías Lucene y Tika. Devuelve un listado de DocumentDetails con
     * nombre y listado de tokens
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static ArrayList<DocumentDetails> procesarDocs(File file) throws FileNotFoundException, IOException {

        // Lista para almacenar los documentos
        ArrayList<String> files = new ArrayList();

        // Buscar documentos
        buscar_archivos(file, files);

        /**
         * Se crea una lista de DocumentDetails(objeto que guarda el nombre y la
         * lista de tokens asociada al documento)
         */
        ArrayList<DocumentDetails> docs = new ArrayList();

        /**
         * A partir de los ficheros buscados (files) se utilizan los métodos: -
         * getDocument - obtener un documento con los campos a partir de un file
         * - getTokens - obtener tokens filtrados a partir de un documento. Con
         * cada fichero se genera un documento con sus campos, luego se generan
         * los tokens a partir de ese documento y sus campos, y finalmente se
         * crean un DocumentDetails con el nombre del documento y su listado de
         * tokens
         */
        for (String path : files) {
            try {
                /**
                 * Se cargan los campos del archivo que se encuentra en path, y
                 * se guardan en un documento Lucene
                 */
                Document art1 = getDocument(new File(path));

                //Se cargan los tokens
                ArrayList tokenList = getTokens(art1);

                //Se crea el DocumentDetails con el nombre y los tokens del Document
                DocumentDetails d = new DocumentDetails(art1.get("nombre"), tokenList);
                //Se agrega a la lista de DocumentDetails
                docs.add(d);

            } catch (Exception ex) {
                Logger.getLogger(TikaLuceneProcessing.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return docs;
    }

    /**
     * Realiza una busqueda en profundidad de todos los documentos y los pone en
     * una lista
     *
     * @param file
     * @param files
     */
    private static void buscar_archivos(File file, ArrayList files) {//buscar el camino de cada fichero
        if (file.isFile()) {
            files.add(file.getAbsolutePath());
        } else {
            for (File ficheros : file.listFiles()) {
                buscar_archivos(ficheros, files);
            }
        }
    }

}
