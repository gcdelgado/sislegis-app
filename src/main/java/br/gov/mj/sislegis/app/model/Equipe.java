package br.gov.mj.sislegis.app.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "equipe")
@XmlRootElement
public class Equipe extends AbstractEntity {

	private static final long serialVersionUID = 8516082010865687791L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Column
	private String nome;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "equipe", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
	private Set<EquipeUsuario> listaEquipeUsuario;

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Set<EquipeUsuario> getListaEquipeUsuario() {
		return listaEquipeUsuario;
	}

	public void setListaEquipeUsuario(Set<EquipeUsuario> listaEquipeUsuario) {
		this.listaEquipeUsuario = listaEquipeUsuario;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (nome != null && !nome.trim().isEmpty())
			result += "nome: " + nome;

		return result;
	}
}