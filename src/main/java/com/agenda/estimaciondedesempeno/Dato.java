package com.agenda.estimaciondedesempeno;

public class Dato {
    private String id;
    private String claseVerdadera;
    private String clasePredicha;

    public Dato(String id, String claseVerdadera, String clasePredicha) {
        this.id = id;
        this.claseVerdadera = claseVerdadera;
        this.clasePredicha = clasePredicha;
    }

    public String getId() { return id; }
    public String getClaseVerdadera() { return claseVerdadera; }
    public String getClasePredicha() { return clasePredicha; }
}
