package sistrecuperacioninformacion;

import java.util.ArrayList;

/**
 *
 * @author Daniel Dans
 */
public class Cluster {

    /**
     * Esta clase contiene un listado de indices que hacen referencia a los
     * índices de los documentos que pertenecen al cluster. En un incio la lista
     * tiene un solo elemento pq cada elemento es un grupo en sí.
     */
    private ArrayList<Integer> indices;

    public Cluster(ArrayList<Integer> indices) {
        this.indices = indices;
    }

    public Cluster(int index) {
        this.indices = new ArrayList<>();
        this.indices.add(index);
    }

    public ArrayList<Integer> getIndices() {
        return indices;
    }
}
