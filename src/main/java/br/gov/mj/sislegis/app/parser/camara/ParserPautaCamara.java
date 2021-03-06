package br.gov.mj.sislegis.app.parser.camara;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import br.gov.mj.sislegis.app.enumerated.Origem;
import br.gov.mj.sislegis.app.model.Comissao;
import br.gov.mj.sislegis.app.model.Proposicao;
import br.gov.mj.sislegis.app.model.pautacomissao.PautaReuniaoComissao;
import br.gov.mj.sislegis.app.model.pautacomissao.ProposicaoPautaComissao;
import br.gov.mj.sislegis.app.parser.ParserFetcher;
import br.gov.mj.sislegis.app.parser.ProposicaoSearcher;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

public class ParserPautaCamara {

	public static void main(String[] args) throws Exception {
		ParserPautaCamara parser = new ParserPautaCamara();

		// TODO: Informação que vem do filtro
		Long idComissao = 2001L;
		String datIni = "20151102";
		String datFim = "20151109";
		Set<PautaReuniaoComissao> pautas = parser.getPautaComissao("", idComissao, datIni, datFim);
		for (Iterator iterator = pautas.iterator(); iterator.hasNext();) {
			PautaReuniaoComissao pautaReuniaoComissao = (PautaReuniaoComissao) iterator.next();
			System.out.println(pautaReuniaoComissao);
			for (Iterator iterator2 = pautaReuniaoComissao.getProposicoesDaPauta().iterator(); iterator2.hasNext();) {
				ProposicaoPautaComissao ppc = (ProposicaoPautaComissao) iterator2.next();
				System.out.println("\t" + ppc + " " + ppc.getProposicao() + " ");
				System.out.println("\t Resultado: " + ppc.getResultado());

			}

		}
	}

	public List<ReuniaoBeanCamara> getReunioes(Long idComissao, String datIni, String datFim) throws IOException {
		return getPauta(idComissao, datIni, datFim).getReunioes();
	}

	public PautaBean getPauta(Long idComissao, String datIni, String datFim) throws IOException {
		String wsURL = new StringBuilder("http://www.camara.gov.br/SitCamaraWS/Orgaos.asmx/ObterPauta?IDOrgao=")
				.append(idComissao).append("&datIni=").append(datIni).append("&datFim=").append(datFim).toString();

		XStream xstream = new XStream();
		xstream.ignoreUnknownElements();

		PautaBean pauta = new PautaBean();

		config(xstream);

		ParserFetcher.fetchXStream(wsURL, xstream, pauta);
		return pauta;
	}

