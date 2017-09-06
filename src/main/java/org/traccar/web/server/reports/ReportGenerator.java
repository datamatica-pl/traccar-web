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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSettings;
import pl.datamatica.traccar.model.ReportFormat;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;
import org.traccar.web.client.model.DataService;
import org.traccar.web.server.model.ServerMessages;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.traccar.web.client.model.api.ApiDeviceIcon;
import org.traccar.web.client.model.api.Resources;

public abstract class ReportGenerator {
    public static final int DEFAULT_TABLE_HEIGHT = 150;
    
    @Inject
    EntityManager entityManager;

    @Inject
    User currentUser;

    @Inject
    HttpServletRequest request;

    @Inject
    HttpServletResponse response;

    @Inject
    DataService dataService;

    @Inject
    ServerMessages messages;

    @Inject
    ApplicationSettings applicationSettings;

    private IReportRenderer renderer;

    private SimpleDateFormat dateFormat;

    private SimpleDateFormat longDateFormat;

    private TimeZone timeZone;

    abstract void generateImpl(Report report) throws IOException;

    public final void generate(Report report) throws IOException {
        if(report.getFormat() == ReportFormat.CSV)
            renderer = new CSVReportRenderer(response);
        else
            renderer = new HtmlReportRenderer(response);

        timeZone = currentUser.getUserSettings().getTimeZoneId() == null
                ? TimeZone.getDefault()
                : TimeZone.getTimeZone(currentUser.getUserSettings().getTimeZoneId());
        Locale locale = new Locale(getLocale());
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        dateFormat.setTimeZone(timeZone);
        longDateFormat = new SimpleDateFormat("d MMM yyyy", locale);
        longDateFormat.setTimeZone(timeZone);
        
        loadDeviceIcons();
        renderer.start(report);
        generateImpl(report);
        renderer.end(report);
    }

