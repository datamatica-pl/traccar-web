package org.traccar.web.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name="registrationReviews", 
        indexes = @Index(name="regReview_pkey", columnList="id"))
public class RegistrationMaintenance extends MaintenanceBase implements IsSerializable{
    
    public RegistrationMaintenance() {}
    
    private Date serviceDate;

    RegistrationMaintenance(RegistrationMaintenance other) {
        copyFrom(other);
    }
    
    public Date getServiceDate() {
        return serviceDate;
    }
    
    public void setServiceDate(Date serviceDate) {
        this.serviceDate = serviceDate;
    }
    
    public void copyFrom(RegistrationMaintenance other) {
        serviceDate = other.serviceDate;
        super.copyFrom(other);
    }
}
