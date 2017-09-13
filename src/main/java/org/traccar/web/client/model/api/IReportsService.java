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
package org.traccar.web.client.model.api;

import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;
import pl.datamatica.traccar.model.Report;

/**
 *
 * @author ŁŁ
 */
@Path("../api/v1/reports")
public interface IReportsService extends RestService{
    @GET
    void getReports(MethodCallback<List<Report>> callback);
    
    @POST
    void createReport(Report report, MethodCallback<Report> callback);
    
    @DELETE
    @Path("/{id}")
    void removeReport(@PathParam("id") long id, MethodCallback<Void> callback);
}
