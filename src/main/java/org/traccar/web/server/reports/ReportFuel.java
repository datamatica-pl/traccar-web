/*
 * Copyright 2017 Datamatica (dev@datamatica.pl)
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import static org.traccar.web.server.reports.ReportGenerator.DEFAULT_TABLE_HEIGHT;
import org.traccar.web.server.utils.JsonXmlParser;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.Report;

public class ReportFuel extends ReportGenerator {
    
    @Override
    void generateImpl(Report report) throws IOException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            List<Position> positions = entityManager.createQuery("SELECT p FROM Position p" +
                    " WHERE p.device=:device AND p.time BETWEEN :from AND :to "
                    + "ORDER BY p.time", Position.class)
                    .setParameter("device", device)
                    .setParameter("from", report.getFromDate())
                    .setParameter("to", report.getToDate())
                    .getResultList();
            panelStart();

            // heading
            panelHeadingStart();
            text(device.getName());
            panelHeadingEnd();

            // body
            panelBodyStart();
            
            printData(positions);
            declareFunctions();
            drawTable(positions);
            drawPlot(new DataAccessor().id("io83").conversion("y*0.1")
                    .normalize(true),
                    "fuelConsumed");
            drawPlot(new DataAccessor().id("io84"),
                    "fuelLevel");

            panelBodyEnd();

            panelEnd();
        }
    }
    
    private void declareFunctions() {
        html("<script type=\"text/javascript\">");
        html("function range(arr, f) {\n"
           + "    return [d3.min(arr, f), d3.max(arr, f)];\n"
           + "}");
        html("function drawPlot(id, pts, title) {");
        html("var plotWidth = d3.select(\"#\"+id).node().getBoundingClientRect().width;");
        html("var x = d3.scaleTime()\n" +
             "               .domain(range(pts, function(d) {return d.x;}))\n" +
             "               .range([30, plotWidth-30]);\n" +
             "var bisector = d3.bisector(function(d) { return d.x; }).left;\n" +
             "var yRange = range(pts, function(d) { return d.y;});\n"+
             "yRange[1] = Math.max(yRange[1], yRange[0]+1);\n"+
             "var y = d3.scaleLinear()\n" +
             "          .domain(yRange)\n" +
             "          .range([200, 30]);\n" +
             "var line = d3.line()\n" +
             "             .x(function(d) { return x(d.x);})\n" +
             "             .y(function(d) { return y(d.y);})\n" +
             "             .curve(d3.curveStepAfter);\n" +
             "var svg = d3.select('#'+id);");
        
        html("svg.append('text')\n"+
             "   .attr(\"text-anchor\", \"middle\")\n" +
             "   .attr(\"x\", plotWidth/2)\n" + 
             "   .attr('y', 20)\n" +
             "   .text(title);");
        
        html("svg.append(\"g\")\n" +
             "   .append(\"path\")\n" +
             "   .attr(\"d\", line(pts))\n" +
             "   .attr(\"stroke\", \"blue\")\n" +
             "   .attr(\"stroke-width\", 2)\n" +
             "   .attr(\"fill\", \"none\");\n" +
             "svg.append(\"g\")\n" +
             "   .attr(\"transform\", \"translate(30, 0)\")\n" +
             "   .call(d3.axisLeft().scale(y));\n" +
             "svg.append(\"g\")\n" +
             "   .attr(\"transform\", \"translate(0, 200)\")\n" +
             "   .call(d3.axisBottom().scale(x));");
        
        html("var focus = svg.append(\"g\")\n" +
             "               .style(\"display\", \"none\");\n" +
             "focus.append(\"rect\")\n" +
             "     .attr(\"fill\", \"#FFFFFF\")\n" +
             "     .attr(\"opacity\", 0.7)\n" +
             "     .attr(\"stroke\", \"black\")\n" +
             "     .attr(\"stroke-width\", 1);\n" +
             "focus.append(\"text\")\n" +
             "     .attr(\"text-anchor\", \"middle\")\n" +
             "     .attr(\"dy\", \".35em\");\n" +
             "svg.on(\"mouseover\", function() { focus.style(\"display\", null); })\n" +
             "   .on(\"mouseout\", function() { focus.style(\"display\", \"none\"); });\n" +
             "svg.on(\"mousemove\", function() {\n" +
             "    var date = x.invert(d3.mouse(this)[0]);\n" +
             "    var index = bisector(pts, date, 1);\n" +
             "    d0 = pts[index-1];\n" +
             "    d1 = index == pts.length ? d0 : pts[index];\n" +
             "    var d = date - d0.x > d1.x - date ? d1 : d0;\n" +
             "    focus.attr(\"transform\", \"translate(\"+x(d.x)+\",\"+y(d.y)+\")\");\n" +
             "    focus.select(\"text\").text(+d.y.toFixed(1));\n" +
             "    var bbox = focus.select(\"text\").node().getBBox();\n" +
             "    focus.select(\"rect\")\n" +
             "         .attr(\"x\", bbox.x-3)\n" +
             "         .attr(\"y\", bbox.y-1)\n" +
             "         .attr(\"width\", bbox.width+6)\n" +
             "         .attr(\"height\", bbox.height+2);\n" +
             "});");
        html("}");
        html("</script>");
    }
    
    private void drawPlot(DataAccessor accessor, String title) {
        html("<svg id=\""+accessor.id()+"\" width=\"100%\" height=\"260\" style=\"border: 1px solid black;\">");
        html("</svg>");
        html("<script type=\"text/javascript\">");
        html(accessor.build());
        html("drawPlot('"+accessor.id()+"', pts, '"+message(title)+"');");
        html("</script>");
    }

    private void printData(List<Position> positions) {
        html("<script type=\"text/javascript\">");
        html("data=[");
        for(Position p : positions)
            if(p.getOther() != null && !p.getOther().isEmpty()) {
                html("{time:new Date("+p.getTime().getTime()+"),"
                        + "other:"+p.getOther()+"},");
            }
        html("]");
        html("</script>");
    }

    private void drawTable(List<Position> positions) {
        tableStart("table", hover().condensed().height(DEFAULT_TABLE_HEIGHT));

        // header
        tableHeadStart();
        tableRowStart();

        for (String header : new String[] {"time", "fuelConsumed", "fuelLevel"}) {
            tableHeadCellStart();
            text(message(header));
            tableHeadCellEnd();
        }

        tableHeadEnd();
        
        // body
        tableBodyStart();

        Double startFuelConsumed = null;
        for (Position p : positions) {
            if (p.getOther() == null || p.getOther().isEmpty()) {
                continue;
            }
            Map<String, Object> other = JsonXmlParser.parse(p.getOther());
            if(!other.containsKey("io83") && !other.containsKey("io84"))
                continue;
            
            tableRowStart();
            tableCell(formatDate(p.getTime()));
            if(other.containsKey("io83")) {
                double val = Double.parseDouble(other.get("io83").toString()) / 10;
                if(startFuelConsumed == null)
                    startFuelConsumed = val;
                tableCell(String.format("%.1f", val - startFuelConsumed));
            } else {
                tableCell("");
            }
            
            if(other.containsKey("io84")) {
                double val = Double.parseDouble(other.get("io84").toString());
                tableCell(String.format("%.1f", val));
            } else {
                tableCell("");
            }
            
            tableRowEnd();
        }

        tableBodyEnd();
        tableEnd();
    }
    
    
    private static class DataAccessor {
        String id;
        String conversion = "y";
        boolean normalize = false;
        
        public DataAccessor id(String id) {
            this.id = id;
            return this;
        }
        
        public DataAccessor conversion(String conversion) {
            this.conversion = conversion;
            return this;
        }
        
        public DataAccessor normalize(boolean normalize) {
            this.normalize = normalize;
            return this;
        }
        
        public String id() {
            return id;
        }
        
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("yfc = function(y) { return ").append(conversion).append("; };\n");
            sb.append("var pts = data.filter(function(d) { return d.other.").append(id)
                    .append(" !== undefined; })\n")
                    .append(".map(function(d) { return {x: d.time, ")
                    .append("y: yfc(d.other.").append(id).append(")}; });\n");
            if(normalize) {
                sb.append("var yRange = range(pts, function(d) { return d.y; });\n");
                sb.append("var scale = d3.scaleLinear().domain(yRange).range([0, yRange[1]-yRange[0]]);\n");
                sb.append("pts = pts.map(function(d) { return {x: d.x, ")
                        .append("y: scale(d.y)}; });\n");
            }
            return sb.toString();
        }
    }
}
