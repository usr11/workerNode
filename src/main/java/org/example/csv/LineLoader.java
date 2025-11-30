package org.example.csv;

import org.example.model.Line;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LineLoader {

    public static List<Line> load(String csv) throws Exception {
        List<String[]> rows = CsvUtils.readCsv(csv);
        List<Line> list = new ArrayList<>();

        for (String[] r : rows) {
            Line l = new Line(
                    Integer.parseInt(r[0]),
                    Integer.parseInt(r[1]),
                    r[2],
                    r[3],
                    LocalDateTime.parse(r[5].replace(" ", "T"))
            );
            list.add(l);
        }
        return list;
    }
}
