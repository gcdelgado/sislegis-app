package br.gov.mj.sislegis.app.service.ejbs;

import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.mj.sislegis.app.model.Comissao;
import br.gov.mj.sislegis.app.parser.camara.ParserComissoesCamara;
import br.gov.mj.sislegis.app.parser.senado.ParserComissoesSenado;
import br.gov.mj.sislegis.app.service.AbstractPersistence;
import br.gov.mj.sislegis.app.service.ComissaoService;
import br.gov.mj.sislegis.app.service.EJBDataCacher;

@Stateless
public class ComissaoServiceEjb extends AbstractPersistence<Comissao, Long> implements ComissaoService {

	private static final String CACHE_KEY_COMISSOES_CAMARA = "COMISSOES_CAMARA";
	private static final String CACHE_KEY_COMISSOES_SENADO = "COMISSOES_SENADO";

	@PersistenceContext
	private EntityManager em;

	@Inject
	private ParserComissoesSenado parserComissoesSenado;

	@Inject
	private ParserComissoesCamara parserComissoesCamara;

	@Inject
	private EJBDataCacher dataCacher;

	public ComissaoServiceEjb() {
		super(Comissao.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	public List<Comissao> listarComissoesCamara() throws Exception {
		if (!dataCacher.isEntityCached(CACHE_KEY_COMISSOES_CAMARA)) {
			dataCacher.updateDataCache(CACHE_KEY_COMISSOES_CAMARA, parserComissoesCamara.getComissoes());
		}
		return (List<Comissao>) dataCacher.getReferenceData(CACHE_KEY_COMISSOES_CAMARA);
	}

	@Override
	public List<Comissao> listarComissoesSenado() throws Exception {
		if (!dataCacher.isEntityCached(CACHE_KEY_COMISSOES_SENADO)) {
			dataCacher.updateDataCache(CACHE_KEY_COMISSOES_SENADO, parserComissoesSenado.getComissoes());
		}
		return (List<Comissao>) dataCacher.getReferenceData(CACHE_KEY_COMISSOES_SENADO);
	}

	@Override
	public Comissao getBySigla(String sigla) throws Exception {
		if (sigla != null && sigla.indexOf("-") > 0) {
			sigla = sigla.substring(0, sigla.indexOf("-")).trim();
		}

		for (Iterator<Comissao> iterator = listarComissoesCamara().iterator(); iterator.hasNext();) {
			Comissao c = iterator.next();
			if (c.getSigla().trim().equals(sigla)) {
				return c;
			}
		}
		return null;

	}

	@Override
	public String getComissaoCamara(Long idComissao) throws Exception {

		for (Iterator<Comissao> iterator = listarComissoesCamara().iterator(); iterator.hasNext();) {
			Comissao c = iterator.next();
			if (c.getId() == idComissao) {
				return c.getSigla();
			}
		}
		return null;
	}

}
