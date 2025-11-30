package org.example.csv;

import org.example.model.LineStop;

import java.util.ArrayList;
import java.util.List;

public class LineStopLoader {

    public static List<LineStop> load(String csv) throws Exception {
        List<String[]> rows = CsvUtils.readCsv(csv);
        List<LineStop> list = new ArrayList<>();

        for (String[] r : rows) {
            LineStop ls = new LineStop(
                    Integer.parseInt(r[0]), // LINESTOPID
                    Integer.parseInt(r[1]), // STOPSEQUENCE
                    Integer.parseInt(r[2]), // ORIENTATION
                    Integer.parseInt(r[3]), // LINEID
                    Integer.parseInt(r[4]), // STOPID
                    Integer.parseInt(r[5]), // PLANVERSIONID
                    Integer.parseInt(r[6]), // LINEVARIANT
                    Integer.parseInt(r[8])  // LINEVARIANTTYPE
            );
            list.add(ls);
        }
        return list;
    }
}