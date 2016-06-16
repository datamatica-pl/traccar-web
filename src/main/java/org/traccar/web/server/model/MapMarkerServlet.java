/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.server.model;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class MapMarkerServlet extends HttpServlet{
    
    public static final String COLOR_PARAMETER = "color";
    public static final String IMG_ADDRESS = "/img/arrow.svg";
    private Document img;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            img = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(config.getServletContext().getRealPath(IMG_ADDRESS));
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(MapMarkerServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("image/svg+xml");
        try{
            String hexColor = req.getParameter(COLOR_PARAMETER);
            Element styles = (Element)img.getElementsByTagName("defs").item(0);
            styles = (Element)styles.getElementsByTagName("style").item(0);
            styles.setTextContent(String.format(".background{fill :#%s;}", hexColor));
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource domSource = new DOMSource(img);
            StreamResult result = new StreamResult(resp.getOutputStream());
            transformer.transform(domSource, result);
        } catch(Exception e) {}
    } 
}
