package com.medisons.dbm;

import java.util.List;

public class SignalDataRowList {
    private final String name;
    private final List<SignalDataRow> rows;

    public SignalDataRowList(String name, List<SignalDataRow> rows) {
        this.name = name;
        this.rows = rows;
    }

    public String getName() {
        return name;
    }

    public List<SignalDataRow> getRows() {
        return rows;
    }
}
