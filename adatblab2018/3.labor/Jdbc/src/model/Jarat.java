package model;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.regex.Pattern;

import dal.exceptions.ValidationException;

public class Jarat {
    public Jarat() {
    
    }
    private Integer vonatszam;

    public Integer getVonatszam() {
        return vonatszam;
    }

    public void setVonatszam(Integer vonatszam) {
        this.vonatszam = vonatszam;
    }

    public void parseVonatszam(String vonatszam) throws ValidationException {
		if (!Pattern.matches("[0-9]\\d{0,4}", vonatszam)) {
			throw new ValidationException("vonatszam");		
		}
		else
			setVonatszam(Integer.parseInt(vonatszam));
    }
    
    private String tipus;

    public String getTipus() {
        return tipus;
    }

    public void setTipus(String tipus) {
        this.tipus = tipus;
    }

    public void parseTipus(String tipus) throws ValidationException {
        setTipus(tipus);
    }
    private String nap;

    public String getNap() {
        return nap;
    }

    public void setNap(String nap) {
        this.nap = nap;
    }

    public void parseNap(String nap) throws ValidationException {
    	if (!Pattern.matches("[0-1]\\d{6}", nap)) {
			throw new ValidationException("vonatszam");		
		}
		else
			setNap(nap);
    }
    private LocalDate kezd;

    public LocalDate getKezd() {
        return kezd;
    }

    public void setKezd(LocalDate kezd) {
        this.kezd = kezd;
    }

    public void parseKezd(String kezd) throws ValidationException, ParseException {
    	if(kezd.equals("")){
    		setKezd(null);
    		return;
    	}
    	if (!Pattern.matches("[1-9]\\d{3}\\-[0-1]\\d\\-[0-3]\\d", kezd)) {
    		throw new ValidationException("kezd");
    	}
    	else {
    		LocalDate localDate = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(kezd).getTime()).toLocalDate();
    		setKezd(localDate);
		}
    }
    private LocalDate vege;

    public LocalDate getVege() {
        return vege;
    }

    public void setVege(LocalDate vege) {
        this.vege = vege;
    }

    public void parseVege(String vege) throws ValidationException, ParseException {
    	if(vege.equals("")){
    		setVege(null);
    		return;
    	}
    	if (!Pattern.matches("[1-9]\\d{3}\\-[0-1]\\d\\-[0-3]\\d", vege)) {
    		throw new ValidationException("vege");
    	}
    	else {
    		LocalDate localDate = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(vege).getTime()).toLocalDate();
    		setVege(localDate);
		}
    }
    
    private String megjegyzes;

    public String getMegjegyzes() {
        return megjegyzes;
    }

    public void setMegjegyzes(String megjegyzes) {
        this.megjegyzes = megjegyzes;
    }

    public void parseMegjegyzes(String megjegyzes) throws ValidationException {
        setMegjegyzes(megjegyzes);
    }

}