	public Set<PautaReuniaoComissao> getPautaComissao(String comissaoNome, Long idComissao, String datIni, String datFim)
			throws IOException, ParseException {

		Set<PautaReuniaoComissao> pautas = new HashSet<PautaReuniaoComissao>();
		String wsURL = new StringBuilder("http://www.camara.gov.br/SitCamaraWS/Orgaos.asmx/ObterPauta?IDOrgao=")
				.append(idComissao).append("&datIni=").append(datIni).append("&datFim=").append(datFim).toString();

		XStream xstream = new XStream();
		xstream.ignoreUnknownElements();

		PautaBean pauta = new PautaBean();
		xstream.alias("pauta", PautaBean.class);
		xstream.alias("reuniao", ReuniaoBeanCamara.class);
		xstream.alias("proposicao", ProposicaoPautaComissaoWrapper.class);

		// Utilizamos o implicit quando os filhos já tem os dados que queremos
		// buscar. Ou seja, não tem um pai e vários filhos do mesmo tipo.
		xstream.addImplicitCollection(PautaBean.class, "reunioes");
		xstream.aliasAttribute(PautaBean.class, "orgao", "orgao");
		xstream.aliasAttribute(PautaBean.class, "dataInicial", "dataInicial");
		xstream.aliasAttribute(PautaBean.class, "dataFinal", "dataFinal");

		xstream.aliasField("comissao", ReuniaoBeanCamara.class, "comissao");
		xstream.aliasField("horario", ReuniaoBeanCamara.class, "hora");
		xstream.aliasField("data", ReuniaoBeanCamara.class, "data");
		xstream.aliasField("codReuniao", ReuniaoBeanCamara.class, "codigo");
		xstream.aliasField("tipo", ReuniaoBeanCamara.class, "tipo");
		xstream.aliasField("estado", ReuniaoBeanCamara.class, "situacao");
		xstream.aliasField("tituloReuniao", ReuniaoBeanCamara.class, "titulo");

		ParserFetcher.fetchXStream(wsURL, xstream, pauta);

		for (ReuniaoBeanCamara reuniao : pauta.getReunioes()) {
			Comissao comissao = new Comissao();
			String sigla = reuniao.getComissao();
			if (sigla != null && sigla.indexOf("-") > -1) {
				sigla = sigla.substring(0, sigla.indexOf("-")).trim();
			}
			comissao.setSigla(sigla);

			PautaReuniaoComissao pautaReuniaoComissao = new PautaReuniaoComissao(reuniao.getDate(), comissao,
					reuniao.getCodigo());
			pautaReuniaoComissao.setOrigem(Origem.CAMARA);
			pautaReuniaoComissao
					.setLinkPauta("http://www.camara.leg.br/internet/ordemdodia/ordemDetalheReuniaoCom.asp?codReuniao="
							+ reuniao.getCodigo().toString());
			pautaReuniaoComissao.converterSituacao(reuniao.getSituacao());
			pautaReuniaoComissao.setTipo(reuniao.getTipo());
			pautaReuniaoComissao.setTitulo(reuniao.getTitulo());

			// adiciona dados da comissao
			for (ProposicaoPautaComissaoWrapper pautaProposicao : reuniao.getPautaProposicoes()) {
				Proposicao ptemp = new Proposicao();
				ptemp.setIdProposicao(pautaProposicao.idProposicao);
				ptemp.setOrigem(Origem.CAMARA);
				ptemp.setEmenta(pautaProposicao.ementa);
				ptemp.setComissao(sigla);
				ptemp.setSigla(pautaProposicao.sigla);
				ProposicaoPautaComissao ppc = new ProposicaoPautaComissao(pautaReuniaoComissao, ptemp);
				ppc.setOrdemPauta(pautaProposicao.numOrdemApreciacao);
				ppc.setRelator(pautaProposicao.getRelator());
				ppc.setResultado(pautaProposicao.resultado);
				pautaReuniaoComissao.addProposicaoPauta(ppc);
			}
			if (pautaReuniaoComissao.getProposicoesDaPauta().size() > 0) {
				pautas.add(pautaReuniaoComissao);
			}
		}

		return pautas;
	}

	private void config(XStream xstream) {
		xstream.alias("pauta", PautaBean.class);
		xstream.alias("reuniao", ReuniaoBeanCamara.class);
		xstream.alias("proposicao", Proposicao.class);

		// Utilizamos o implicit quando os filhos já tem os dados que queremos
		// buscar. Ou seja, não tem um pai e vários filhos do mesmo tipo.
		xstream.addImplicitCollection(PautaBean.class, "reunioes");
		xstream.aliasAttribute(PautaBean.class, "orgao", "orgao");
		xstream.aliasAttribute(PautaBean.class, "dataInicial", "dataInicial");
		xstream.aliasAttribute(PautaBean.class, "dataFinal", "dataFinal");

		xstream.aliasField("comissao", ReuniaoBeanCamara.class, "comissao");
		xstream.aliasField("horario", ReuniaoBeanCamara.class, "hora");
		xstream.aliasField("data", ReuniaoBeanCamara.class, "data");
		xstream.aliasField("codReuniao", ReuniaoBeanCamara.class, "codigo");
		xstream.aliasField("tipo", ReuniaoBeanCamara.class, "tipo");
		xstream.aliasField("estado", ReuniaoBeanCamara.class, "situacao");
		xstream.aliasField("tituloReuniao", ReuniaoBeanCamara.class, "titulo");
	}
}

class OrgaosBean {

	protected List<OrgaoCamara> orgaos = new ArrayList<OrgaoCamara>();

}

class PautaBean {
	protected String orgao;
	protected String dataInicial;
	protected String dataFinal;

	protected List<ReuniaoBeanCamara> reunioes = new ArrayList<ReuniaoBeanCamara>();

	protected List<ReuniaoBeanCamara> getReunioes() {
		return reunioes;
	}

	protected String getOrgao() {
		return orgao;
	}
}

@XStreamAlias("proposicao")
class ProposicaoPautaComissaoWrapper {
	Integer idProposicao;
	Integer numOrdemApreciacao;
	String resultado;
	String relator;
	String sigla;
	String textoParecerRelator;
	String ementa;

	public String getRelator() {
		if (relator == null || relator.length() == 0) {
			return ProposicaoSearcher.SEM_RELATOR_DEFINIDO;
		}
		return relator;
	}

}