package br.gov.mj.sislegis.app.service.ejbs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.CommunicationException;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import br.gov.mj.sislegis.app.model.Proposicao;
import br.gov.mj.sislegis.app.model.Usuario;
import br.gov.mj.sislegis.app.model.pautacomissao.AgendaComissao;
import br.gov.mj.sislegis.app.service.AbstractPersistence;
import br.gov.mj.sislegis.app.service.UsuarioService;
import br.gov.mj.sislegis.app.util.SislegisUtil;

@Stateless
public class UsuarioServiceEjb extends AbstractPersistence<Usuario, Long> implements UsuarioService {

	@PersistenceContext
	private EntityManager em;
	private SearchControls controls = null;

	public UsuarioServiceEjb() {
		super(Usuario.class);
		controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		controls.setTimeLimit(1000);// maximo 1 segundo de espera
		controls.setCountLimit(20); // maximo 20 resultados
		controls.setReturningAttributes(new String[] { "cn", "userPrincipalName", "displayName", "department",
				"sAMAccountName" });
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Usuario findByEmail(String email) {
		TypedQuery<Usuario> findByIdQuery = em.createQuery(
				"SELECT u FROM Usuario u WHERE upper(u.email) like upper(:email) ORDER BY u.email ASC", Usuario.class);
		findByIdQuery.setParameter("email", email);

		try {
			Usuario user = findByIdQuery.getSingleResult();
			return user;
		} catch (javax.persistence.NoResultException e) {
			// no execption just return null
			return null;
		}

	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Usuario findOrCreateByEmail(String name, String email) {
		Usuario user = findByEmail(email);
		if (user == null) {
			user = new Usuario();
			user.setEmail(email);
			user.setNome(name);
			save(user);
		}
		return user;
	}

	@Override
	public List<Usuario> listUsuariosSeguidoresDeComissao(AgendaComissao agenda) {
		TypedQuery<Usuario> findByIdQuery = em.createQuery(
				"SELECT u FROM Usuario u join u.agendasSeguidas agendas where agendas.id=:idAgenda", Usuario.class);

		findByIdQuery.setParameter("idAgenda", agenda.getId());
		return findByIdQuery.getResultList();

	}

	@Override
	public List<Usuario> listUsuariosSeguidoresDeProposicao(Proposicao proposicao) {
		TypedQuery<Usuario> findByIdQuery = em.createQuery(
				"SELECT u FROM Usuario u join u.proposicoesSeguidas prop where prop.id=:idProp", Usuario.class);

		findByIdQuery.setParameter("idProp", proposicao.getId());
		return findByIdQuery.getResultList();

	}

	@Override
	public List<Usuario> findByNome(String nome) {
		TypedQuery<Usuario> findByIdQuery = em.createQuery(
				"SELECT u FROM Usuario u WHERE upper(u.nome) like upper(:nome) ORDER BY u.nome ASC", Usuario.class);
		findByIdQuery.setParameter("nome", "%" + nome + "%");
		return findByIdQuery.getResultList();
	}

	@Override
	public List<Usuario> findByIdEquipe(Long idEquipe) {

		Query query = em.createNativeQuery("SELECT u.* FROM Usuario u "
				+ " inner join equipe_usuario eu on u.id = eu.usuario_id "
				+ " inner join Equipe e on e.id = eu.equipe_id " + "	WHERE e.id = :idEquipe ORDER BY u.nome ASC",
				Usuario.class);
		query.setParameter("idEquipe", idEquipe);
		List<Usuario> usuarios = query.getResultList();

		return usuarios;
	}

	/**
	 * Definido no xml de configuração do servidor. Da forma abaixo:<br>
	 * 
	 * <pre>
	 * <subsystem xmlns="urn:jboss:domain:naming:2.0">             
	 *      <bindings>
	 *          <external-context module="org.jboss.as.naming" name="java:global/federation/ldap/mjldap" class="javax.naming.directory.InitialDirC    ontext" cache="true">
	 *               <environment>
	 *                   <property name="java.naming.factory.initial" value="com.sun.jndi.ldap.LdapCtxFactory" />
	 *                   <property name="java.naming.provider.url" value="ldap://SERVIDOR:389/SUBTREE" />
	 *                   <property name="java.naming.security.authentication" value="simple" />
	 *                   <property name="java.naming.security.principal" value="PRINCIPAL" />
	 *                   <property name="java.naming.security.credentials" value="****" />
	 *               </environment>
	 *          </external-context>
	 *      </bindings>
	 * </subsystem>
	 * 
	 * </pre>
	 */
	// @Resource(lookup = "java:global/federation/ldap/mjldap")
	private javax.naming.directory.InitialDirContext ldapContext;

	@Override
	public List<Usuario> findByNomeOnLDAP(String nome) {
		List<Usuario> usuarios = new ArrayList<Usuario>();
		try {
			ldapContext = (InitialDirContext) InitialContext.doLookup("java:global/federation/ldap/mjldap");

			NamingEnumeration<SearchResult> results = ldapContext.search("OU=SISLEGIS", "(&(objectclass=person)(cn="
					+ nome + "*))", controls);
			if (results.hasMoreElements()) {
				while (results.hasMoreElements()) {
					SearchResult searchResult = (SearchResult) results.nextElement();
					Usuario ldapUser = new Usuario();
					ldapUser.setEmail(String.valueOf(searchResult.getAttributes().get("userPrincipalName").get()));
					ldapUser.setNome(String.valueOf(searchResult.getAttributes().get("cn").get()));
					usuarios.add(ldapUser);
				}
			}

		} catch (NamingException e) {

			try {
				if (e.getRootCause().getCause().getCause().getCause() instanceof CommunicationException) {
					if (Logger.getLogger(SislegisUtil.SISLEGIS_LOGGER).isLoggable(Level.FINE)) {
						Logger.getLogger(SislegisUtil.SISLEGIS_LOGGER).log(Level.SEVERE,
								"Não foi possível carregar o recurso do LDAP. Sua rede pode acessar o LDAP do MJ?");
					} else {
						Logger.getLogger(SislegisUtil.SISLEGIS_LOGGER).log(Level.SEVERE,
								"Não foi possível carregar o recurso do LDAP. Sua rede pode acessar o LDAP do MJ?", e);
					}
				} else {
					Logger.getLogger(SislegisUtil.SISLEGIS_LOGGER).log(Level.SEVERE,
							"Houve um erro consultando o LDAP", e);
				}
			} catch (Exception e1) {
				Logger.getLogger(SislegisUtil.SISLEGIS_LOGGER).log(Level.SEVERE, "Houve um erro consultando o LDAP", e);
			}

		}
		return usuarios;
	}

	@Override
	public Usuario loadComAgendasSeguidas(Long id) {
		Usuario user = findById(id);
		user.getAgendasSeguidas().size();
		return user;
	}

	@Override
	public Collection<Proposicao> proposicoesSeguidas(Long id) {
		Collection<Proposicao> props = (findById(id)).getProposicoesSeguidas();
		props.size();
		return props;
	}

}
