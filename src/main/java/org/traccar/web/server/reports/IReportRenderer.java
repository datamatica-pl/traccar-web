/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.server.reports;

import java.io.IOException;
import pl.datamatica.traccar.model.Report;

public interface IReportRenderer {
    
    static class TableStyle {
        private boolean hover;
        private boolean condensed;
        private Integer height;

        TableStyle hover() {
            this.hover = true;
            return this;
        }

        TableStyle condensed() {
            this.condensed = true;
            return this;
        }
        
        TableStyle height(int h) {
            this.height = h;
            return this;
        }

        @Override
        public String toString() {
            return "class=\"table" +
                    (hover ? " table-hover" : "") +
                    (condensed ? " table-condensed" : "") + "\" "
                    + "style=\"overflow:auto;"+
                    (height != null ? "display:block;height:"+height+"px;" : "")
                    +"\"";
        }
    }
    
    static class CellStyle {
        int colspan;
        int rowspan;
        boolean hidden;
        String id;

        CellStyle id(String id) {
            this.id = id;
            return this;
        }
        
        CellStyle colspan(int colspan) {
            this.colspan = colspan;
            return this;
        }

        CellStyle rowspan(int rowspan) {
            this.rowspan = rowspan;
            return this;
        }
        
        CellStyle hidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        @Override
        public String toString() {
            return (colspan == 0 ? "" : ("colspan=\"" + colspan + "\"")) +
                   (rowspan == 0 ? "" : (" rowspan=\"" + rowspan + "\"")) +
                   (id != null ? "id=\""+id+"\"" : "")+
                   (hidden ? "style=\"display:none;\"" : "");
        }
    }

    void bold(String text);

    void end(Report report) throws IOException;

    String getFilename(Report report);

    void h1(String text);

    void h2(String text);

    void h3(String text);
    
    void html(String html);

    void link(String url, String target, String text);

    void panelBodyEnd();

    void panelBodyStart();

    void panelEnd();

    void panelHeadingEnd();

    void panelHeadingStart();

    void panelStart();

    void paragraphEnd();

    void paragraphStart();

    void start(Report report) throws IOException;

    void tableBodyEnd();

    void tableBodyStart();

    void tableCellEnd();

    void tableCellStart(CellStyle style);

    void tableEnd();

    void tableHeadCellEnd();

    void tableHeadCellStart(CellStyle style);

    void tableHeadEnd();

    void tableHeadStart();

    void tableRowEnd();

    void tableRowStart();

    void tableStart(String id, TableStyle style);

    void text(String text);
    
}
