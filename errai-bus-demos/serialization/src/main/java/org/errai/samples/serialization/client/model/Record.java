package org.errai.samples.serialization.client.model;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@ExposeEntity
public class Record implements Serializable {
    private int recordId;
    private String name;
    private float balance;
    private Date accountOpened;
    private Set<String> stuff;

    public Record() {
    }

    public Record(int recordId, String name, float balance, Date accountOpened, String[] stuff) {
        this.recordId = recordId;
        this.name = name;
        this.balance = balance;
        this.accountOpened = accountOpened;

        this.stuff = new HashSet<String>();
        this.stuff.addAll(Arrays.asList(stuff));
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

    public Set<String> getStuff() {
        return stuff;
    }

    public void setStuff(Set<String> stuff) {
        this.stuff = stuff;
    }
}
