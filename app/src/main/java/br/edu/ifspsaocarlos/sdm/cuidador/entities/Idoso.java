package br.edu.ifspsaocarlos.sdm.cuidador.entities;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe que representa um idoso
 *
 * @author Anderson Canale Garcia
 */
public class Idoso implements Serializable {

    private Contato contato;

    public Contato getContato() {
        return contato;
    }

    public void setContato(Contato contato) {
        this.contato = contato;
    }
}
