package com.zyb.reader.bean;

import java.util.List;

/**
 */
public class TRPage {
    private long begin;
    private long end;
    private List<String> lines;

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public List<String> getLines() {
        return lines;
    }

    public String getLineToString(){
        StringBuilder text = new StringBuilder();
        if (lines != null){
            for (String line : lines){
                text.append(line);
            }
        }
        return text.toString();
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}
