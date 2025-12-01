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
                    Integer.parseInt(r[0]),
                    Integer.parseInt(r[1]),
                    Integer.parseInt(r[2]),
                    Integer.parseInt(r[3]),
                    Integer.parseInt(r[4]),
                    Integer.parseInt(r[5]),
                    Integer.parseInt(r[6]),
                    Integer.parseInt(r[8])
            );
            list.add(ls);
        }
        return list;
    }
}