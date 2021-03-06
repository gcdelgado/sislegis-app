package br.gov.mj.sislegis.app.rest;

import br.gov.mj.sislegis.app.model.EncaminhamentoProposicao;
import br.gov.mj.sislegis.app.service.EncaminhamentoProposicaoService;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

@Path("/encaminhamentoProposicao")
public class EncaminhamentoProposicaoEndpoint {

	@Inject
	private EncaminhamentoProposicaoService service;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(EncaminhamentoProposicao entity, @HeaderParam("Referer") String referer) {
		EncaminhamentoProposicao savedEntity = service.salvarEncaminhamentoProposicao(entity);
		
		return Response.created(
				UriBuilder.fromResource(TipoEncaminhamentoEndpoint.class)
						.path(String.valueOf(savedEntity.getId())).build()).build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Long id) {
		service.deleteById(id);
		return Response.noContent().build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findById(@PathParam("id") Long id) {
		return Response.ok(service.findById(id)).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<EncaminhamentoProposicao> listAll(@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult) {
		return service.listAll();
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(EncaminhamentoProposicao entity) {
		try {
			entity = service.salvarEncaminhamentoProposicao(entity);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
		}

		return Response.noContent().build();
	}

	@GET
	@Path("/proposicao/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<EncaminhamentoProposicao> findByProposicao(@PathParam("id") Long id) {
		final List<EncaminhamentoProposicao> results = service.findByProposicao(id);
		return results;
	}

	@POST
	@Path("/finalizar")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response finalizar(@FormParam("idEncaminhamentoProposicao") Long idEncaminhamentoProposicao, @FormParam("descricaoComentario") String descricaoComentario){
		service.finalizar(idEncaminhamentoProposicao, descricaoComentario);
		return Response.ok().build();
	}
}
