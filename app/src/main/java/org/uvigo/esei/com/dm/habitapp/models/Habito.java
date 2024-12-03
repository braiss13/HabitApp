package org.uvigo.esei.com.dm.habitapp.models;

public class Habito {
    private int id;
    private String nombre;
    private String descripcion;
    private String frecuencia;
    private String categoria;
    private int objetivo;
    private int progreso;
    private boolean estado;

    public Habito(int id, String nombre, String descripcion, String frecuencia, String categoria, int objetivo, int progreso, boolean estado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.frecuencia = frecuencia;
        this.categoria = categoria;
        this.objetivo = objetivo;
        this.progreso = progreso;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public String getCategoria() {
        return categoria;
    }
    public int getProgreso() { return progreso; }
    public int getObjetivo() { return objetivo; }

    public boolean isEstado() {
        return estado;
    }

    public void setId(int id) {
        this.id = id;
    }



    public void setObjetivo(int objetivo) { this.objetivo = objetivo; }

    public void setProgreso(int progreso) { this.progreso = progreso; }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombre + " - " + categoria;
    }
}
