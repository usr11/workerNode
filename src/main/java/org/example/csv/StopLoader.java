package org.example.csv;

import org.example.model.Stop;

import java.util.ArrayList;
import java.util.List;

public class StopLoader {

    public static List<Stop> load(String csv) throws Exception {
        List<String[]> rows = CsvUtils.readCsv(csv);
        List<Stop> list = new ArrayList<>();

        for (String[] r : rows) {
            Stop s = new Stop(
                    Integer.parseInt(r[0]),
                    Integer.parseInt(r[1]),
                    r[2],
                    r[3],
                    Integer.parseInt(r[4]),
                    Integer.parseInt(r[5]),
                    Double.parseDouble(r[6]),
                    Double.parseDouble(r[7])
            );
            list.add(s);
        }

        return list;
    }
}