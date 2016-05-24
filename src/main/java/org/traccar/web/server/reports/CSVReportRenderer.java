/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.server.reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.traccar.web.shared.model.Position;
import org.traccar.web.shared.model.Report;
import org.traccar.web.shared.model.UserSettings;

/**
 *
 * @author admin
 */
public class CSVReportRenderer implements IReportRenderer{
    private class CellLock{
        public int startCol;
        public int rowSpan;
        public int colSpan;
        
        public CellLock(int startCol, int rowSpan, int colSpan) {
            rowSpan = Math.max(1, rowSpan);
            colSpan = Math.max(1, colSpan);
            
            this.startCol = startCol;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
        }

        @Override
        public String toString() {
            return "CellLock{" + "startCol=" + startCol + ", rowSpan=" + rowSpan + ", colSpan=" + colSpan + '}';
        }
        
        public boolean containsCol(int colNo) {
            return rowSpan > 0 && colNo >= startCol && colNo < startCol + colSpan;
        }
    }
    
    private ArrayList<CellLock> cellLocks;
    private CellLock currentCell;
    private int colNo;
    private int rowNo;
    private final HttpServletResponse response;
    private final PrintWriter writer;
    private static final char SEPARATOR = ';';

    public CSVReportRenderer(HttpServletResponse response) throws IOException {
        for(int i=0;i<3;++i)
            System.out.println();
        
        this.response = response;
        response.setCharacterEncoding("UTF-8");
        this.writer = response.getWriter();
        this.cellLocks = new ArrayList<>();
    }
    
    @Override
    public String getFilename(Report report) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        return report.getName() +
                "_" +
                dateFormat.format(report.getFromDate()) +
                "_" +
                dateFormat.format(report.getToDate()) +
                ".csv";
    }
    
    @Override
    public void start(Report report) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        writer.write(new String(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF}, "UTF-8"));
        if (!report.isPreview()) {
            response.setHeader("Content-Disposition", "attachment; filename=" + getFilename(report));
        }
    }
    
    @Override
    public void panelHeadingEnd() {
        writer.println();
    }
    
    @Override
    public void tableHeadStart() {
        colNo = 0;
        cellLocks.clear();
        System.out.println("headStart");
    }
    
    @Override
    public void tableHeadCellStart(CellStyle style) {
        tableCellStart(style);
    }
    
    @Override
    public void tableHeadCellEnd() {
        tableCellEnd();
    }
    
    @Override
    public void tableHeadEnd() {
        writer.println();
        System.out.println("headEnd");
    }
    
    @Override
    public void tableRowStart() {
        System.out.println("Row start");
        
        ArrayList<CellLock> toRemove = new ArrayList<>();
        for(CellLock lock:cellLocks)
            if(lock.rowSpan == 0)
                toRemove.add(lock);
        cellLocks.removeAll(toRemove);
        
        colNo = 0;
        for(int i=0;i<cellLocks.size();++i)
            while(cellLocks.get(i).containsCol(colNo)) {
                colNo++;
                writer.print(SEPARATOR);
            }
            
    }
    
    @Override
    public void tableCellStart(CellStyle style) {
        if(style == null)
            currentCell = new CellLock(colNo, 1, 1);
        else
            currentCell = new CellLock(colNo, style.rowspan, style.colspan);
        System.out.println("Cell start, width="+currentCell.colSpan);
        cellLocks.add(currentCell);
    }
    
    @Override
    public void tableCellEnd() {
        System.out.println("Cell end, width="+currentCell.colSpan);
        for(int i=0;i<currentCell.colSpan;++i)
            writer.print(SEPARATOR);
        
        colNo += currentCell.colSpan;
        for(int i=0;i<cellLocks.size();++i)
            while(cellLocks.get(i).containsCol(colNo)) {
                writer.print(SEPARATOR);
                colNo++;
            }
    }
    
    @Override
    public void tableRowEnd() {
        writer.println();
        for(int i=0;i<cellLocks.size();++i) {
            cellLocks.get(i).rowSpan--;
            System.out.print(cellLocks.get(i)+" ");
        }
        System.out.println();
        System.out.println();
    }
    
    @Override
    public void tableBodyEnd() {
        System.out.println("body end.....");
        writer.println();
    }
    
    @Override
    public void tableEnd() {
    }
    
    @Override
    public void paragraphEnd() {
        writer.println();
    }
    
    @Override
    public void panelEnd() {
        writer.println();
        writer.println();
    }
    
    @Override
    public void end(Report report) throws IOException {
        System.out.println("Document ended");
        System.out.println("ContentType: "+response.getContentType());
    }
    
    @Override
    public void text(String text) {
        writer.print(text);
    }
    
    @Override
    public void bold(String text) {
        text(text);
    }
    
    
    
    
    
    //-----not visible in csv

    @Override
    public void h1(String text) {
    }

    @Override
    public void h2(String text) {
    }

    @Override
    public void h3(String text) {
    }

    @Override
    public void link(String url, String target, String text) {
    }

    @Override
    public void mapWithRoute(List<Position> positions, UserSettings.MapType mapType, int zoomLevel, String width, String height) {
    }

    @Override
    public void panelBodyEnd() {
    }

    @Override
    public void panelBodyStart() {
    }

    @Override
    public void panelHeadingStart() {
    }

    @Override
    public void panelStart() {
    }

    @Override
    public void paragraphStart() {
    }

    @Override
    public void tableBodyStart() {
    }

    @Override
    public void tableStart(TableStyle style) {
    }
}
