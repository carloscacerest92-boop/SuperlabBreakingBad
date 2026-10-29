package superlab.modelo;

import java.util.List;

public class EstadoLaboratorio {
    private String zona;
    private List<Elemento> elementos;

    public EstadoLaboratorio() {
    }

    public EstadoLaboratorio(String zona, List<Elemento> elementos) {
        this.zona = zona;
        this.elementos = elementos;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public List<Elemento> getElementos() {
        return elementos;
    }

    public void setElementos(List<Elemento> elementos) {
        this.elementos = elementos;
    }

    public static class Elemento {
        private String id;
        private String tipo;
        private String estado;

        public Elemento() {
        }

        public Elemento(String id, String tipo, String estado) {
            this.id = id;
            this.tipo = tipo;
            this.estado = estado;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }
}