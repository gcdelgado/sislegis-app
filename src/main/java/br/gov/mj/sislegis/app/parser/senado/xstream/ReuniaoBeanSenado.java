package br.gov.mj.sislegis.app.parser.senado.xstream;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.gov.mj.sislegis.app.enumerated.SituacaoSenado;
import br.gov.mj.sislegis.app.util.SislegisUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import br.gov.mj.sislegis.app.enumerated.Origem;
import br.gov.mj.sislegis.app.model.Proposicao;
import br.gov.mj.sislegis.app.model.pautacomissao.PautaReuniaoComissao;
import br.gov.mj.sislegis.app.model.pautacomissao.ProposicaoPautaComissao;
import br.gov.mj.sislegis.app.model.pautacomissao.Sessao;
import br.gov.mj.sislegis.app.model.pautacomissao.SituacaoSessao;

public class ReuniaoBeanSenado extends br.gov.mj.sislegis.app.parser.ReuniaoBean {
	private static final String MATERIA = "MATE";

	public static String getMateria() {
		return MATERIA;
	}

	protected List<ComissaoBean> comissoes = new ArrayList<ComissaoBean>();

	protected List<ParteBean> partes = new ArrayList<ParteBean>();

	protected List<ParteBean> getPartes() {
		return partes;
	}

	protected List<ComissaoBean> getComissoes() {
		return comissoes;
	}

	public Set<ProposicaoPautaComissao> getProposicoesPauta(PautaReuniaoComissao reuniao) {

		for (ParteBean parteBean : this.getPartes()) {
			List<ItemBean> itens = parteBean.getItens();
			for (Iterator iterator = itens.iterator(); iterator.hasNext();) {
				ItemBean itemBean = (ItemBean) iterator.next();

				// Não adicionamos por exemplo, os requerimentos, pois não são
				// tratados como proposições
				if (itemBean.tipo.equalsIgnoreCase(MATERIA)) {

					Materia mat = itemBean.getMateria();
					Proposicao prop = mat.toProposicao();
					//prop.setComissao(comissoes.get(0).getSigla() + " - " + comissoes.get(0).getNome());
                    prop.setComissao(reuniao.getComissao());
					prop.setOrigem(Origem.SENADO);
					prop.setLinkProposicao("http://www.senado.leg.br/atividade/materia/detalhes.asp?p_cod_mate="
							+ prop.getIdProposicao());
					ProposicaoPautaComissao propPauta = new ProposicaoPautaComissao(reuniao, prop);
					propPauta.setOrdemPauta(itemBean.getSeqOrdemPauta());
					propPauta.setRelator(itemBean.getMateria().getRelator());
					propPauta.setResultado(itemBean.resultado.descricao);
					reuniao.addProposicaoPauta(propPauta);
				}
			}

			// Eventos não deve aparecer na lista de proposicoes discutidas

		}

		return reuniao.getProposicoesDaPauta();
	}

	@Override
	public String toString() {

		return titulo + ":" + tipo + " " + situacao + "@" + data + " " + hora;
	}

	public Sessao getSessao() {
		Sessao sessao = new Sessao();
		try {
			sessao.setData(getDate());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		sessao.setIdentificadorExterno(getCodigo().toString());
		sessao.setTitulo(titulo);
		situacao = situacao.replaceAll("\\s", "");

		try {
			sessao.setSituacao(SituacaoSenado.valueOf(situacao).situacaoSessaoCorrespondente());

		} catch (IllegalArgumentException e) {
			Logger.getLogger(SislegisUtil.SISLEGIS_LOGGER).log(Level.SEVERE, "Falha ao converter a situacao da Senado: " + situacao, e);
			sessao.setSituacao(SituacaoSessao.Desconhecido);
		}

		return sessao;
	}

	public Date getDate() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy kk:mm");
		return sdf.parse(data + " " + hora);
	}

}
