package comparison.chem;

import uk.ac.cam.ch.wwmm.oscar.Oscar;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ChemicalStructure;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.FormatType;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ResolvedNamedEntity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.min;

/**
 * Created by Kisa on 08.07.2017.
 */
public class Comparison {

    /** Свойство - Путь к файлу с патентом */
    private String PatentPath;

    /** Свойство - список порядковых номеров (начиная с нуля) патентов */
    private ArrayList<Integer> numbersOfPatents;

    /** Свойство - список с множествами похожих химических формул
     * HashSet - множество, содержащее все похожие химические формулы в виде String*/
    private List<HashSet<String>> chemicalsMap;



    /**
     * Конструктор класса Comparison
     */
    public Comparison() {
        chemicalsMap = new ArrayList<>();
        numbersOfPatents = new ArrayList<>();
    }

    /**
     * Метод возвращает список с похожими химическими формулами
     * @return список с похожими химическими формулами
     */
    public List<HashSet<String>> getSimilarChemicals() {
        return chemicalsMap;
    }

    /**
     * Метод проверяет наличие формулы в списке похожих
     * @param formula проверяемая формула
     * @return возвращает index множества, если формула содержится в списке;
     * возвращает null, если формула не была найдена в списках
     */
    private Integer isAdded(String formula) {
        int i = 0;
        for (HashSet<String> entry : chemicalsMap) {
            if (entry.contains(formula)) {
                return i;
            }
            i++;
        }
        return null;
    }

    /**
     * Метод поиска химических наименований в патенте
     * @param patent патент в виде строки
     * @return список с химическими наименованиями
     */
    private List<ResolvedNamedEntity> findChemicals(String patent) {

        Oscar oscar = new Oscar();
        return oscar.findAndResolveNamedEntities(patent);
    }

    /**
     * Метод получает список похожих формул для пары патентов
     * @param patent1 патент 1
     * @param patent2 патент 2
     */
    public void comparePatentsCouple(String patent1, String patent2) {

        // Списки с хим.формулами для двух патентов
        List<ResolvedNamedEntity> namedEntities1 = findChemicals(patent1);
        List<ResolvedNamedEntity> namedEntities2 = findChemicals(patent2);

        // Коэффициент сходства
        double coeff = 0;

        // Выполняем попарное сравнение формул
        for (ResolvedNamedEntity entity1 : namedEntities1) {

            // entity1 - одна из найденных химических формул в первом патенте
            // Узнаем хим.структуру формулы(она не должна быть null)
            ChemicalStructure inchi_structure_one = entity1.getFirstChemicalStructure(FormatType.STD_INCHI);
            boolean is_inchi_one = false;
            if (inchi_structure_one != null)
                is_inchi_one = true;

            // Выполняем поиск во втором наборе хим. формул
            for (ResolvedNamedEntity entity2 : namedEntities2) {

                // entity2 - одна из найденных химических формул во втором патенте
                // Узнаем хим.структуру (она также не должна быть null)
                ChemicalStructure inchi_structure_two = entity2.getFirstChemicalStructure(FormatType.STD_INCHI);

                // Выполняем сравнение двух химических формул
                if (is_inchi_one && inchi_structure_two != null) {
                    coeff = compareChemicalsCouple(inchi_structure_one, inchi_structure_two);
                }

                // Формируем список похожих формул
                String formula1 = entity1.getSurface();
                String formula2 = entity2.getSurface();

                HashSet<String> formulas_set = new HashSet<>();
                if (coeff > 0.7) {
                    if (isAdded(formula1) == null && isAdded(formula2) == null) {
                        // Добавление нового списка с текущими формулами
                        formulas_set.add(formula1);
                        formulas_set.add(formula2);
                        chemicalsMap.add(formulas_set);
                    }
                    else if (isAdded(formula1) == null && isAdded(formula2) != null) {
                        // Добавление первой формулы к списку, в котором содержится вторая формула
                        int index = isAdded(formula2);
                        chemicalsMap.get(index).add(formula1);
                    }
                    else if (isAdded(formula2) == null && isAdded(formula1) != null) {
                        // Добавление второй формулы к списку, в котором содержится первая формула
                        int index = isAdded(formula1);
                        chemicalsMap.get(index).add(formula2);
                    }
                    // В ином случае обе формулы уже содержатся в списке и с ними не нужно выполнять никаких манипуляций
                }
            }
        }
    }

    /**
     * Метод поиска патентов по формуле
     * @param formula формула для сравнения
     * @param patent патент в виде строки
     * @param num порядковый номер патента (см. класс Patents)
     */
    public void findFormulaInPatents(String formula, String patent, int num){
        // Распознать формулу с помощью Oscar4
        List<ResolvedNamedEntity> formula_oscar =  findChemicals(formula);
        List<ResolvedNamedEntity> namedEntities = findChemicals(patent);

        // Коэффициент сходства
        double coeff = 0;

        // Сравнить формулу с формулами патента
        for (ResolvedNamedEntity entity1 : formula_oscar) {
            // Узнаем хим.структуру формулы(она не должна быть null)
            ChemicalStructure inchi_structure_one = entity1.getFirstChemicalStructure(FormatType.STD_INCHI);
            boolean is_inchi_one = false;
            if (inchi_structure_one != null)
                is_inchi_one = true;

            for (ResolvedNamedEntity entity2 : namedEntities) {
                // Узнаем хим.структуру (она также не должна быть null)
                ChemicalStructure inchi_structure_two = entity2.getFirstChemicalStructure(FormatType.STD_INCHI);

                // Выполняем сравнение двух химических формул
                if (is_inchi_one && inchi_structure_two != null) {
                    coeff = compareChemicalsCouple(inchi_structure_one, inchi_structure_two);
                }

                if (coeff > 0.7) {
                    numbersOfPatents.add(num);
                    return;
                }
            }

        }
    }

