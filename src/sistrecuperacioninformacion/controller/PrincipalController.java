/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistrecuperacioninformacion.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import sistrecuperacioninformacion.DocumentDetails;
import sistrecuperacioninformacion.FuzzyCMeans;
import sistrecuperacioninformacion.Kmeans;
import sistrecuperacioninformacion.Linkage;
import sistrecuperacioninformacion.Main;
import sistrecuperacioninformacion.TikaLuceneProcessing;

/**
 *
 * @author Ale
 */
public class PrincipalController implements Initializable {

    @FXML
    private Pane paneContenedor;
    @FXML
    private Pane paneBarraTitulo;
    @FXML
    private TextArea JFXTextAreaGroups;
    @FXML
    private TextArea JFXTextAreaDocuments;
    @FXML
    private Pane JFXPaneContenedorCarga;

    //Mis atributoss
    public static Pane paneGlobal;
    ArrayList<DocumentDetails> documents; // Documentos cargados

    //make dragable (permitir que la ventana se arraste)
    private double xOffSet = 0;
    private double yOffSet = 0;
    @FXML
    private Pane JFXPaneContenedorCargaGrupos;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paneGlobal = paneContenedor;
        makeStageDragable();
    }

    /**
     * Metodo que permite al Stage ser transparente y que pueda moverse cuando
     * su estilo es UNDECORATED.
     */
    private void makeStageDragable() {
        //Para el paneContenedor global y para el paneBarraTitulo se le establece la propiedad
        //PaneGlobal
        paneGlobal.setOnMousePressed((event) -> {
            xOffSet = event.getSceneX();
            yOffSet = event.getSceneY();
        });
        paneGlobal.setOnMouseDragged((event) -> {
            Main.stage.setX(event.getScreenX() - xOffSet);
            Main.stage.setY(event.getScreenY() - yOffSet);
            Main.stage.setOpacity(0.8f);
        });
        paneGlobal.setOnDragDone((event) -> {
            Main.stage.setOpacity(1.0f);
        });
        paneGlobal.setOnMouseReleased((event) -> {
            Main.stage.setOpacity(1.0f);
        });

        //PaneBarraTitulo
        paneBarraTitulo.setOnMousePressed((event) -> {
            xOffSet = event.getSceneX();
            yOffSet = event.getSceneY();
        });
        paneBarraTitulo.setOnMouseDragged((event) -> {
            Main.stage.setX(event.getScreenX() - xOffSet);
            Main.stage.setY(event.getScreenY() - yOffSet);
            Main.stage.setOpacity(0.8f);
        });
        paneBarraTitulo.setOnDragDone((event) -> {
            Main.stage.setOpacity(1.0f);
        });
        paneBarraTitulo.setOnMouseReleased((event) -> {
            Main.stage.setOpacity(1.0f);
        });
    }

    @FXML
    private void cerrar(MouseEvent event) {
        System.exit(0);

    }

    @FXML
    private void minimizar(MouseEvent event) {
        /*Codigo para minimizar*/
        Stage stage = (Stage) paneGlobal.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    // Cargar los DocumentDetails
    private void cargar(MouseEvent event) {
        JFXTextAreaDocuments.clear();
        JFXTextAreaGroups.clear();
        documents = null;
        try {
            // Crear un nuevo DirectoryChooser
            DirectoryChooser directoryChooser = new DirectoryChooser();
            // Configurar el título del cuadro de diálogo
            directoryChooser.setTitle("Seleccionar carpeta");
            // Mostrar el cuadro de diálogo y esperar a que el usuario seleccione una carpeta
            File selectedDirectory = directoryChooser.showDialog(Main.stage);

            if (selectedDirectory != null) {
                //Cargar la vista Loading
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sistrecuperacioninformacion/view/Loading.fxml"));
                Node node = loader.load();
                JFXPaneContenedorCarga.getChildren().clear();
                JFXPaneContenedorCarga.getChildren().add(node);

                //Procesar el directorio seleccionado en un hilo aparte
                Thread thread = new Thread(() -> {
                    try {
                        // Cargar los documentos
                        documents = TikaLuceneProcessing.procesarDocs(new File(selectedDirectory.getAbsolutePath()));
                        // Una vez que el trabajo haya terminado, ejecutar ciertas acciones en el hilo de JavaFX
                        Platform.runLater(() -> {
                            //Quitar el loading
                            JFXPaneContenedorCarga.getChildren().clear();
                            JFXPaneContenedorCarga.getChildren().add(JFXTextAreaDocuments);
                            //Cargar los nombres de ficheros y ponerlos en el textarea del visual
                            for (int i = 0; i < documents.size(); i++) {
                                JFXTextAreaDocuments.appendText(documents.get(i).getNombre() + "\n");
                            }
                        });
                    } catch (IOException ex) {
                        Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                // Iniciar el hilo que carga los documentos
                thread.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void limpiar(MouseEvent event) {
        JFXTextAreaDocuments.clear();
        JFXTextAreaGroups.clear();
        this.documents = null;
    }

    @FXML
    private void kmeans(MouseEvent event) {
        if (this.documents != null) {
            try {
                //Cargar la vista Loading
                cargarLoadingGroups();
                //Procesar el directorio seleccionado en un hilo aparte
                Thread thread = new Thread(() -> {
                    // Limpiar el textarea de grupos
                    JFXTextAreaGroups.clear();
                    // Procesar kmeans
                    ArrayList<ArrayList<DocumentDetails>> clusters = Kmeans.kmeans(documents, 3);
                    // Una vez que el trabajo haya terminado, ejecutar ciertas acciones en el hilo de JavaFX
                    Platform.runLater(() -> {
                        //Quitar el loading
                        JFXPaneContenedorCargaGrupos.getChildren().clear();
                        JFXPaneContenedorCargaGrupos.getChildren().add(JFXTextAreaGroups);
                        //Cargar los nombres de ficheros y ponerlos en el textarea de grupos
                        // Imprimir los resultados
                        showKmeas(clusters);
                    });
                });
                // Iniciar el hilo que carga los documentos
                thread.start();
            } catch (IOException ex) {
                Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            mensaje();
        }
    }

    private void showKmeas(ArrayList<ArrayList<DocumentDetails>> clusters) {
        String solution = "K-Means:\n";
        int groups = 0;
        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).size() == 0) {
                continue;
            }
            solution += "Grupo " + (++groups) + ":\n";
            for (int j = 0; j < clusters.get(i).size(); j++) {
                solution += "Documento: " + clusters.get(i).get(j).getNombre() + "\n";
            }
            solution += "\n";
        }
        // Aunque K sea 3 puede que solo se impriman 2 o menos grupos pq pueden que algunos
        // se queden vacios cuando se actualicen los centroids y estos coincidan
        JFXTextAreaGroups.setText(solution);
    }

    @FXML
    private void linkage(MouseEvent event) {
        if (this.documents != null) {

            try {
                //Cargar la vista Loading
                cargarLoadingGroups();
                //Procesar el directorio seleccionado en un hilo aparte
                Thread thread = new Thread(() -> {
                    // Limpiar el textarea de grupos
                    JFXTextAreaGroups.clear();
                    // Procesar linkage
                    Linkage.historicalGroups = new ArrayList<>(); // reiniciar la lista histórica
                    JFXTextAreaGroups.clear();
                    double[][] distanceMatrix = Linkage.calculateDistanceMatrix(documents);
                    // Realizar el clustering jerárquico aglomerativo utilizando Linkage
                    Linkage.performLinkageClustering(documents, distanceMatrix);

                    // Una vez que el trabajo haya terminado, ejecutar ciertas acciones en el hilo de JavaFX
                    Platform.runLater(() -> {
                        //Quitar el loading
                        JFXPaneContenedorCargaGrupos.getChildren().clear();
                        JFXPaneContenedorCargaGrupos.getChildren().add(JFXTextAreaGroups);
                        //Cargar los nombres de ficheros y ponerlos en el textarea de grupos
                        // Imprimir los resultados
                        showLinkage();
                    });
                });
                // Iniciar el hilo que carga los documentos
                thread.start();
            } catch (IOException ex) {
                Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            mensaje();
        }
    }

    private void showLinkage() {
        // Imprimir los resultados del clustering (Clustering Jerárquico Aglomerativo)
        // A continuación se imprimen los distintos grupos obtenidos en cada momento
        String solution = "Linkage: \n";
        for (int i = 0; i < Linkage.historicalGroups.size(); i++) {
            solution += "Fase " + (i + 1) + " :\n";
            for (int j = 0; j < Linkage.historicalGroups.get(i).size(); j++) {
                solution += "Grupo " + (j + 1) + " :\n";
                for (int k = 0; k < Linkage.historicalGroups.get(i).get(j).getIndices().size(); k++) {
                    //Obtener el indice del documento del listado
                    int index = Linkage.historicalGroups.get(i).get(j).getIndices().get(k);
                    //Obtener el documento
                    DocumentDetails doc = documents.get(index);
                    solution += "Documento: " + doc.getNombre() + "\n";
                }
                solution += "\n";
            }
            solution += "\n";
        }
        JFXTextAreaGroups.setText(solution);
    }

    @FXML
    private void fuzzy(MouseEvent event) {
        if (this.documents != null) {
            try {
                //Cargar la vista Loading
                cargarLoadingGroups();
                //Procesar el directorio seleccionado en un hilo aparte
                Thread thread = new Thread(() -> {
                    // Procesar fuzzy
                    JFXTextAreaGroups.clear();
                    double[][] dataMatrix = FuzzyCMeans.tfIdfTransform(documents);//FuzzyCMeans.getDataMatrix(documents);
                    // Ejecutar Fuzzy C-Means
                    double[][] membershipMatrix = FuzzyCMeans.fuzzyCMeansClustering(dataMatrix, FuzzyCMeans.NUM_CLUSTERS, FuzzyCMeans.FUZZINESS, FuzzyCMeans.EPSILON, FuzzyCMeans.MAX_ITERATIONS);
                    // Cargar los cluster
                    ArrayList<ArrayList<DocumentDetails>> cluster = FuzzyCMeans.getClusters(membershipMatrix, documents);

                    // Una vez que el trabajo haya terminado, ejecutar ciertas acciones en el hilo de JavaFX
                    Platform.runLater(() -> {
                        //Quitar el loading
                        JFXPaneContenedorCargaGrupos.getChildren().clear();
                        JFXPaneContenedorCargaGrupos.getChildren().add(JFXTextAreaGroups);
                        //Cargar los nombres de ficheros y ponerlos en el textarea de grupos
                        // Imprimir los resultados
                        showfuzzy(cluster);
                    });
                });
                // Iniciar el hilo que carga los documentos
                thread.start();
            } catch (IOException ex) {
                Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            mensaje();
        }
    }

    private void showfuzzy(ArrayList<ArrayList<DocumentDetails>> cluster) {
        String solution = "Fuzzy C-means:\n\n";
        for (int i = 0; i < cluster.size(); i++) {
            solution += "Grupo " + (i + 1) + " :\n";
            for (int j = 0; j < cluster.get(i).size(); j++) {
                solution += "Documento " + (j + 1) + " :\n\t";
                solution += "Nombre: " + cluster.get(i).get(j).getNombre() + "\n\t";
                solution += "Grado de pertenecia al grupo: " + cluster.get(i).get(j).getSignificacion();
                solution += "\n";
            }
            solution += "\n";
        }
        JFXTextAreaGroups.setText(solution);
    }

    private void cargarLoadingGroups() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sistrecuperacioninformacion/view/LoadingGroups.fxml"));
        Node node = loader.load();
        JFXPaneContenedorCargaGrupos.getChildren().clear();
        JFXPaneContenedorCargaGrupos.getChildren().add(node);
    }

    private void mensaje() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setTitle("Error");
        alert.setContentText("Debe cargar los documentos");
        alert.showAndWait();
    }
}
