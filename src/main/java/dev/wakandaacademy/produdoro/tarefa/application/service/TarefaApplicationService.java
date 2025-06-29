package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.config.security.service.TokenService;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaDoUsuarioListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaEditaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class TarefaApplicationService implements TarefaService {
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;


    @Override
    public TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest) {
        log.info("[inicia] TarefaApplicationService - criaNovaTarefa");
        Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest));
        log.info("[finaliza] TarefaApplicationService - criaNovaTarefa");
        return TarefaIdResponse.builder().idTarefa(tarefaCriada.getIdTarefa()).build();
    }

    @Override
    public Tarefa detalhaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - detalhaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
        return tarefa;
    }

    @Override
    public void limparTodasTarefas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - limparTodasTarefas");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        if (!usuarioPorEmail.getIdUsuario().equals(idUsuario)) {
            throw APIException.build(HttpStatus.UNAUTHORIZED,
                    "Usuário não autorizado para requisição solicitada!");
        }
        usuarioPorEmail.validaUsuario(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasDoUsuario(idUsuario);
        if (tarefas.isEmpty())
            throw APIException.build(HttpStatus.CONFLICT,
                    "Usuário não possui tarefas cadastradas!");
        if (tarefas.size() >= 2)
            tarefaRepository.deletaTodasAsTarefasDoUsuario(idUsuario);
        log.info("[finaliza] TarefaApplicationService - limparTodasTarefas");

    }

    @Override
    public List<TarefaDoUsuarioListResponse> buscaTarefasDoUsuario(String emailUsuario, UUID idUsuario) {
        log.info("[start] TarefaApplicationService - buscaTarefasDoUsuario");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(emailUsuario);
        Usuario usuario = usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuario.tokenPertenceAoUsuario(usuarioPorEmail);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasDoUsuario(idUsuario);
        log.info("[finish] TarefaApplicationService - buscaTarefasDoUsuario");
        return TarefaDoUsuarioListResponse.converte(tarefas);
    }

    @Override
    public void ativaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - ativaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "id da tarefa inválido"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        tarefa.verficaSePodeSerAtiva();
        tarefaRepository.inativaTarefa(usuarioPorEmail.getIdUsuario());
        tarefa.ativaTarefa();
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - ativaTarefa");

    }

    @Override
    public void editaTarefa(String token, UUID idTarefa, TarefaEditaRequest request) {
        log.info("[inicia] TarefaApplicationService - editaTarefa");
        Tarefa tarefa = detalhaTarefa(token, idTarefa);
        tarefa.editarDescricao(request.getDescricao());
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - editaTarefa");
    }
}


