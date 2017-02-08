package tv.bouyguestelecom.fr.bboxapilibrary.util;


import java.util.HashMap;

public class ListenerList<T> {

    /**
     * list of listener
     */
    private HashMap<String, T> listenerMap = new HashMap<String, T>();

    /**
     * random string generator for listener id
     */
    private RandomGen randomGen = new RandomGen(15);


    public ListenerList() {

    }

    public String add(T obj) {
        String generatedId = randomGen.nextString();
        listenerMap.put(generatedId, obj);
        return generatedId;
    }

    public void remove(String id) {
        if (listenerMap.containsKey(id))
            listenerMap.remove(id);
    }

    /**
     * clear listener
     */
    public void clear() {
        listenerMap.clear();
    }

    /**
     * get listener count
     *
     * @return
     */
    public int size() {
        return listenerMap.size();
    }

    /**
     * retrieve list of listeners
     *
     * @return
     */
    public HashMap<String, T> getMap() {
        return listenerMap;
    }
}
