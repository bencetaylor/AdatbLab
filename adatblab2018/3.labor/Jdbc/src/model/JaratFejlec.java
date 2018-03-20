package model;

import java.time.LocalDate;

import dal.exceptions.ValidationException;

public class JaratFejlec {
    public JaratFejlec() {
    
    }
    private Integer vonatszam;

    public Integer getVonatszam() {
        return vonatszam;
    }

    public void setVonatszam(Integer vonatszam) {
        this.vonatszam = vonatszam;
    }

    private String tipus;

    public String getTipus() {
        return tipus;
    }

    public void setTipus(String tipus) {
        this.tipus = tipus;
    }

    private String megjegyzes;

    public String getMegjegyzes() {
        return megjegyzes;
    }

    public void setMegjegyzes(String megjegyzes) {
        this.megjegyzes = megjegyzes;
    }


}