    /**
     * Метод возвращает список с найдеными патентами по схожей формуле
     * @return ArrayList - список порядковых номеров патентов
     */
    public ArrayList<Integer> getNumbersOfPatents() {
        return numbersOfPatents;
    }

    /**
     * Метод сравнения двух химических формул
     * @param en1 первое химическое наименование в виде InChi
     * @param en2 второе химическое наименование в виде InChi
     * @return коэффициент сходства двух формул (соединений)
     */
    public double compareChemicalsCouple(ChemicalStructure en1, ChemicalStructure en2) {
        double coeff = 0;
        // 1. Сравнить брутто-формулы
        String brutto_one = en1.toString();
        String brutto_two = en2.toString();
        Pattern reg = Pattern.compile("/\\w+/{0,1}");

        try {
            // 1.1 Поиск брутто формулы в структуре от OSCAR4
            Matcher matcher = reg.matcher(brutto_one);
            matcher.find();
            brutto_one = brutto_one.substring(matcher.start(), matcher.end());

            matcher = reg.matcher(brutto_two);
            matcher.find();
            brutto_two = brutto_two.substring(matcher.start(), matcher.end());

            reg = Pattern.compile("\\w+");
            matcher = reg.matcher(brutto_one);
            matcher.find();
            brutto_one = brutto_one.substring(matcher.start(), matcher.end());

            matcher = reg.matcher(brutto_two);
            matcher.find();
            brutto_two = brutto_two.substring(matcher.start(), matcher.end());

            // 1.2 Поиск символов и чисел (количество атомов) в брутто формулах
            // Поиск чисел в формулах
            ArrayList<Integer> chem_digits_one = findDigits(brutto_one);
            ArrayList<Integer> chem_digits_two = findDigits(brutto_two);

            // Поиск строки хим.элементов в формулах
            String elements_one = findElements(brutto_one);
            String elements_two = findElements(brutto_two);

            // 1.3 Сравнение строк с хим.элементами
            // Если строки различны, т.е. состав соединений разный, то считать формулы не похожими
            // и коэффициент схожести равным нулю
            // Иначе продолжать сравнение
            if (!elements_one.equals(elements_two))
                return 0;

            // 1.4 Сравнение массивов с числами (количеством атомов)
            int digits_size_one = chem_digits_one.size();
            int digits_size_two = chem_digits_two.size();
            // дополнение массива чисел 1
            if (digits_size_one < digits_size_two) {
                int count = digits_size_two - digits_size_one;
                chem_digits_one = addNumbers(chem_digits_one, 0, count);
                digits_size_one = chem_digits_one.size();
            }
            // дополнение массива чисел 2
            if (digits_size_two < digits_size_one) {
                int count = digits_size_one - digits_size_two;
                chem_digits_two = addNumbers(chem_digits_two, 0, count);
                digits_size_two = chem_digits_two.size();
            }
            if (digits_size_one == 0 && digits_size_two == 0)
                return 1;

            double crossing = 0;
            for (int i = 0; i < min(digits_size_one, digits_size_two); i++) {
                if (chem_digits_one.get(i).equals(chem_digits_two.get(i)))
                    crossing += 1;
                else {
                    double x = Math.abs(chem_digits_one.get(i)-chem_digits_two.get(i));
                    crossing += 1 / (0.2*x + 1);
                }
            }
            // Коэфф. Серенсена
            coeff = 2*crossing/(digits_size_one + digits_size_two);
        }
        catch (IllegalStateException e) {
            System.out.println(brutto_one + " " + brutto_two);
            e.printStackTrace();
            return 0;
        }

        return coeff;
    }

    /**
     * Метод поиска чисел в брутто формуле
     * @param formula строковое представление брутто-формулы
     * @return список с числами
     */
    private ArrayList<Integer> findDigits(String formula) {
        Pattern reg = Pattern.compile("\\d+");
        Matcher matcher = reg.matcher(formula);
        ArrayList<Integer> chem_digits = new ArrayList<Integer>();
        while(matcher.find()) {
            chem_digits.add(Integer.parseInt(formula.substring(matcher.start(), matcher.end())));
        }
        return chem_digits;
    }

    /**
     * Метод поиска строки химических элементов в формуле
     * @param formula строковое представление брутто-формулы
     * @return строка, состоящая из химических элементов, присутствующих в формуле
     */
    private String findElements(String formula) {
        Pattern reg = Pattern.compile("[A-Za-z]+");
        Matcher matcher = reg.matcher(formula);
        String elements = "";
        while (matcher.find()) {
            elements += formula.substring(matcher.start(), matcher.end());
        }

        return elements;
    }

    /**
     * Метод добавляет в список число указанное количество раз
     * @param digits список чисел
     * @param number число для добавления
     * @param count сколько раз добавить число
     * @return измененный список чисел
     */
    private ArrayList<Integer> addNumbers(ArrayList<Integer> digits, int number, int count) {
        for (int i = 0; i < count; i++) {
            digits.add(number);
        }
        return digits;
    }
}