    private void loadDeviceIcons() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        //http://nemerosa.ghost.io/2014/11/06/disabling-ssl-checks-with-apache-httpclient-4-3-x/
        SSLConnectionSocketFactory sslSocketFactory;
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{
                new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }    
                }
            }, new SecureRandom());
            sslSocketFactory = new SSLConnectionSocketFactory(ctx, 
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        CredentialsProvider cp = new BasicCredentialsProvider();
        try {
            Context ctx = new InitialContext();
            cp.setCredentials(new AuthScope("localhost", 443), 
                    new UsernamePasswordCredentials("report", ctx.lookup("java:/reports.password").toString()));
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
        
        CloseableHttpClient client = HttpClientBuilder.create()
                .setConnectionManager(new PoolingHttpClientConnectionManager(registry))
                .setDefaultCredentialsProvider(cp)
                .build();
        try {
            String res = EntityUtils.toString(client
                    .execute(new HttpGet("https://localhost/api/v1/resources/deviceicons"))
                    .getEntity());
            List<ApiDeviceIcon> icons = mapper.readValue(res, new TypeReference<List<ApiDeviceIcon>>(){});
            for(ApiDeviceIcon icon : icons)
                Resources.getInstance().icon(icon.getId(), icon.getUrl().replace("/images/", "/markers/"));
        } finally {
            client.close();
        }
    }

    void h1(String text) {
        renderer.h1(text);
    }

    void h2(String text) {
        renderer.h2(text);
    }

    void h3(String text) {
        renderer.h3(text);
    }

    public void tableRowStart() {
        renderer.tableRowStart();
    }

    public void paragraphEnd() {
        renderer.paragraphEnd();
    }

    public void tableRowEnd() {
        renderer.tableRowEnd();
    }

    public void tableBodyEnd() {
        renderer.tableBodyEnd();
    }

    public void tableStart() {
        renderer.tableStart("", null);
    }

    public void tableStart(IReportRenderer.TableStyle style) {
        renderer.tableStart("", style);
    }
    
    public void tableStart(String id, IReportRenderer.TableStyle style) {
        renderer.tableStart(id, style);
    }

    IReportRenderer.TableStyle hover() {
        return new HtmlReportRenderer.TableStyle().hover();
    }

    IReportRenderer.TableStyle condensed() {
        return new HtmlReportRenderer.TableStyle().condensed();
    }

    public void tableHeadStart() {
        renderer.tableHeadStart();
    }

    public void tableHeadEnd() {
        renderer.tableHeadEnd();
    }

    public void tableHeadCellStart() {
        renderer.tableHeadCellStart(null);
    }

    public void tableHeadCellStart(IReportRenderer.CellStyle style) {
        renderer.tableHeadCellStart(style);
    }

    public void tableHeadCellEnd() {
        renderer.tableHeadCellEnd();
    }

    public void panelBodyStart() {
        renderer.panelBodyStart();
    }

    public void panelBodyEnd() {
        renderer.panelBodyEnd();
    }

    public void tableCellEnd() {
        renderer.tableCellEnd();
    }

    public void panelStart() {
        renderer.panelStart();
    }

    public void panelHeadingEnd() {
        renderer.panelHeadingEnd();
    }

    public void text(String text) {
        renderer.text(text);
    }

    public void tableEnd() {
        renderer.tableEnd();
    }

    public void panelEnd() {
        renderer.panelEnd();
    }

    public void panelHeadingStart() {
        renderer.panelHeadingStart();
    }

    public void tableBodyStart() {
        renderer.tableBodyStart();
    }
    
    public void paragraphStart() {
        renderer.paragraphStart();
    }

    public void bold(String text) {
        renderer.bold(text);
    }

    public void tableCellStart() {
        renderer.tableCellStart(null);
    }

    void tableCell(String text) {
        tableCellStart();
        text(text);
        tableCellEnd();
    }
    
    void extentCell(Position p1, Position p2) {
        String extent = new MapBuilder.MapBoundsBuilder()
                .addPosition(p1)
                .addPosition(p2)
                .create(1e-2);
        tableCellStart(new HtmlReportRenderer.CellStyle().hidden(true).id("ext"));
        text(extent);
        tableCellEnd();
    }

    public void tableCellStart(IReportRenderer.CellStyle style) {
        renderer.tableCellStart(style);
    }

    public void link(String url, String target, String text) {
        renderer.link(url, target, text);
    }

    void mapLink(double latitude, double longitude) {
        UserSettings userSettings = currentUser.getUserSettings();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat lonLatFormat = new DecimalFormat("##.######", symbols);
        String text = lonLatFormat.format(latitude) + " \u00B0, " +
                lonLatFormat.format(longitude) + " \u00B0";

        link("http://www.openstreetmap.org/?" +
                "mlat=" + lonLatFormat.format(latitude) + "&mlon=" + lonLatFormat.format(longitude) +
                "#map=" + userSettings.getZoomLevel() + "/" +
                lonLatFormat.format(latitude) + "/" + lonLatFormat.format(longitude),
                "_blank", text);
    }
    
    protected MapBuilder getMapBuilder() {
        return new MapBuilder("100%", "500px");
    }
    
    void html(String html) {
        renderer.html(html);
    }
    
    void dataRow(String title, String text) {
        tableRowStart();
        tableCellStart();
        bold(title + ":");
        tableCellEnd();
        tableCellStart();
        text(text);
        tableCellEnd();
        tableRowEnd();
    }

    void deviceDetails(Device device) {
        if (hasNonEmpty(device.getDescription(), device.getPlateNumber(), device.getVehicleInfo())) {
            paragraphStart();
            tableStart();
            tableBodyStart();

            if (isNotEmpty(device.getDescription())) {
                dataRow(message("description"), device.getDescription());
            }

            if (isNotEmpty(device.getPlateNumber())) {
                dataRow(message("plateNumber"), device.getPlateNumber());
            }

            if (isNotEmpty(device.getVehicleInfo())) {
                dataRow(message("vehicleBrandModelColor"), device.getVehicleInfo());
            }

            tableBodyEnd();
            tableEnd();
            paragraphEnd();
        }
    }

    boolean hasNonEmpty(String... strings) {
        for (String string : strings) {
            if (isNotEmpty(string)) {
                return true;
            }
        }
        return false;
    }

    boolean isNotEmpty(String string) {
        return string != null && !string.trim().isEmpty();
    }

    IReportRenderer.CellStyle colspan(int colspan) {
        return new HtmlReportRenderer.CellStyle().colspan(colspan);
    }

    IReportRenderer.CellStyle rowspan(int rowspan) {
        return new HtmlReportRenderer.CellStyle().rowspan(rowspan);
    }

    List<Device> getDevices(Report report) {
        if (report.getDevices().isEmpty()) {
            return dataService.getDevices();
        } else {
            List<Device> devices = new ArrayList<>(report.getDevices().size());
            for (Device reportDevice : report.getDevices()) {
                Device device = entityManager.find(Device.class, reportDevice.getId());
                if (currentUser.hasAccessTo(device)) {
                    devices.add(device);
                }
            }
            return devices;
        }
    }

    List<GeoFence> getGeoFences(Report report, Device device) {
        List<GeoFence> geoFences;
        if (report.getGeoFences().isEmpty()) {
            geoFences = new ArrayList<>(dataService.getGeoFences());
        } else {
            geoFences = new ArrayList<>(report.getGeoFences().size());
            for (GeoFence reportGeoFence : report.getGeoFences()) {
                GeoFence geoFence = entityManager.find(GeoFence.class, reportGeoFence.getId());
                if (currentUser.hasAccessTo(geoFence)) {
                    geoFences.add(geoFence);
                }
            }
        }
        // filter device-specific geo-fences that are not assigned to device from method arguments
        for (Iterator<GeoFence> it = geoFences.iterator(); it.hasNext(); ) {
            GeoFence geoFence = it.next();
            if (!geoFence.getDevices().contains(device)) {
                it.remove();
            }
        }
        return geoFences;
    }

    String formatDuration(long duration) {
        if (duration == 0) {
            return "0s";
        }

        int days = (int) (duration / 86400000L);
        duration -= (long) days * 86400000L;

        int hours = (int) (duration / 3600000L);
        duration -= (long) hours * 3600000L;

        int minutes = (int) (duration / 60000L);
        duration -= (long) minutes * 60000L;

        int seconds = (int) (duration / 1000L);

        return
                (days == 0 ? "" : days + message("day") + " ") +
                        (hours == 0 ? "" : hours + message("hour") + " ") +
                        (minutes == 0 ? "" : minutes + message("minute") + " ") +
                        (seconds == 0 ? "" : seconds + message("second") + " ");
    }

    String formatSpeed(double speed) {
        UserSettings.SpeedUnit speedUnit = currentUser.getUserSettings().getSpeedUnit();
        NumberFormat speedFormat = NumberFormat.getInstance();
        speedFormat.setMaximumFractionDigits(2);
        speedFormat.setMinimumIntegerDigits(0);
        return speedFormat.format((Double.isNaN(speed) ? 0d : speed) * speedUnit.getFactor()) + " " + speedUnit.getUnit();
    }

    String formatDistance(double distance) {
        UserSettings.SpeedUnit speedUnit = currentUser.getUserSettings().getSpeedUnit();
        UserSettings.DistanceUnit distanceUnit = speedUnit.getDistanceUnit();
        NumberFormat distanceFormat = NumberFormat.getInstance();
        distanceFormat.setMaximumFractionDigits(2);
        distanceFormat.setMinimumIntegerDigits(0);
        distanceFormat.setMinimumIntegerDigits(1);
        return distanceFormat.format((Double.isNaN(distance) ? 0d : distance) * distanceUnit.getFactor()) + " " + distanceUnit.getUnit();
    }

    String formatDate(Date date) {
        return dateFormat.format(date);
    }

    String formatDateLong(Date date) {
        return longDateFormat.format(date);
    }

    String getLocale() {
        String locale = request.getParameter("locale");
        if (locale == null) {
            locale = applicationSettings.getLanguage();
        }
        return locale;
    }

    String message(String key) {
        return messages.message(getLocale(), key);
    }

    TimeZone getTimeZone() {
        return timeZone;
    }
}
