/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.server.reports;

import org.apache.commons.io.IOUtils;
import pl.datamatica.traccar.model.Report;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class HtmlReportRenderer implements IReportRenderer {
    final HttpServletResponse response;
    final PrintWriter writer;

    public HtmlReportRenderer(HttpServletResponse response) throws IOException {
        this.response = response;
        response.setCharacterEncoding("UTF-8");
        this.writer = response.getWriter();
    }

    @Override
    public String getFilename(Report report) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        return report.getName() +
                "_" +
                dateFormat.format(report.getFromDate()) +
                "_" +
                dateFormat.format(report.getToDate()) +
                ".html";
    }

    @Override
    public void start(Report report) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        if (!report.isPreview()) {
            response.setHeader("Content-Disposition", "attachment; filename=" + getFilename(report));
        }

        line("<!DOCTYPE html>");
        line("<html>");
        line("<head>");
        line("<title>" + report.getName() + "</title>");
        line("<meta charset=\"utf-8\">");
        // include bootstrap CSS
        line("<style type=\"text/css\">");
        IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/traccar/web/server/reports/bootstrap.min.css"),
                writer, "UTF-8");
        line("</style>");
        // include OpenLayers 3 css and javascript if report intends to include map
        if (report.isIncludeMap() && report.getType().supportsMapDisplay()) {
            line("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/ol3/3.11.1/ol.min.js\" type=\"text/css\">");
            line("<script src=\"https://cdnjs.cloudflare.com/ajax/libs/ol3/3.11.1/ol.min.js\" type=\"text/javascript\"></script>");
        }

        line("</head>").line("<body>").line("<div class=\"container\">");
    }

    @Override
    public void end(Report report) throws IOException {
        line("</div>").line("</body>").line("</html>");
    }

    @Override
    public void h1(String text) {
        line("<h1>" + text + "</h1>");
    }

    @Override
    public void h2(String text) {
        line("<h2>" + text + "</h2>");
    }

    @Override
    public void h3(String text) {
        line("<h3>" + text + "</h3>");
    }

    @Override
    public void text(String text) {
        writer.write(text);
    }

    @Override
    public void bold(String text) {
        writer.write("<strong>");
        writer.write(text);
        writer.write("</strong>");
    }

    @Override
    public void panelStart() {
        line("<div class=\"panel panel-default\">");
    }

    @Override
    public void panelEnd() {
        line("</div>");
    }

    @Override
    public void panelHeadingStart() {
        line("<div class=\"panel-heading\">");
    }

    @Override
    public void panelHeadingEnd() {
        line("</div>");
    }

    @Override
    public void panelBodyStart() {
        line("<div class=\"panel-body\">");
    }

    @Override
    public void panelBodyEnd() {
        line("</div>");
    }

    @Override
    public void paragraphStart() {
        writer.write("<p>");
    }

    @Override
    public void paragraphEnd() {
        line("</p>");
    }
    
    @Override
    public void tableStart(String id, TableStyle style) {
        if (style == null) {
            line("<table id=\""+id+"\">");
        } else {
            line("<table id=\""+id+"\" "+style+">");
        }
    }

    @Override
    public void tableEnd() {
        line("</table>");
    }

    @Override
    public void tableHeadStart() {
        line("<thead>");
    }

    @Override
    public void tableHeadEnd() {
        line("</thead>");
    }

    @Override
    public void tableHeadCellStart(CellStyle style) {
        if (style == null) {
            line("<th>");
        } else {
            line("<th " + style + ">");
        }
    }

    @Override
    public void tableHeadCellEnd() {
        line("</th>");
    }

    @Override
    public void tableBodyStart() {
        line("<tbody>");
    }

    @Override
    public void tableBodyEnd() {
        line("</tbody>");
    }

    @Override
    public void tableRowStart() {
        line("<tr>");
    }

    @Override
    public void tableRowEnd() {
        line("</tr>");
    }

    @Override
    public void tableCellStart(CellStyle style) {
        if (style == null) {
            line("<td>");
        } else {
            line("<td " + style + ">");
        }
    }

    @Override
    public void tableCellEnd() {
        line("</td>");
    }

    @Override
    public void link(String url, String target, String text) {
        writer.write("<a href=\"");
        writer.write(url);
        writer.write("\"");
        if (target != null) {
            writer.write(" target=\"");
            writer.write(target);
            writer.write("\"");
        }
        writer.write(">");
        writer.write(text);
        writer.write("</a>");
    }

    int mapCount;

    @Override
    public void html(String html) {
        writer.println(html);
    }
    
    private HtmlReportRenderer line(String html) {
        writer.println(html);
        return this;
    }
}
