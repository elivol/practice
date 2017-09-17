package comparison.patents;

import comparison.chem.Comparison;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Kisa on 20.08.2017.
 */
public class Patents {

    /** Свойство - путь к файлу с патентами */
    private String patentPath;

    /** Свойство - карта с номерами патентов и патентами */
    private HashMap<Integer, String> patentsMap;

    /** Свойство - список для сравнения патентов */
    private ArrayList<int[]> listOfKeys;

    /**
     * Конструктор. Формирует карту с номерами патентов и патентами
     * @param pt путь к файлу с патентами
     */
    public Patents(String pt) {
        patentPath = pt;
        int counter = 0;

        try {
            // Открыть файл с текстом
            List<String> patentLines = Files.readAllLines(Paths.get(patentPath), StandardCharsets.UTF_8);
            patentsMap = new HashMap<>();
            listOfKeys = new ArrayList<>();

            for (String patent : patentLines) {
                patentsMap.put(counter, patent);
                counter++;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Метод - пример для анализа патентов. Возвращает схожие хим.формулы
     */
    public List<HashSet<String>> analysisPatent() {

        Comparison cn = new Comparison();

        // Получаем набор элементов
        Set<Map.Entry<Integer, String>> set = patentsMap.entrySet();

        for (Map.Entry<Integer, String> entry1 : set) {

            for (Map.Entry<Integer, String> entry2 : set) {
                int key1 = entry1.getKey();
                int key2 = entry2.getKey();

                String patent1 = entry1.getValue();
                String patent2 = entry2.getValue();

                if (!patent1.equals(patent2) && !isCompared(key1, key2)) {
                    cn.comparePatentsCouple(patent1, patent2);
                    listOfKeys.add(new int[] {key1, key2});
                }
            }
        }

        return cn.getSimilarChemicals();
    }

    /**
     * Метод, проверяющий были ли сравнены патенты, соответствующие ключам key1 и key2
     * @param key1 ключ, соответствующий первому патенту
     * @param key2 ключ, соответствующий второму патенту
     * @return возвращает True, если патенты уже были сравнены, False - не были
     */
    private boolean isCompared(int key1, int key2) {

        for (int[] list_iterator : listOfKeys) {
            if (list_iterator[0] == key1 && list_iterator[1] == key2
                    || list_iterator[0] == key2 && list_iterator[1] == key1)
                return true;
        }
        return false;
    }

    /**
     * Метод для получения нужного ключа из отображения patentsMap
     * @param entity значение, ключ которого нужно найти
     * @return ключ, соответствующий значению entity,
     * если ключ не найден возвращается -1 (в patentsMap нет ключа "-1")
     */
    private int getKey(String entity) {
        for (Map.Entry<Integer, String> entry : patentsMap.entrySet()) {
            if (entity.equals(entry.getValue()))
                return entry.getKey();
        }
        return -1;
    }

    /**
     * Возвращает карту с номерами патентов и патентами (для поиска патентов по схожей формуле)
     * @return карта с номерами патентов и патентами
     */
    public HashMap<Integer, String> getPatentsMap() {
        return patentsMap;
    }
}
