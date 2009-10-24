package org.errai.samples.serialization.client.model;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

import java.io.Serializable;
import java.util.Date;

@ExposeEntity
public class Record implements Serializable {
    private int recordId;
    private String name;
    private float balance;
    private Date accountOpened;

    public Record() {
    }

    public Record(int recordId, String name, float balance, Date accountOpened) {
        this.recordId = recordId;
        this.name = name;
        this.balance = balance;
        this.accountOpened = accountOpened;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public Date getAccountOpened() {
        return accountOpened;
    }

    public void setAccountOpened(Date accountOpened) {
        this.accountOpened = accountOpened;
    }
}
