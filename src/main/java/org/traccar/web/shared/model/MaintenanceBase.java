package org.traccar.web.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gwt.user.client.rpc.IsSerializable;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class MaintenanceBase implements IsSerializable {

    public MaintenanceBase() {}
    
    protected MaintenanceBase(MaintenanceBase other) {
        copyFrom(other);
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    @JsonIgnore
    protected long id;
    
    public long getId(){
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
   // sequence number of this interval
    private int indexNo;

    public int getIndexNo() {
        return indexNo;
    }

    public void setIndexNo(int indexNo) {
        this.indexNo = indexNo;
    }
    
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "maintenances_fkey_device_id"))
    @JsonIgnore
    protected Device device;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Maintenance)) return false;

        Maintenance that = (Maintenance) o;

        if (getId() != that.getId()) return false;
        if (getIndexNo() != that.getIndexNo()) return false;
        if (getDevice() != null ? !getDevice().equals(that.getDevice()) : that.getDevice() != null) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + (getDevice() != null ? getDevice().hashCode() : 0);
        result = 31 * result + getIndexNo();
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        return result;
    }
    
    public void copyFrom(MaintenanceBase other) {
        this.device = other.device;
        this.id = other.id;
        this.indexNo = other.indexNo;
        this.name = other.name;
    }
}
