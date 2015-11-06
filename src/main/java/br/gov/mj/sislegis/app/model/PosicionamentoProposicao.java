package br.gov.mj.sislegis.app.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "posicionamento_proposicao")
public class PosicionamentoProposicao extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne
    private Posicionamento posicionamento;

    @ManyToOne
    private Proposicao proposicao;

    @ManyToOne
    private Usuario usuario;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCriacao = new Date();

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Posicionamento getPosicionamento() {
        return posicionamento;
    }

    public void setPosicionamento(Posicionamento posicionamento) {
        this.posicionamento = posicionamento;
    }

    public Proposicao getProposicao() {
        return proposicao;
    }

    public void setProposicao(Proposicao proposicao) {
        this.proposicao = proposicao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}