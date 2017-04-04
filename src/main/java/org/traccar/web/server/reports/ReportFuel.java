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
            drawPlot();

            panelBodyEnd();

            panelEnd();

        }
    
    }
    
    private void drawPlot() {
        html("<svg width=\"100%\" height=\"260\" style=\"border: 1px solid black;\">");
        html("</svg>");
        html("<script type=\"text/javascript\">");
        html("var pts = data.filter(function(d) { return d.other.io89 !== undefined; })"
                + ".map(function (d) {return {x: d.time, y: d.other.io89}};);");
        html("var plotWidth = d3.select(\"svg\").node().getBoundingClientRect().width;");
        html("var minDate = d3.min(pts, function(d) { return d.x; });\n" +
             "var maxDate = d3.max(pts, function(d) { return d.x; });\n" +
             "var x = d3.scaleTime()\n" +
             "               .domain([minDate, maxDate])\n" +
             "               .range([30, plotWidth-30]);\n" +
             "var bisector = d3.bisector(function(d) { return d.x; }).left;\n" +
             "var y = d3.scaleLinear()\n" +
             "          .domain([0, 100])\n" +
             "          .range([200, 30]);\n" +
             "var line = d3.line()\n" +
             "             .x(function(d) { return x(d.x);})\n" +
             "             .y(function(d) { return y(d.y);});\n" +
             "var svg = d3.select(\"svg\");");
        
        html("svg.append(\"g\")\n" +
             "   .append(\"path\")\n" +
             "   .attr(\"d\", line(pts))\n" +
             "   .attr(\"stroke\", \"blue\")\n" +
             "   .attr(\"stroke-width\", 2)\n" +
             "   .attr(\"fill\", \"none\");\n" +
             "svg.append(\"g\")\n" +
             "   .attr(\"transform\", \"translate(30, 0)\")\n" +
             "   .call(d3.axisLeft().scale(scaleY));\n" +
             "svg.append(\"g\")\n" +
             "   .attr(\"transform\", \"translate(0, 200)\")\n" +
             "   .call(d3.axisBottom().scale(scaleX));");
        
        html("var focus = svg.append(\"g\")\n" +
             "               .style(\"display\", \"none\");\n" +
             "focus.append(\"rect\")\n" +
             "     .attr(\"fill\", \"#FFFFFF\")\n" +
             "     .attr(\"opacity\", 0.7)\n" +
             "     .attr(\"stroke\", \"black\")\n" +
             "     .attr(\"stroke-width\", 1);\n" +
             "focus.append(\"text\")\n" +
             "     .attr(\"style\", \"border: 1px solid black;\")\n" +
             "     .attr(\"text-anchor\", \"middle\")\n" +
             "     .attr(\"dy\", \".35em\");\n" +
             "svg.on(\"mouseover\", function() { focus.style(\"display\", null); })\n" +
             "   .on(\"mouseout\", function() { focus.style(\"display\", \"none\"); });\n" +
             "svg.on(\"mousemove\", function() {\n" +
             "    var date = x.invert(d3.mouse(this)[0]);\n" +
             "    var index = bisector(pts, date, 1);\n" +
             "    d0 = ev[index-1];\n" +
             "    d1 = index == ev.length ? d0 : ev[index];\n" +
             "    var d = date - new Date(d0.x) > new Date(d1.x) - date ? d1 : d0;\n" +
             "    focus.attr(\"transform\", \"translate(\"+x(d.x)+\",\"+y(d.y)+\")\");\n" +
             "    focus.select(\"text\").text(d.y);\n" +
             "    var bbox = focus.select(\"text\").node().getBBox();\n" +
             "    focus.select(\"rect\")\n" +
             "         .attr(\"x\", bbox.x-3)\n" +
             "         .attr(\"y\", bbox.y-1)\n" +
             "         .attr(\"width\", bbox.width+6)\n" +
             "         .attr(\"height\", bbox.height+2);\n" +
             "});");
        html("</script>");
    }

    private void printData(List<Position> positions) {
        html("<script type=\"text/javascript\">");
        html("data=[");
        for(Position p : positions)
            if(p.getOther() != null && !p.getOther().isEmpty())
                html("{time:new Date(\""+p.getTime()+"\"),other:"+p.getOther()+"},");
        html("]");
        html("</script>");
    }
    
}
